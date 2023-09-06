package com.hestia.sixthsense2.data.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.LocationManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.hestia.sixthsense2.data.bluetooth.model.Permissions;
import com.hestia.sixthsense2.ui.main.MainActivity;

/**
 * Вспомогательный класс для проверки и запроса разрешений Bluetooth
 *
 * @see Permissions
 * @author     Dmitry Abakumov <killerinshadow2@gmail.com>
 */
public class AppBluetoothHelper implements BluetoothHelper {
    public static final int GPS_RESOLUTION_REQUEST = 99;

    private Context mContext;

    /**
     * {@link Permissions}
     */
    private Permissions mPermissions;


    public AppBluetoothHelper(Context context) {
        mContext = context;

        // Permissions
        mPermissions = new Permissions(context);
    }

    /**
     * {@link Permissions#checkBluetoothFeatures()}
     */
    public boolean checkBluetoothFeatures() {
        return mPermissions.checkBluetoothFeatures();
    }

    /**
     * {@link Permissions#checkBluetoothPermissions()}
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean checkBluetoothPermissions() {
        return mPermissions.checkBluetoothPermissions();
    }

    /**
     * {@link Permissions#requestPermissions(Activity)}
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestPermissions(Activity activity) {
        mPermissions.requestPermissions(activity);
    }


    public boolean checkGeolocationEnabled(Context context)
    {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * (НЕ ИСПОЛЬЗУЕТСЯ)
     */
    public void openSettingsMenu(Activity activity){
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Геолокация не включена.Приложение может работать не корректно.Открыть меню настроек для включения геолокации?")
                .setCancelable(false)
                .setPositiveButton("Да", (dialog, id) -> {
                    activity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                })
                .setNegativeButton("Нет", (dialog, id) -> activity.finish());


        final AlertDialog alert = builder.create();
        alert.show();
        alert.getWindow().setBackgroundDrawableResource(android.R.color.holo_red_dark);
    }

    /**
     * Включение сервиса геолокации
     */
    public void enableGeolocation(MainActivity mainActivity) {
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(10*1000)
                    .setFastestInterval(2*1000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);

            LocationServices
                    .getSettingsClient(mainActivity)
                    .checkLocationSettings(builder.build())
                    .addOnSuccessListener(mainActivity, (LocationSettingsResponse response) -> {
                        Toast.makeText(mainActivity,"OK",Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(mainActivity, ex -> {
                        try {
                            ResolvableApiException rae = (ResolvableApiException) ex;
                            rae.startResolutionForResult(mainActivity, GPS_RESOLUTION_REQUEST);

                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    });
    }
}


/*

try {
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult((Activity) context, AppConstants.GPS_REQUEST);
                                    } catch (IntentSender.SendIntentException sie) {
                                        Log.i(TAG, "PendingIntent unable to execute request.");
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    String errorMessage = "Location settings are inadequate, and cannot be " +
                                            "fixed here. Fix in Settings.";
                                    Log.e(TAG, errorMessage);
Toast.makeText((Activity) context, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }


 */
