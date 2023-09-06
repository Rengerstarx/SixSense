package com.hestia.sixthsense2.data.network;

import androidx.annotation.NonNull;

import com.hestia.sixthsense2.data.network.model.beacon.BeaconResponse;
import com.hestia.sixthsense2.data.network.model.beacon.GraphResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.hestia.sixthsense2.utils.AppConstants.API_BASE_URL;
import static com.hestia.sixthsense2.utils.AppConstants.API_CONNECT_TIMEOUT;
import static com.hestia.sixthsense2.utils.AppConstants.SERVER_LIST;

/**
 * Класс, в котором происходит работа с сетью
 * Создается объект Retrofit и делаются запросы на end-поинты которые указаны в {@link ApiHelper}
 * Для работы с сетью используется библиотека Retrofit с call-адапетром RxJava
 *
 * <p>Если локация определяется по маячкам:</p>
 * <p> -Сканируется пространство, получается список мак-адресов</p>
 * <p> -По найденным мак-адресам с сервера ("http://feel.sfedu.ru/hestia/api/beaconbymac/")</p>
 * <p>     запрашивается информация о маячках (в том числе и id локации, к которой принадлежат маячки)</p>
 * <p>     c помощью метода {@link #getBeaconByMac(String)}</p>
 * <p> -По полученной id локации с сервера ("http://feel.sfedu.ru/hestia/api/location/{id}/")</p>
 * <p>     запрашивается информация о локации (в том числе и имя)</p>
 * <p>     с помощью метода {@link #getLocation(int)}</p>
 * <p>Если локация определяется пользователем по нажатию кнопки:</p>
 * <p> -С сервера запрашиваются все локации ("http://feel.sfedu.ru/hestia/api/location/")</p>
 * <p>     с помощью метода {@link #getLocations()}</p>
 * <p> -Выводятся имена полученных локаций</p>
 *
 * @author Dmitry Abakumov <killerinshadow2@gmail.com>
 */
public class AppApiHelper {

    /**
     * Создается retrofit adapter
     *
     * Устанавливаются параметры для retrofit
     * Для работы используются запросы, используемые в {@link ApiHelper}
     *
     * @param baseUrl базовый адрес сервера
     * @return retrofit adapter
     * Чтобы узнать подробнее, следует прочитать о библиотеке retrofit
     */
    @NonNull
    private static ApiHelper createRetrofitAdapter(String baseUrl) {
        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(API_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit.create(ApiHelper.class);
    }

    private static ApiHelper createRetrofitAdapter() {
        return createRetrofitAdapter(API_BASE_URL);
    }


    /*public static void scheduleNetworkJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, SyncService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);

        // Время запуска службы
        builder.setMinimumLatency(SCHEDULER_MINIMUM_LATENCY);
        builder.setOverrideDeadline(SCHEDULER_OVERRIDE_DEAD_LINE);

        // Необходимое сетевое подключение
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

        // Период синхронизации
        builder.setPeriodic(SCHEDULER_SCAN_PERIOD);

        // Persist
        builder.setPersisted(true);

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }*/

    /**
     * Получение всех локаций с сервера единомоментно. <p></p>
     * Данный метод срабатывает, когда не представляется возможным определить
     *  текущую локацию с помощью маячков, при нажатии кнопки локация в
     *  {@link com.hestia.sixthsense2.ui.routing.detail.RoutingDetailLocations}.
     *
     * @return Список локаций в виде list of {@link GraphResponse}
     */
    public static Observable<List<GraphResponse>> getLocations() {
        int serverIndex = 0;
        PublishSubject<List<GraphResponse>> response = PublishSubject.create();
        MakeMultiServerCallGetLocations(serverIndex, response);
        return response;
    }


    /**
     * Получение определенной локации по ее id. <p></p>
     * Метод срабатывает при запуске и инициализации
     *  {@link com.hestia.sixthsense2.ui.routing.RoutingActivity}
     *  в {@link com.hestia.sixthsense2.ui.routing.BeaconBroadcastListener}
     *
     *
     * @param id Идентификатор локации, которую нужно получить при запросе
     * @return локация в виде {@link GraphResponse}
     */
    public static PublishSubject<GraphResponse> getLocation(int id) {
        int serverIndex = 0;
        PublishSubject<GraphResponse> response = PublishSubject.create();
        MakeMultiServerCallGetLocation(id, serverIndex, response);
        return response;
    }

    /**
     * Получение маячка с сервера с помощью его mac-адреса
     *
     * @param mac mac-адрес маячка, по которому происходит запрос на сервер
     * @return возвращается маячок в виде {@link GraphResponse}
     */
    public static PublishSubject<List<BeaconResponse>> getBeaconByMac(String mac) {
        //mac = mac.substring(6);
        int serverIndex = 0;
        PublishSubject<List<BeaconResponse>> response = PublishSubject.create();
        MakeMultiServerCallGetBeaconByMac(mac, serverIndex, response);
        return response;
    }


    /**
     * В методе происходит непосредственный запрос на сервер с помощью библиотеки retrofit
     *  с целью получения списка локаций
     *
     *
     * @param serverIndex индекс в массиве серверов
     *  ({@link com.hestia.sixthsense2.utils.AppConstants#SERVER_LIST})
     *  В массиве используются два сервера, основной(индекс 0) и запасной(индекс 1)
     * @param response Список локаций
     */
    private static void MakeMultiServerCallGetLocations(int serverIndex, PublishSubject<List<GraphResponse>> response) {
        if (serverIndex > SERVER_LIST.length - 1) {
            response.onError(new Throwable());
            return;
        }
        createRetrofitAdapter(SERVER_LIST[serverIndex])
                .getLocations()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<List<GraphResponse>>() {
                    @Override
                    public void onSuccess(List<GraphResponse> graphResponse) {
                        response.onNext(graphResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        MakeMultiServerCallGetLocations(serverIndex + 1, response);
                    }
                });
        ;
    }

    /**
     *
     * В методе происходит непосредственный запрос на сервер с помощью библиотеки retrofit
     *  с целью получения локации по ее идентификатору
     *
     *
     * @param serverIndex индекс в массиве серверов
     *  ({@link com.hestia.sixthsense2.utils.AppConstants#SERVER_LIST})
     *  В массиве используются два сервера, основной(индекс 0) и запасной(индекс 1)
     * @param response Локация
     * @param id Идентификатор запрашиваемой локации
     */
    private static void MakeMultiServerCallGetLocation(int id, int serverIndex, PublishSubject<GraphResponse> response) {
        if (serverIndex > SERVER_LIST.length - 1) {
            response.onError(new Throwable());
            return;
        }
        createRetrofitAdapter()
                .getLocation(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<GraphResponse>() {
                    @Override
                    public void onSuccess(GraphResponse graphResponse) {
                        response.onNext(graphResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        MakeMultiServerCallGetLocation(id, serverIndex + 1, response);

                    }
                });
    }

    /**
     *
     * В методе происходит непосредственный запрос на сервер с помощью библиотеки retrofit
     *  с целью получения локации по ее идентификатору
     *
     *
     * @param serverIndex индекс в массиве серверов
     *  ({@link com.hestia.sixthsense2.utils.AppConstants#SERVER_LIST})
     *  В массиве используются два сервера, основной(индекс 0) и запасной(индекс 1)
     * @param response Маячок
     * @param mac Mac-адрес маячка, информация о котором запрашивается с сервера
     */
    private static void MakeMultiServerCallGetBeaconByMac(String mac, int serverIndex, PublishSubject<List<BeaconResponse>> response) {
        if (serverIndex > SERVER_LIST.length - 1) {
            response.onError(new Throwable());
            return;
        }
        createRetrofitAdapter()
                .getBeaconByMac(mac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<List<BeaconResponse>>() {
                    @Override
                    public void onSuccess(List<BeaconResponse> beaconResponse) {
                        response.onNext(beaconResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        MakeMultiServerCallGetBeaconByMac(mac,serverIndex+1,response);
                    }
                });
    }

}
