package com.hestia.sixthsense2.data.network;

import com.hestia.sixthsense2.data.network.model.beacon.BeaconResponse;
import com.hestia.sixthsense2.data.network.model.beacon.GraphResponse;

import java.util.List;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Интерфейс для работы с сетью через библиетеку Retrofit
 * Задаются фукнции для запроса на сервер, в скобках прибавочный адрес к базовому, куда
 * будет обращаться библиотека, базовый адрес храниться здесь {@link com.hestia.sixthsense2.utils.AppConstants#API_BASE_URL}
 *
 * @see AppApiHelper Реализация интерфейса
 *
 * @author Rengerstar <vip.bekezin@mail.ru>
 */
public interface ApiHelper {

    /**
     * @see AppApiHelper#getLocations() 
     */
    @GET("api/location/")
    Single<List<GraphResponse>> getLocations();

    /**
     * @see AppApiHelper#getLocation(int) 
     */
    @GET("api/location/{id}/")
    Single<GraphResponse> getLocation(@Path("id")int id);

    /**
     * @see AppApiHelper#getBeaconByMac(String) 
     */
    @GET("api/beaconbymac/")
    Single<List<BeaconResponse>> getBeaconByMac(@Query("mac") String mac);

}
