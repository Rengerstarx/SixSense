package com.hestia.sixthsense.data.network.model.beacon;

import com.google.gson.annotations.SerializedName;

/**
 * (НЕ ИСПОЛЬЗУЕТСЯ)
 * Модель координат маячка (Beacon), который храниться на сервере
 *
 * @author     Dmitry Abakumov <killerinshadow2@gmail.com>
 */
public class Coordinate {

    /** @var mX X coordinate of beacon **/
    @SerializedName("x")
    private Integer mX;

    /** @var mY Y coordinate of beacon **/
    @SerializedName("y")
    private Integer mY;

    /**
     * Coordinate constructor
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public Coordinate(Integer x, Integer y) {
        mX = x;
        mY = y;
    }

    /**
     * Get x coordinate
     *
     * @return x coordinate
     */
    public Integer getX() {
        return mX;
    }

    /**
     * Get y coordinate
     *
     * @return y coordinate
     */
    public Integer getY() {
        return mY;
    }

}
