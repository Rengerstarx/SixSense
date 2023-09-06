package com.hestia.sixthsense2.data.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hestia.sixthsense2.data.network.model.beacon.GraphResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для работы с кешем (сохранение и получение данных от туда)
 * Когда есть возможность подключиться к сети, данные кешируются и потом,
 * когда такой возможности нет данные запрашиваются не с сервера, а из кеша
 *
 * @author Dmitry Abakumov <killerinshadow2@gmail.com>
 */
public class LocationCacheHelper {
    public static final String APP_LOCATION_CACHE = "location-cache";
    public static final String PARAM_LOCATION = "location";

    private Context context;
    private SharedPreferences mCache;


    public LocationCacheHelper(Context context) {
        this.context = context;
        mCache = context.getSharedPreferences(APP_LOCATION_CACHE,Context.MODE_PRIVATE);
    }

    /**
     * Получение последнего сохраненного Json в виде List<GraphResponse>
     */
    public List<GraphResponse> getLastCache(){
        String cachedJson = mCache.getString(PARAM_LOCATION,"");
        List<GraphResponse> cachedLocations = new ArrayList<>();
        if(cachedJson.length() == 0)
            return cachedLocations;
        else{
            return new Gson().fromJson(cachedJson,new TypeToken<List<GraphResponse>>(){}.getType());
        }
    }

    /**
     * Сохранение списка локаций, как Gson в кеш
     * @param locations списаок локаций
     */
    public void saveLastCache(List<GraphResponse> locations){
        String cachedJson = new Gson().toJson(locations);
        Editor editor = mCache.edit();
        editor.putString(PARAM_LOCATION,cachedJson);
        editor.commit();

    }

    /**
     * Проверка, есть ли соединение с сетью
     * @param context context
     * @return есть ли соединение с сетью
     */
    public static boolean isNetAvailable(Context context){
        // Check connection
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = false;
        try {
            isConnected = activeNetwork.isConnectedOrConnecting()
                    && activeNetwork.isAvailable()
                    && (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                    || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE);
        }catch (Exception e){
            isConnected = false;
        }
    return isConnected;
    }

}
