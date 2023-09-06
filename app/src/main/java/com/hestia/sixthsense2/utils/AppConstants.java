package com.hestia.sixthsense2.utils;

import org.altbeacon.beacon.BeaconManager;

/**
 * Константы (адреса серверов, переменные дебага, некоторые списки дефольных значений)
 *
 * @author     Dmitry Abakumov <killerinshadow2@gmail.com>
 */
public class AppConstants {

    // Application
    /**
     * Debug mode
     */
    public static final boolean DEBUG_MODE = false;
    public static final boolean BEACON_DEBUG_MODE = true;

    // API
    // public static final String API_AUTHORIZATION_HEADER = null;
    // public static final String API_PASSWORD_AUTHENTICATION = "password";
    // public static final String API_TOKEN_AUTHENTICATION = "refresh_token";


    //public static final String API_BASE_URL = "http://192.168.0.106:8000";

    /**
     * Список адресов используемых серверов
     * В данном случае первый основной, второй запасной(НЕ РАБОТАЕТ)
     */
    public static final String[] SERVER_LIST = {"http://feel.sfedu.ru/hestia/","http://51.158.98.60:8002/"};
    public static final String API_BASE_URL = SERVER_LIST[0];

    /**
     * Время ожидания подключения в миллисекундах
     */
    public static final long API_CONNECT_TIMEOUT = 2000;

    // Network
    /**
     * Время ожидания вызова в миллисекундах
     */
    public static final long API_CALL_TIMEOUT = 5;

    /**
     * Тайм-аут чтения в миллисекундах
     */
    public static final long API_READ_TIMEOUT = 5;


    /**
     * Время ожидания записи в миллисекундах
     */
    public static final long API_WRITE_TIMEOUT = 5;

    // Synchronization
    /**
     * Минимальная задержка планировщика для работы
     */
    public static final int SCHEDULER_MINIMUM_LATENCY = 1000;

    /**
     * Срок переопределения планировщика
     */
    public static final int SCHEDULER_OVERRIDE_DEAD_LINE = 5000;

    /**
     * Период сканирования планировщика
     */
    public static final int SCHEDULER_SCAN_PERIOD = 60 * 60 * 1000;

    // Bluetooth
    /**
     * Период фонового сканирования Bluetooth
     */
    public static final long BLUETOOTH_BACKGROUND_SCAN_PERIOD = 100;


    /**
     * Стандартные настройки для {@link com.hestia.sixthsense2.ui.route.RouteActivity}
     */

    public static final String SETTINGS_NAME = "SETTINGS";

    /**
     * Размер буффера отсканированных маяков, по умолчанию равен 7
     */
    public static final int DISTANCE_BUFFER_SIZE = 7;
    public static final int BROADCAST_DISTANCE_BUFFER_SIZE = 12;

    /**
     * Стандартное значение, которым заполняется буфер маяков, когда расстояния еще не определены
     */
    public static final int DISTANCE_BUFFER_DEFAULT_VALUE = 1;
    public static final float MIN_UPDATE_DISTANCE = 600;
    public static final float AVERAGE_DISTANCE_DELTA = 10f;

    /**
     * Продолжительность в миллисекундах каждого цикла сканирования Bluetooth LE для поиска новых маяков.
     *
     * @see BeaconManager#setForegroundScanPeriod(long)
     */
    public static final int UPDATE_TIME = 500;

    public static final String PHANTOM_NAME = "PHANTOM";
    public static final String PHANTOM_DELIMETR = "#";

    public static final int DISTANCE_OF_ONE_METER = -81;


}