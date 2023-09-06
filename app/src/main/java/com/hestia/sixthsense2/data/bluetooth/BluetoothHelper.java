package com.hestia.sixthsense2.data.bluetooth;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.hestia.sixthsense2.data.bluetooth.model.Permissions;

/**
 * Интерфейс, который реализуется в классе AppBluetoothHelper
 * Цель та же - проверка и запрос разришений Bluetooth
 *
 * @author     Dmitry Abakumov <killerinshadow2@gmail.com>
 */
public interface BluetoothHelper {

    /**
     * {@link Permissions#checkBluetoothFeatures()}
     */
    boolean checkBluetoothFeatures();

    /**
     * {@link Permissions#checkBluetoothPermissions()}
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    boolean checkBluetoothPermissions();

    /**
     * {@link Permissions#requestPermissions(Activity)}
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    void requestPermissions(Activity activity);

}
