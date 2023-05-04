package com.hestia.sixthsense.data.network.model.beacon;

import com.google.gson.annotations.SerializedName;
import com.hestia.sixthsense.data.network.AppApiHelper;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Модель Beacon (BLE-метка, которая храниться на сервере)
 * <p><ul>
 * <li>Может быть физической (реально установленной)
 * <li>или фантомной (метка которой нет в реальности. Ее не возможно поймать.
 *  Такие метки используются в основном там, где невозможно или не целесообразно ставить физические метки.
 *  Хотя эти метки невозможно поймать, но они как и обычные метки используются для введения по маршруту.
 *  Например, если маяки идут по коридору и мы хотим где-то повернуть направо или налево,
 *  то на повороте можно сделать не физический маячок, а просто на графе отметить его как точку
 *  к которой нужно попасть, у которой есть своя инструкция
 *  Поворот и т.п)
 *
 * @see GraphResponse Сетевая модель локации
 * @see NodeResponse Сетевая модель ноды (вершины графа)
 * @see EdgeResponse Сетевая модель ребра
 *
 * @author Dmitry Abakumov <killerinshadow2@gmail.com>
 */
public class BeaconResponse {
    @SerializedName("id")
    private int id;
    @SerializedName("mac")
    private String mac;
    @SerializedName("name")
    private String name;
    @SerializedName("location")
    private int locationId;

    public int getId() {
        return id;
    }

    public String getMac() {
        return mac;
    }

    public String getName() {
        return name;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String toString() {
        return "BeaconResponse{" +
                "id=" + id +
                ", mac='" + mac + '\'' +
                ", name='" + name + '\'' +
                ", locationId=" + locationId +
                '}';
    }

    /**
     * @see AppApiHelper#getLocation(int)
     * @param callback коллбек получения локации
     */
    public void getLocationFromServer(DefaultObserver<GraphResponse> callback){
        AppApiHelper.getLocation(this.locationId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(callback);
    }
}