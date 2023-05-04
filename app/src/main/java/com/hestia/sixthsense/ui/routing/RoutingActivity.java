package com.hestia.sixthsense.ui.routing;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hestia.sixthsense.R;
import com.hestia.sixthsense.data.network.model.beacon.GraphResponse;
import com.hestia.sixthsense.data.network.model.beacon.NodeResponse;
import com.hestia.sixthsense.ui.main.AboutUsActivity;
import com.hestia.sixthsense.ui.main.MainActivity;
import com.hestia.sixthsense.ui.main.SettingsActivity;
import com.hestia.sixthsense.ui.route.RouteActivity;
import com.hestia.sixthsense.ui.routing.detail.RoutingDetailLocations;
import com.hestia.sixthsense.ui.routing.detail.RoutingDetailNodes;
import com.hestia.sixthsense.ui.routing.master.RoutingMaster;
import com.hestia.sixthsense.utils.AppConstants;

/**
 * Экран (Activity), в котором происходит определение локации и точки назначения
 * RoutingActivity использует паттерн Master-Detail
 *
 * @see MainActivity Главная активность
 * @see com.hestia.sixthsense.ui.route.RouteActivity Экран "Ведение по маршруту"
 * @see SettingsActivity Экран "Настройки"
 * @see AboutUsActivity Экран "О нас"
 *
 * @author Dmitry Abakumov <killerinshadow2@gmail.com>
 */
public class RoutingActivity extends FragmentActivity implements RoutingMaster.OnFragmentInteractionListener, RoutingDetailLocations.OnLocationFragmentInteractionListener, RoutingDetailNodes.OnNodeFragmentInteractionListener, TextToSpeech.OnInitListener {
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private final String TAG = "RoutingActivity";
    private PowerManager powerManager = null;

    /**
     * WakeLock — это механизм, указывающий, что вашему приложению необходимо,
     * чтобы устройство оставалось включенным.
     */
    private PowerManager.WakeLock wakeLock = null;
    private GraphResponse selectedLocation = null;
    private NodeResponse selectedNode = null;
    private int lastViewId = -1;
    /*private Compass compass;

    private final int accuracyOfCompass = 15;*/


    private TextToSpeech textToSpeech;
    private BeaconBroadcastListener mBeaconBroadcastListener;

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if(wakeLock != null)
            wakeLock.acquire();
        textToSpeech = new TextToSpeech(getApplicationContext(), this);
    }

    private BeaconBroadcastListener.BroadcastCallback broadcastCallback = new BeaconBroadcastListener.BroadcastCallback() {
        @Override
        public void onLocation(GraphResponse location) {
            UpdateSelectedLocation(location);
        }

        @Override
        public void onNewBroadcastBeacon(NodeResponse node) {
            if(node.getBroadcastText().length() > 0){
                textToSpeech.speak(node.getBroadcastText(), TextToSpeech.QUEUE_FLUSH, null, "");
                if(AppConstants.DEBUG_MODE)
                    Toast.makeText(getApplicationContext(), String.format("%s - %s", node.getMac(),node.getBroadcastText()), Toast.LENGTH_LONG).show();
            }else
            if(AppConstants.DEBUG_MODE)
                Toast.makeText(getApplicationContext(), String.format("%s - НЕ БРОДКАСТ", node.getMac()), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing);
        powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ":LOCK_"+TAG);

        /*compass = new Compass(getApplicationContext());*/

        textToSpeech = new TextToSpeech(this, this);
        mFragmentManager = getSupportFragmentManager();
// Set default fragment
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.routing_fragment, RoutingMaster.newInstance("", ""));
        mFragmentTransaction.commit();
    }


    /**
     * Срабатывает, когда пользователь выбирает новое местоположение в Fragment {@link RoutingDetailLocations}
     * После ручного обновления локации по нажатию кнопки
     *
     * @param item Новая выбранная локация пользователем
     */
    @Override
    public void onListFragmentInteraction(GraphResponse item) {
        switch (lastViewId) {
            case R.id.find_route_building_selector:
                UpdateSelectedLocation(item);
                mBeaconBroadcastListener.setLocation(item); // change listening location
                break;
        }
    }

    /**
     * Изменение текущей локации
     * @param location Новая локация
     */
    private void UpdateSelectedLocation(GraphResponse location) {
        selectedLocation = location;
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.routing_fragment, RoutingMaster.newInstance(selectedLocation.getName(), ""));
        mFragmentTransaction.commit();

    }

    /**
     * Запускается, когда пользователь выбирает пункт назначения в Fragment {@link RoutingDetailNodes}
     * @param item Новый пункт назначения
     */
    @Override
    public void onListFragmentInteraction(NodeResponse item) {
        switch (lastViewId) {
            case R.id.find_route_campus_selector:
                selectedNode = item;
                break;
        }
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.routing_fragment, RoutingMaster.newInstance(selectedLocation.getName(), selectedNode.getName()));
        mFragmentTransaction.commit();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onFragmentInteraction(int viewId) {
        mFragmentTransaction = mFragmentManager.beginTransaction();
        switch (viewId) {
            case R.id.find_route_building_selector:
                // Кнопка, после нажатия на которую запускается фрагмент доступных локаций
                // Кнопка "Локация"
                mFragmentTransaction.replace(R.id.routing_fragment, RoutingDetailLocations.newInstance(1));
                break;
            case R.id.find_route_campus_selector:
                // Кнопка выбора пункта назначения
                selectedNode = null;
                mFragmentTransaction.replace(R.id.routing_fragment, RoutingDetailNodes.newInstance(selectedLocation.getNodes()));
                break;
            case R.id.find_route_find_route_button:
                // Кнопка найти маршрут

                /*int azimuth = compass.getAzimuth();
                int az = selectedLocation.getAzimuth() - azimuth;*/

                if (selectedNode != null && selectedLocation != null /*&& (Math.abs(az) <= accuracyOfCompass)*/) {
                    Intent intent = new Intent(RoutingActivity.this, RouteActivity.class);
                    intent.putExtra("destination", selectedNode.getMac());
                    intent.putExtra("location", new Gson().toJson(selectedLocation));
                    mBeaconBroadcastListener.Stop();
                    startActivity(intent);
                } /*else if (Math.abs(az) > accuracyOfCompass) {
                    String side = "";

                    if(az < 0){
                        if(Math.abs(az) > 180) side = "направо на " + (360 - Math.abs(az));
                        else side = "налево на " + Math.abs(az);
                    }else{
                        if(Math.abs(az) > 180) side = "налево на " + (360 - Math.abs(az));
                        else side = "направо на " + Math.abs(az);
                    }

                    textToSpeech.speak("Повернитесь " + side +
                            " градусов и нажмите найти маршрут", TextToSpeech.QUEUE_FLUSH, null, "");
                    Log.d("CompassTest", (azimuth) + "");
                }*/
                break;
            case R.id.find_route_debug:

                break;
        }
        lastViewId = viewId;
        mFragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*compass.resume();*/
    }


    @Override
    protected void onPause() {
        /*compass.pause();*/
        try {
            mBeaconBroadcastListener.Stop();
            if(textToSpeech != null)
                textToSpeech.shutdown();
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        } if(wakeLock != null)
            wakeLock.release();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mBeaconBroadcastListener.Stop();
            if(textToSpeech != null)
                textToSpeech.shutdown();
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    /*
     * Don't know why, but here is somewhere bug, onInit triggers twice,
     * Therefore i put there some check. Otherwise it cause unbining BeaconManager's Consumers.
     * It unbind only last binded one;
     * */
    @Override
    public void onInit(int i) {
        if(mBeaconBroadcastListener != null)
            mBeaconBroadcastListener.Stop();
        mBeaconBroadcastListener = new BeaconBroadcastListener(getApplicationContext(), broadcastCallback);
        mBeaconBroadcastListener.Start();
        if(AppConstants.DEBUG_MODE)
            Toast.makeText(getBaseContext(), "Инициализировано", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}