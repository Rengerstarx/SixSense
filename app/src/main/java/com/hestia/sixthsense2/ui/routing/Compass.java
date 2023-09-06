package com.hestia.sixthsense2.ui.routing;

import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Класс, в котором происходит основная работа компаса,
 * нужного для поворота пользователя к первой метке маршрута
 *
 * Используется в {@link RoutingActivity}
 */
public class Compass implements SensorEventListener {

    private SensorManager sensorManager;
    private int azimuth = 0;
    private Context context;
    private Sensor rotationVector;
    private float[] matrixRotate = new float[9];
    private float[] resultVal = new float[3];

    public Compass(Context context) {
        this.context = context;
        foo();
    }

    private void foo(){
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void resume(){
        if (rotationVector != null)
            sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI);
    }

    public void pause(){
        sensorManager.unregisterListener(this);
    }

    public int getAzimuth() {
        return azimuth;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(matrixRotate, event.values.clone());
            SensorManager.remapCoordinateSystem(matrixRotate, SensorManager.AXIS_X, SensorManager.AXIS_Y, matrixRotate);
            SensorManager.getOrientation(matrixRotate, resultVal);

            azimuth = (int) Math.round(Math.toDegrees(resultVal[0]));
            if (azimuth < 0) azimuth += 360;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
