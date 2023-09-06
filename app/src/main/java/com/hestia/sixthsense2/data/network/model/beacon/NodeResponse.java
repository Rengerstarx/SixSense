package com.hestia.sixthsense2.data.network.model.beacon;

import com.google.gson.annotations.SerializedName;

/**
 * Модель Node (Вершина графа или точка маршрута. Нода включает в себя Beacon).
 * Запрашивается с сервера в составе локации  {@link GraphResponse}
 *
 * @see GraphResponse Сетевая модель локации
 * @see BeaconResponse Сетевая модель маячка (Beacon)
 * @see EdgeResponse Сетевая модель ребра
 *
 * @author Dmitry Abakumov <killerinshadow2@gmail.com>
 */
public class NodeResponse {

    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;

    @SerializedName("coordinate_x")
    private int x;
    @SerializedName("coordinate_y")
    private int y;

    /**
     * Текст который будет произносится
     */
    @SerializedName("text")
    private String events;

    /**
     * Связанный с данной нодой Beacon
     */
    @SerializedName("beacon")
    private BeaconResponse beacon;

    /**
     * Текст который проговаривается в режиме "прогулки"
     */
    @SerializedName("text_broadcast")
    private String broadcastText;

    /**
     * Можно ли выбрать данную ноду как конечную (будет ли нода отображаться в списке)
     */
    @SerializedName("is_destination")
    private boolean isDestination;

    /**
     * Фантомная метка или нет
     */
    @SerializedName("is_phantom")
    private boolean isPhantom;

    /**
     * При проходе через данную ноду, нужно ли произносить направление или нет
     */
    @SerializedName("is_turns_verbose")
    private boolean isTurnsVerbose;


    public String getMac() {
        return beacon.getMac();
    }
    public void setMac(String mac) {
         beacon.setMac(mac);
    }
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getEvents() {
        return events;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BeaconResponse getBeacon() {
        return beacon;
    }

    public String getBroadcastText() {
        return broadcastText;
    }

    public boolean isDestination() {
        return isDestination;
    }

    public boolean isPhantom() {
        return isPhantom;
    }
    public boolean isTurnsVerbose() {
        return isTurnsVerbose;
    }


}
