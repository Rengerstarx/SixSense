package com.hestia.sixthsense.data.bluetooth.model;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

/**
 * Класс, где описываются методы проверки разрешений, нужных в приложении и запроса оных
 *
 * <p></p>
 * <p>Для правильной работы приложения нужны следующие разрешения:</p>
 * <p> ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION</p>
 * <p>Также необходима поддержка следующих фукнций:</p>
 * <p> FEATURE_BLUETOOTH, FEATURE_BLUETOOTH_LE</p>
 * <p>Первое - это непосредственно Bluetooth,</p>
 * <p> второе - Bluetooth Low Energy(BLE), </p>
 * <p> технология на которой и основаны маячки, с помощью которых осуществляется навигации в приложении</p>
 *
 *
 * @author     Dmitry Abakumov <killerinshadow2@gmail.com>
 */
public class Permissions {

    public static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    public static final int PERMISSION_REQUEST_COARSE_BL = 2;

    private Context mContext;

    public Permissions(Context context) {
        mContext = context;
    }

    /**
     * Проверка поддержки Bluetooth и Bluetooth Low Energy в телефоне
     *
     * @return Статус проверки
     */
    public boolean checkBluetoothFeatures() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) &&
                mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Проверка, предоставляются ли нужные разрешения для приложения
     * Для правильной работы приложения на всех версиях Android, нужны оба разрешения
     * На младших версиях (До 10) нужен только ACCESS_FINE_LOCATION,
     * На старших версих (10 и после) нужно и второе
     *
     * @return статус проверки разрешений в виде boolean
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean checkBluetoothPermissions() {
        return mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                && mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Запрос разрешений для приложения
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestPermissions(Activity activity){
        activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_COARSE_LOCATION);
    }

}
