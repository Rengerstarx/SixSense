package com.hestia.sixthsense.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.hestia.sixthsense.R;
import com.hestia.sixthsense.ui.routing.RoutingActivity;
import com.hestia.sixthsense.utils.AppConstants;


/**
 * Экран (Activity), в котором происходит настройка приложения
 *
 * @see MainActivity Главная активность
 * @see AboutUsActivity Экран "О нас"
 * @see RoutingActivity Экран "Навигация"
 * @see com.hestia.sixthsense.ui.route.RouteActivity Экран "Ведение по маршруту"
 *
 * @author Dmitry Abakumov <killerinshadow2@gmail.com>
 */
public class SettingsActivity extends AppCompatActivity {
    public static final String SETTINGS_UPDATE_TIME = "update_time";
    public static final String SETTINGS_MIN_DISTANCE = "min_distance";
    public static final String SETTINGS_DISTANCE_OF_ONE_METER = "distance_value";
    public static final String SETTINGS_BUFFER_SIZE = "buffer_size";
    public static final String SETTINGS_BROADCAST_BUFFER_SIZE = "broadcast_buffer_size";
    public static final String SETTINGS_DELTA = "distance_delta";

    /**
     * Минимальный возможный размер буфера маяков com.hestia.sixthsense.ui.route.RouteActivity#beaconBuffer
     * Если настроенное значение буффера меньше минимального, то размер будет равен минимальному
     */
    private int minBufferSize = 3;

    /**
     * Максимальный возможный размер буфера маяков com.hestia.sixthsense.ui.route.RouteActivity#beaconBuffer
     * Если настроенное значение буффера больше максимального, то размер будет равен максимальному
     */
    private int maxBufferSize = 30;
    private LinearLayout mDebugLayout;
    private EditText mMinUpdateDistance;
    private EditText mBufferSize;
    private EditText mDistanceOfOneMeter;
    private EditText mDistanceDelta;
    private EditText mUpdateTime;
    private EditText mDefaultBroadcastBufferValue;
    private Button mButtonSave;
    private Button mButtonRestore;
    private Button mButtonIncrease;
    private Button mButtonDecrease;
    private Button mButtonApplay;
    private float minUpdateDistance = AppConstants.MIN_UPDATE_DISTANCE;
    private int bufferSize = AppConstants.DISTANCE_BUFFER_SIZE;
    private int broadCastBufferSize = AppConstants.BROADCAST_DISTANCE_BUFFER_SIZE;
    private int distanceValue = AppConstants.DISTANCE_OF_ONE_METER;
    private float delta = AppConstants.AVERAGE_DISTANCE_DELTA;
    private int updateTime = AppConstants.UPDATE_TIME;
    SharedPreferences sharedPreferences;
    boolean isFirstLaunch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        InitReference();
        Init();
    }

    /**
     * Привязка View-элементов
     */
    private void InitReference() {
        sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true);
        mDebugLayout = findViewById(R.id.debug_layout);
        mMinUpdateDistance = findViewById(R.id.settings_min_distance);
        mBufferSize = findViewById(R.id.settings_buffer_size);
        mDistanceOfOneMeter = findViewById(R.id.settings_distance_of_one_meter);
        mDistanceDelta = findViewById(R.id.settings_average_delta);
        mButtonSave = findViewById(R.id.settings_button_save);
        mButtonRestore = findViewById(R.id.settings_button_reset);
        mButtonIncrease = findViewById(R.id.settings_increase_button);
        mButtonDecrease = findViewById(R.id.settings_decrease_button);
        mUpdateTime = findViewById(R.id.settings_default_time_update);
        mDefaultBroadcastBufferValue = findViewById(R.id.settings_default_broadcast_buffer_size);
        mButtonApplay = findViewById(R.id.AplaySettings);
        if (isFirstLaunch) {
            findViewById(R.id.AplaySettings).setVisibility(View.VISIBLE);
            findViewById(R.id.linearLayout).setVisibility(View.GONE);
        }else{
            findViewById(R.id.AplaySettings).setVisibility(View.GONE);
            findViewById(R.id.linearLayout).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Первоначальная настройка экрана настроек (полей, кнопок и т.д.)
     */
    private void Init() {
        InitSettings();
        UpdateEditText();
        UpdateVisible();
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {SaveSettings(); finish(); }
        });
        mButtonRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {RestoreSettings();}
        });
        mButtonApplay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                SaveSettings();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isFirstLaunch", false);
                editor.apply();
                finish();
            }
        });
        mButtonIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bufferSize++;
                if(bufferSize < minBufferSize){
                    bufferSize = minBufferSize;
                }
                if(bufferSize > maxBufferSize){
                    bufferSize = maxBufferSize;
                }
                UpdateEditText();
                SaveSettings();
            }
        });
        mButtonDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bufferSize--;
                if(bufferSize < minBufferSize){
                    bufferSize = minBufferSize;
                }
                if(bufferSize > maxBufferSize){
                    bufferSize = maxBufferSize;
                }
                UpdateEditText();
                SaveSettings();
            }
        });
    }

    /**
     * Обновление всез строковых полей экрана
     */
    private void UpdateEditText() {
        mMinUpdateDistance.setText(String.valueOf(minUpdateDistance));
        mBufferSize.setText(String.valueOf(bufferSize));
        mDistanceOfOneMeter.setText(String.valueOf(distanceValue));
        mDistanceDelta.setText(String.valueOf(delta));
        mUpdateTime.setText(String.valueOf(updateTime));
        mDefaultBroadcastBufferValue.setText(String.valueOf(broadCastBufferSize));
    }

    /**
     * Обновление видимости кнопок
     * Все эти кнопки нужны для Debug-режима
     * Если {@link AppConstants#DEBUG_MODE} true, то кнопки появляются,
     * иначе исчезают
     */
    private void UpdateVisible(){
        /*mDebugLayout.setVisibility(AppConstants.DEBUG_MODE?View.VISIBLE:View.GONE);
        mButtonSave.setVisibility(AppConstants.DEBUG_MODE?View.VISIBLE:View.GONE);
        mMinUpdateDistance.setVisibility(AppConstants.DEBUG_MODE?View.VISIBLE:View.GONE);
        mDistanceOfOneMeter.setVisibility(AppConstants.DEBUG_MODE?View.VISIBLE:View.GONE);
        mDistanceDelta.setVisibility(AppConstants.DEBUG_MODE?View.VISIBLE:View.GONE);
        mUpdateTime.setVisibility(AppConstants.DEBUG_MODE?View.VISIBLE:View.GONE);
        mDefaultBroadcastBufferValue.setVisibility(AppConstants.DEBUG_MODE?View.VISIBLE:View.GONE);*/
    }

    /**
     * Парсинг значений, содержащихся в текстовых полях, в соответствующие переменные
     */
    private void GetValues() {
        minUpdateDistance = Float.parseFloat(mMinUpdateDistance.getText().toString());
        bufferSize = Integer.parseInt(mBufferSize.getText().toString());
        distanceValue = Integer.parseInt(mDistanceOfOneMeter.getText().toString());
        delta = Float.parseFloat(mDistanceDelta.getText().toString());
        updateTime = Integer.parseInt(mUpdateTime.getText().toString());
        broadCastBufferSize = Integer.parseInt(mDefaultBroadcastBufferValue.getText().toString());
    }

    /**
     * Сохранение введенных настроек для дальнейшего использования в приложении
     */
    private void SaveSettings() {
        GetValues();
        SharedPreferences preferences = getSharedPreferences(AppConstants.SETTINGS_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(SETTINGS_MIN_DISTANCE,minUpdateDistance);
        editor.putInt(SETTINGS_BUFFER_SIZE,bufferSize);
        editor.putInt(SETTINGS_DISTANCE_OF_ONE_METER,distanceValue);
        editor.putFloat(SETTINGS_DELTA,delta);
        editor.putInt(SETTINGS_UPDATE_TIME,updateTime);
        editor.putInt(SETTINGS_BROADCAST_BUFFER_SIZE,broadCastBufferSize);
        editor.commit();
    }

    /**
     * Инициализация первоначальных настроек
     */
    private void InitSettings() {
        SharedPreferences preferences = getSharedPreferences(AppConstants.SETTINGS_NAME,Context.MODE_PRIVATE);
        minUpdateDistance = preferences.getFloat(SETTINGS_MIN_DISTANCE,AppConstants.MIN_UPDATE_DISTANCE);
        bufferSize = preferences.getInt(SETTINGS_BUFFER_SIZE,AppConstants.DISTANCE_BUFFER_SIZE);
        distanceValue = preferences.getInt(SETTINGS_DISTANCE_OF_ONE_METER,AppConstants.DISTANCE_OF_ONE_METER);
        delta = preferences.getFloat(SETTINGS_DELTA,AppConstants.AVERAGE_DISTANCE_DELTA);
        updateTime = preferences.getInt(SETTINGS_UPDATE_TIME,AppConstants.UPDATE_TIME);
        broadCastBufferSize = preferences.getInt(SETTINGS_BROADCAST_BUFFER_SIZE,AppConstants.BROADCAST_DISTANCE_BUFFER_SIZE);
    }

    /**
     * Восстановить настройки по-умолчанию из {@link AppConstants}
     */
    private void RestoreSettings() {
        minUpdateDistance = AppConstants.MIN_UPDATE_DISTANCE;
        bufferSize = AppConstants.DISTANCE_BUFFER_SIZE;
        distanceValue = AppConstants.DISTANCE_OF_ONE_METER;
        delta = AppConstants.AVERAGE_DISTANCE_DELTA;
        updateTime = AppConstants.UPDATE_TIME;
        broadCastBufferSize = AppConstants.BROADCAST_DISTANCE_BUFFER_SIZE;
        UpdateEditText();
        SaveSettings();
    }

}
