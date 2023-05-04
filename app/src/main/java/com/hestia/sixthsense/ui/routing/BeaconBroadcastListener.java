package com.hestia.sixthsense.ui.routing;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.hestia.sixthsense.data.network.AppApiHelper;
import com.hestia.sixthsense.data.network.LocationCacheHelper;
import com.hestia.sixthsense.data.network.model.beacon.BeaconResponse;
import com.hestia.sixthsense.data.network.model.beacon.GraphResponse;
import com.hestia.sixthsense.data.network.model.beacon.NodeResponse;
import com.hestia.sixthsense.ui.main.SettingsActivity;
import com.hestia.sixthsense.utils.AppConstants;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.ArmaRssiFilter;

import java.util.Collection;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;


/**
 * Класс, где происходит первоначалальное определение локации по найденным маякам
 * {@link AppApiHelper}
 * <p>
 * А также реализуется режим прогулки, где просто определяется новые маяки и воспроизводится их текст
 * Работает за счет библиотекаи altbeacon. (https://altbeacon.github.io/android-beacon-library/)
 * Примечание: этот класс выполняет несколько сетевых запросов.
 * Это может быть проблемой для некоторых задач.
 *
 * @see BeaconBroadcastListenerHelper
 * @see com.hestia.sixthsense.ui.route.RouteActivity
 */

public class BeaconBroadcastListener implements BeaconConsumer {
    private static final String TAG = "BeaconBroadcastListener";
    private BroadcastCallback broadcastCallback = null;
    private BeaconBroadcastListenerHelper beaconHelper = null;
    private Context context = null;
    private BeaconManager mBeaconManager = null;

    private int updateTime = 1000;
    private float minUpdateDistance;
    private int distanceBufferSize;
    private float distanceDelta;
    private int defaultDistance = 1;
    private int distanceOfOneMeter = -65;

    private GraphResponse locationGraph = null;
    private Region region = new Region("AllBeaconsRegion", null, null, null);
    private String lastDetectedBeacon = "";
    private boolean isAlreadyRecieved = false;

    /**
     * @see RangeNotifier
     */
    private RangeNotifier mRangeNotifier = new RangeNotifier() {
        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
            Log.d(TAG,"Work came "+collection.size());
            try {
                if (collection.size() > 0) {
                    // Если графа (локации) еще нет
                    if (locationGraph == null)
                        GetLocationByBeacons(collection);
                    else {
                        if(!beaconHelper.isFilled){
                            beaconHelper.fillBeaconBuffer(collection);
                            if(AppConstants.DEBUG_MODE)
                                Toast.makeText(context,String.format("%d / %d",beaconHelper.filledIterationCount,beaconHelper.maxFillIterations),Toast.LENGTH_LONG).show();
                        }else{
                            // Если у нас есть местоположение, отфильтруйте маяки по местоположению
                            Collection<Beacon> filteredBeacons = beaconHelper.filterByLocation(collection);
                            //filteredBeacons = beaconHelper.filterByBroadcast(filteredBeacons);
                            String strongestBeacon = beaconHelper.getStrongestBeacon(filteredBeacons).substring(6);
                            if (!strongestBeacon.equals(lastDetectedBeacon) && strongestBeacon.length() != 0) {
                                lastDetectedBeacon = strongestBeacon;
                                broadcastCallback.onNewBroadcastBeacon(locationGraph.getNodeByMac(strongestBeacon));
                            }
                        }
                    }
                }
            } catch (Exception ex) {
            }
        }
    };

    public BeaconBroadcastListener(Context context, BroadcastCallback broadcastCallback) {
        this.context = context;
        this.broadcastCallback = broadcastCallback;
        InitializeBeaconManager();
    }

    public BeaconBroadcastListener(Context context, BroadcastCallback broadcastCallback, int updateTime) {
        this.context = context;
        this.broadcastCallback = broadcastCallback;
        this.updateTime = updateTime;
        InitializeBeaconManager();
    }

    /**
     * Установка параметров поиска для менеджера маячков
     */
    private void InitializeBeaconManager() {
        InitSettings();
        ArmaRssiFilter.setDEFAULT_ARMA_SPEED(0.35f);
        BeaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);
        mBeaconManager = BeaconManager.getInstanceForApplication(this.context);
        mBeaconManager.getBeaconParsers().clear();
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.URI_BEACON_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        //mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-1=4843,i:4-19,p:24-24,d:25-25"));
        mBeaconManager.setForegroundScanPeriod(updateTime);
    }

    private void InitSettings() {
        SharedPreferences preferences = context.getSharedPreferences(AppConstants.SETTINGS_NAME, Context.MODE_PRIVATE);
        minUpdateDistance = -80f;
        distanceBufferSize = preferences.getInt(SettingsActivity.SETTINGS_BROADCAST_BUFFER_SIZE, AppConstants.BROADCAST_DISTANCE_BUFFER_SIZE);
        distanceOfOneMeter = preferences.getInt(SettingsActivity.SETTINGS_DISTANCE_OF_ONE_METER, AppConstants.DISTANCE_OF_ONE_METER);
        distanceDelta = preferences.getFloat(SettingsActivity.SETTINGS_DELTA, AppConstants.AVERAGE_DISTANCE_DELTA);
        updateTime = preferences.getInt(SettingsActivity.SETTINGS_UPDATE_TIME, AppConstants.UPDATE_TIME);
    }

    /**
     * Запуск прослущивания эфира
     */
    public void Start() {
        mBeaconManager.addRangeNotifier(mRangeNotifier);
        mBeaconManager.bind(this);
    }

    /**
     * Остановка прослушивания эфира
     */
    public void Stop() {
        try {
            mBeaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.removeRangeNotifier(mRangeNotifier);
        lastDetectedBeacon = "";
        mBeaconManager.unbind(this);
    }


    /**
     * Подключение к сервису сканирования
     */
    @Override
    public void onBeaconServiceConnect() {
        if(AppConstants.DEBUG_MODE)
            Toast.makeText(this.context, "Сервис подключен", Toast.LENGTH_LONG).show();
        try {
                mBeaconManager.updateScanPeriods();
                mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    @Override
    public Context getApplicationContext() {
        return this.context.getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        this.context.unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return this.context.bindService(intent, serviceConnection, i);
    }

    /**
     * Определение id локации по маячкам
     * Полученные mac-адреса мачков из эфира отправляются на сервер для принятия информации о них
     * По полученной информации (в том числе id локации, маяк которой принадлежит) с сервера
     *  запрашивается текущая локация
     *
     * Если подключиться к сети не представляется возможным, то информация о локации загружается
     *  из кеша {@link LocationCacheHelper}
     *  Чтобы была такая возможность, необходимо хотя бы один раз загрузить информацию с сервера
     *
     * @param collection коллекция полученных маячков(mac-адресов) из эфира, по которым определяется
     *                   текущая локация
     */
    private void GetLocationByBeacons(Collection<Beacon> collection) {
        if (LocationCacheHelper.isNetAvailable(context))
            GetLocationFromNet(collection);
        else
            GetLocationFromCache(collection);
    }

    /**
     * Получение локации с сервера по маячкам
     *
     * @see #GetLocationByBeacons(Collection)
     * @param collection коллекция полученных маячков(mac-адресов) из эфира, по которым определяется
     *                   текущая локация
     */
    private void GetLocationFromNet(Collection<Beacon> collection) {
        for (Beacon beacon : collection) {
            AppApiHelper.getBeaconByMac(beacon.getBluetoothAddress().substring(6))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DefaultObserver<List<BeaconResponse>>() {
                        @Override
                        public void onNext(@NonNull List<BeaconResponse> beaconResponses) {
                            if (beaconResponses.size() > 0) {
                                BeaconResponse beaconFromServer = beaconResponses.get(0);
                                beaconFromServer.getLocationFromServer(new DefaultObserver<GraphResponse>() {
                                    @Override
                                    public void onNext(@NonNull GraphResponse graphResponse) {
                                        if (broadcastCallback != null) {
                                            if(locationGraph == null){
                                                locationGraph = graphResponse;
                                                broadcastCallback.onLocation(locationGraph);
                                                beaconHelper = new BeaconBroadcastListenerHelper(locationGraph,
                                                        distanceBufferSize,
                                                        defaultDistance,
                                                        (int)minUpdateDistance,
                                                        distanceDelta);

                                            }
                                        }
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {}
                                    @Override
                                    public void onComplete() {}
                                });
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            GetLocationFromCache(collection);
                        }

                        @Override
                        public void onComplete() {}
                    });
        }
    }

    /**
     * Получение локации из кеша по маячкам
     *
     * @see #GetLocationByBeacons(Collection)
     * @param collection коллекция полученных маячков(mac-адресов) из эфира, по которым определяется
     *                   текущая локация
     */
    private void GetLocationFromCache(Collection<Beacon> collection) {
        List<GraphResponse> cached = new LocationCacheHelper(context).getLastCache();
        if (cached.size() == 0 || locationGraph != null) {
            return;
        }
        for (GraphResponse location : cached) {
            for (Beacon beacon : collection) {
                if (location.isBeaconContain(beacon.getBluetoothAddress().substring(6))) {
                    // that's mean we found location;
                    locationGraph = location;
                    if (broadcastCallback != null) {
                        broadcastCallback.onLocation(locationGraph);
                        beaconHelper = new BeaconBroadcastListenerHelper(locationGraph,
                                distanceBufferSize,
                                defaultDistance,
                                (int)minUpdateDistance,
                                distanceDelta);
                    }
                    return;
                }
            }
        }

    }

    /**
     * Когда пользователь выбирает локацию вручную срабатывает этот метод
     * Если пользователь начал выбирать локацию быстрее, чем автоопределение локации,
     * Тогда beaconHelper будет null, поэтому в методе есть условие
     * @param item Новая локация
     */
    public void setLocation(GraphResponse item) {
        locationGraph = item;
        if(beaconHelper == null){
            beaconHelper = new BeaconBroadcastListenerHelper(locationGraph,
                    distanceBufferSize,
                    defaultDistance,
                    (int)minUpdateDistance,
                    distanceDelta);
        }else{
            beaconHelper.isFilled = false;
            beaconHelper.filledIterationCount = 0;
        }
    }


    public interface BroadcastCallback {
        // Triggers when BeaconBroadcastListener finds beacon's location
        void onLocation(GraphResponse location);

        void onNewBroadcastBeacon(NodeResponse node);
    }

}
