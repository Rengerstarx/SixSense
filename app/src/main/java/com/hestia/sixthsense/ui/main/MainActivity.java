package com.hestia.sixthsense.ui.main;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.crashlytics.android.Crashlytics;
import com.hestia.sixthsense.R;
import com.hestia.sixthsense.data.bluetooth.AppBluetoothHelper;
import com.hestia.sixthsense.ui.base.BaseActivity;
import com.hestia.sixthsense.ui.routing.RoutingActivity;
import com.hestia.sixthsense.utils.AppConstants;

import org.altbeacon.beacon.BeaconManager;

import static com.hestia.sixthsense.data.bluetooth.AppBluetoothHelper.GPS_RESOLUTION_REQUEST;
import static com.hestia.sixthsense.data.bluetooth.model.Permissions.PERMISSION_REQUEST_COARSE_BL;
import static com.hestia.sixthsense.data.bluetooth.model.Permissions.PERMISSION_REQUEST_COARSE_LOCATION;

/**
 * Главный экран (Activity), из которого можно перейти на другие экраны: "Инструкция" "Навигация" "Настройки" "О нас"
 *
 * @see AboutUsActivity Экран "О нас"
 * @see RoutingActivity Экран "Навигация"
 * @see com.hestia.sixthsense.ui.route.RouteActivity Экран "Ведение по маршруту"
 * @see SettingsActivity Экран "Настройки"
 *
 * @author Dmitry Abakumov <killerinshadow2@gmail.com>
 */
public class MainActivity extends BaseActivity implements TextToSpeech.OnInitListener {

    /**
     * Logger TAG
     */
    public static final String TAG = "MainActivity";

    private AppBluetoothHelper mAppBluetoothHelper;

    /**
     * Используется для beacon manager
     */
    private static final int NOTIFICATION_ID = 666;

    /**
     * Используется для построения канала уведомлений
     */
    private static final String NOTIFICATION_CHANNEL_ID = "main_notification_channel";

    private BeaconManager mBeaconManager;

    private BluetoothAdapter mBluetoothAdapter;

    // Buttons
    private Button mInstructionButton;
    private Button mFindRouteButton;
    private Button mOpenSettings;
    private Button mOpenAboutUs;
    private int instructionIndex = 0;
    private String[] instructions;
    private TextToSpeech textToSpeech;
    private static final int PERMISSION_REQUEST_ENABLE_BLUETOOTH = 5;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 6;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 8;
    Context context = this; // Текущий контекст
    Activity activity = this; // Текущая активность
    SharedPreferences sharedPreferences;
    boolean isFirstLaunch;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true);

        // Service button
        mInstructionButton = findViewById(R.id.main_instruction_button);

        mBeaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
        mBeaconManager.setEnableScheduledScanJobs(false);
        mAppBluetoothHelper = new AppBluetoothHelper(getApplicationContext());
        //-------------------------------------------------------------------------------------------------//
        findViewById(R.id.main_about_us).setOnTouchListener(new View.OnTouchListener() {
            long startTime = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        long totalTime = System.currentTimeMillis() - startTime;
                        long totalSeconds = totalTime / 1000;
                        if (totalSeconds >= 1) {
                            OpenSettings();
                        } else {
                            OpenAboutUs();
                        }
                        break;
                }
                return true;
            }
        });
        //-------------------------------------------------------------------------------------------------//
        mFindRouteButton = findViewById(R.id.main_route_button);
        mFindRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RoutingActivity.class));
            }
        });
        textToSpeech = new TextToSpeech(this, this);
        InitBluetooth();
        if (isFirstLaunch) {
            OpenSettings();
        }else{}
    }

    private void OnBLE(){
        if (!mBluetoothAdapter.isEnabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle); // Use a custom theme
            builder.setTitle("Включить Bluetooth?");
            builder.setMessage("В данный момент Bluetooth отключен. Вы хотите включить его?");
            builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.S)
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Пользователь выбрал "Да", выполните действия для включения Bluetooth
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (bluetoothAdapter != null) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                                == PackageManager.PERMISSION_GRANTED) {
                            // Разрешение на управление Bluetooth уже предоставлено
                            bluetoothAdapter.enable();
                        } else {
                            // Запрос разрешения на управление Bluetooth
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                    PERMISSION_REQUEST_ENABLE_BLUETOOTH);
                        }
                    }
                }
            });
            builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Пользователь выбрал "Нет", выполните действия, если необходимо
                    finish();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            // Запрашиваем разрешение у пользователя
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    REQUEST_BLUETOOTH_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION);
        }

    }

    /**
     * Инициализация Bluetooth на телефоне (проверка совместимости, включение)
     */
    private void InitBluetooth() {
        // Проверка функций bluetooth
        if (!_checkBluetoothFeatures()) {
            finish();
        }

        // Проверка bluetooth разрешений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !_checkBluetoothPermissions()) {
            _requestBluetoothPermissions();

        }

        /*
          Используйте службу основной системы для поиска маяков.
          Это открывает возможность постоянного сканирования
          в течение длительных периодов времени в фоновом режиме
          на ОС Android 8 + в обмен на отображение значка в верхней части экрана
          и постоянного уведомления о том, что приложение использует ресурсы в фоновом режиме.
         */
//        _createForegroundService();
        BeaconManager.setDebug(AppConstants.BEACON_DEBUG_MODE);
        if (!mAppBluetoothHelper.checkGeolocationEnabled(getApplicationContext())) {

            // TODO open settings menu
            // Toast.makeText(getBaseContext(),"No GEO",Toast.LENGTH_LONG).show();
            //mAppBluetoothHelper.openSettingsMenu(this);
            mAppBluetoothHelper.enableGeolocation(this);
        }
        _enableBluetooth();


    }

    /**
     * Открытие окна меню настроек приложения
     * @see SettingsActivity
     */
    private void OpenSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivityForResult(intent, 0);
    }

    /**
     * Открытие окна "О нас"
     * @see AboutUsActivity
     */
    private void OpenAboutUs() {
        Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivityForResult(intent, 0);
    }

    /**
     * Проверка функций bluetooth
     *
     * @return Статус проверки
     */
    protected boolean _checkBluetoothFeatures() {

        // Логгирование
        Log.d(TAG, "Checking bluetooth features");
        Crashlytics.log(Log.DEBUG, TAG, "Checking bluetooth features");

        // Проверка
        if (!mAppBluetoothHelper.checkBluetoothFeatures()) {
            Log.e(TAG, "Bluetooth or Bluetooth Low Energy is not supported on this device");
            Crashlytics.log(Log.ERROR, TAG,
                    "Bluetooth or Bluetooth Low Energy is not supported on this device");

            toast(R.string.main_bluetooth_features_not_supported, null);
            return false;
        }

        return true;
    }

    /**
     * Проверка разрешений Bluetooth
     *
     * @return Статус проверки разрешений
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected boolean _checkBluetoothPermissions() {
        // Checking permissions
        Log.d(TAG, "Checking bluetooth permissions");
        Crashlytics.log(Log.DEBUG, TAG, "Checking bluetooth permissions");

        // Check bluetooth permissions
        if (!mAppBluetoothHelper.checkBluetoothPermissions()) {
            Log.e(TAG, "Bluetooth permissions is not granted");
            Crashlytics.log(Log.ERROR, TAG,
                    "Bluetooth permissions is not granted");

            return false;
        }

        return true;
    }

    /**
     * Запрос разрешения на использование Bluetooth
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void _requestBluetoothPermissions() {
        // Request permissions
        Log.d(TAG, "Request bluetooth permissions");
        Crashlytics.log(Log.DEBUG, TAG, "Request bluetooth permissions");

        mAppBluetoothHelper.requestPermissions(this);

    }

    /**
     * On request permission result
     *
     * Проверка Bluetooth разрешений
     *
     * @param requestCode  request code
     * @param permissions  Список разрешений
     * @param grantResults Список предоставленных разрешений
     *
     */
    private static final int REQUEST_ENABLE_BLUETOOTH = 5;

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение на управление Bluetooth получено, можно включить Bluetooth
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                bluetoothAdapter.enable();
            } else {
                // Разрешение на управление Bluetooth не предоставлено, обработайте это соответствующим образом
            }
        }
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {}
        }
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

            }
        }
        switch (requestCode) {
            // Check coarse location permission
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                int grantedCount = 0;
                for (int result : grantResults) {
                    grantedCount += result == PackageManager.PERMISSION_GRANTED ? 1 : 0;
                }
                if (grantedCount != grantResults.length)
                    finish();
                break;
            }

        }
    }

    /**
     * Создание сервиса сканирования маячков в фоновом режиме
     * Устанавливаются настройки сканирования и уведомление о сканировании
     *
     * @see NotificationChannel
     */
    protected void _createForegroundService() {

        // Build notification
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(translate(R.string.main_notification_content_title));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    translate(R.string.main_notification_content_title),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(translate(R.string.main_notification_content_description));
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }
        mBeaconManager.enableForegroundServiceScanning(builder.build(), NOTIFICATION_ID);
        mBeaconManager.setEnableScheduledScanJobs(false);

        // Set zero for
        mBeaconManager.setBackgroundBetweenScanPeriod(0);
        mBeaconManager.setBackgroundScanPeriod(AppConstants.BLUETOOTH_BACKGROUND_SCAN_PERIOD);
    }

    /**
     * Включение Bluetooth
     */
    protected void _enableBluetooth() {

        //Если поддерживается BLE, получается адаптер BT. Подготовка к использованию!
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Если адаптер возвращает ошибку, приложение закрывается с сообщением об ошибке
        if (mBluetoothAdapter == null) {
            toast(R.string.main_bluetooth_adapter_error, null);
            finish();
        } else {
            OnBLE();
        }
    }


    /**
     * On activity result
     *
     * @param requestCode activity request code
     * @param resultCode  activity result code
     * @param data        intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Проверьте, получен ли ответ от BT
        if(requestCode == PERMISSION_REQUEST_COARSE_BL){
            // User chose not to enable Bluetooth.
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
                return;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
        if(requestCode ==  GPS_RESOLUTION_REQUEST)
        {
            if(resultCode != Activity.RESULT_OK)
            {
                finish();
                return;
            }
        }
    }
    /**
     * Инициализация инструкции приложения
     * Установка прослушивателя для кнопки
     * Получение текста инструкции из R.array.instruction_array
     * @param i - status
     */
    @Override
    public void onInit(int i) {
        instructions = getResources().getStringArray(R.array.instruction_array);
        mInstructionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.speak(instructions[instructionIndex],TextToSpeech.QUEUE_FLUSH,null,"");
                instructionIndex = (instructionIndex+1)%instructions.length;

            }
        });
    }

    @Override
    protected void onPause() {
        textToSpeech.stop();
        super.onPause();
    }
}
