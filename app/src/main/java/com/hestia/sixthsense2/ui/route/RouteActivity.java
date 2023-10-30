package com.hestia.sixthsense2.ui.route;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

import com.google.gson.Gson;
import com.hestia.sixthsense2.R;
import com.hestia.sixthsense2.ui.base.BaseActivity;
import com.hestia.sixthsense2.data.network.model.beacon.GraphResponse;
import com.hestia.sixthsense2.ui.main.AboutUsActivity;
import com.hestia.sixthsense2.ui.main.MainActivity;
import com.hestia.sixthsense2.ui.main.SettingsActivity;
import com.hestia.sixthsense2.ui.routing.RoutingActivity;
import com.hestia.sixthsense2.utils.AppConstants;
import com.hestia.sixthsense2.utils.PathFinder.NetworkConverter;
import com.hestia.sixthsense2.utils.PathFinder.Path.PathItem;
import com.hestia.sixthsense2.utils.PathFinder.PathFinder;
import com.hestia.sixthsense2.utils.TTS;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.ArmaRssiFilter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Экран (Activity), в котором производиться ведение по маршруту, перейти сюда можно после экрана Навигации
 * <ul>
 * <ul>при получении ожидаемой метки:
 *     <li>переход к новой точке маршрута
 *     <li>воспроизведение событий текущей метки
 *     <li>воспроизведение инструкций для поворота
 *     <li>воспроизведений событий ближайшего ребра
 * </ul>
 * <ul>при получении неожиданной метки:
 *     <li>переход к новой точке
 *     <li>воспроизведение событий текущей метки
 *     <li>воспроизведение сообщения "вы заблудились"
 *     <li>перестройка маршрута
 * <ul>
 * при получении конечной метки - завершение
 * </ul>
 *
 * @see MainActivity Главная активность
 * @see RoutingActivity Экран "Навигация"
 * @see SettingsActivity Экран "Настройки"
 * @see AboutUsActivity Экран "О нас"
 * @see PathFinder
 */
public class RouteActivity extends BaseActivity implements BeaconConsumer, TextToSpeech.OnInitListener {

    public final int N = 10;
    private static boolean flag = false;
    // Keys for starting activity
    public static final String KEY_ROUTE = "route";
    public static final String KEY_COMMITTEE = "committee";
    public static final String KEY_EXIT = "exit";
    //
    private static final String NOTIFICATION_CHANNEL_ID = "route_notification_channel";
    private static final int NOTIFICATION_ID = 333;

    private MediaPlayer mMediaPlayer;

    private Map<String, Pair<Integer, MediaPlayer>> mBeacons;
    private Map<String, Pair<Integer, MediaPlayer>> mBeaconsOut;

    private int distanceOfOneMeter = -65;

    private Map<String, Double> beaconDistanceBuffer = new HashMap<>();
    private int mRoute = 0;

    /**
     * Logger TAG
     */
    public static final String TAG = "RouteScreen";

    private BeaconManager mBeaconManager;


    /**
     * Объект из библиотеки altbeacon для работы с маячками
     *
     * @see Region
     */
    private Region region = new Region("AllBeaconsRegion", null, null, null);

    /**
     * {@link PathFinder}
     */
    private PathFinder pathFinder;
    private Button btn;
    private TextView tvDebug;
    private TextView tvDebugBuffer;
    private LinearLayout llDebugLayout;

    //private TextToSpeech textToSpeech;
    private String lastBeaconAddress = "";
    private String penultimateBeaconAddress = "";
    private String curentBeaconAddress = "";
    private String previousBeaconAddress = "";
    private boolean isSpeak = false;
    private TTS textToSpeech;

    /**
     * MAC конечной вершины пути
     */
    private String destinationNode = "nodeD";
    private String nextNode = "";
    ArrayList<PathItem> path;

    private PowerManager powerManager = null;

    /**
     * WakeLock — это механизм, указывающий, что вашему приложению необходимо,
     * чтобы устройство оставалось включенным.
     *
     * @see android.os.PowerManager.WakeLock
     */
    private PowerManager.WakeLock wakeLock = null;

    /**
     * Индекс пути, для отлеживания на какой вершине сейчас находится пользователь,
     * определения следущей и предыдущих вершин
     */
    private int pathIndex = 0;

    /**
     * Инициализированы ли все переменные для работы,
     * если нет то ведение по маршруту производиться не будет.
     * <p>Инициализация происходит в {@link #onCreate(Bundle)}
     *
     * <pre>Переменные, что должны инициализироваться
     *  destinationNode
     *  pathFinder
     */
    private boolean initialized = false;


    /**
     * Буфер, который отслеживает все расстояния до маяков с течением времени
     * <p>Ключ - mac-адрес маяка
     * <p>Значение - кольцевой массив расстояний (В RSSI) до маяка с течением времени.
     * <p>Размер задается {@link #distanceBufferSize}, по умолчанию равен 7
     */
    private Map<String, int[]> beaconBuffer = new HashMap<>();

    /**
     * Индекс для навигации по {@link #beaconBuffer}
     */
    private int distanceBufferIndex = 0;

    /**
     * Размер буффера маяков {@link #beaconBuffer}, по умолчанию равен 7
     */
    private int distanceBufferSize = 7;

    /**
     * Продолжительность в миллисекундах каждого цикла сканирования Bluetooth LE для поиска новых маяков.
     * <p>
     * Устанавливается в {@link #_bindBeaconManager()}
     *
     * @see BeaconManager#setForegroundScanPeriod(long)
     */
    private int updateTime = 900;

    /**
     * Счетчик заполненности кольцевого массива в буффере маяков. {@link #beaconBuffer}
     * Нужен для отслеживания первоначального наполнения расстояний до маяков
     */
    private int bufferFillPasses = 0;

    // Filter Parameters
    private int defaultDistance = 1;

    /**
     * Расстояние при обновлении записи в буфере маяка (метры)
     */
    private float minUpdateDistance = 5;

    /**
     * Расстояние, на которое должен быть ближе новый маяк (в метрах).
     * [averageDelta] * DistanceBufferSize означает, что среднее значение должно быть больше на
     * [averageDelta] [averageDelta] не должно быть равно defaultDistance, иначе маяк будет неизменяемым.
     */
    private int defaultBufferValue = 1;
    private double distanceDelta;

    /**
     * Проверка, достиг ли пользователь последней вершины на пути (пункта назначения)
     */
    private boolean isRouteEnd = false;
    private String outputText = "";

    /**
     * Этот интерфейс реализуется классами, которые получают уведомления о ранжировании маяка.
     * <p>
     * Ранжирование возвращает список маяков в пределах досягаемости вместе
     * с предполагаемой близостью к каждому из них.
     * <p>
     * В данной реализации происходит нахождение маячка, который ближе к пользователю и заполнение {@link #beaconBuffer}
     * <p>
     * https://community.estimote.com/hc/en-us/articles/203356607-What-are-region-Monitoring-and-Ranging-#:~:text=While%20Monitoring%20enables%20you%20to,proximity%20to%20each%20of%20them.
     * <p>
     * https://altbeacon.github.io/android-beacon-library/javadoc/org/altbeacon/beacon/RangeNotifier.html
     *
     * @see RangeNotifier
     */
    RangeNotifier mRangeNotifier = new RangeNotifier() {
        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {

            if (AppConstants.DEBUG_MODE) {
                Log.i(TAG, "Found " + String.valueOf(collection.size()));
                updateDistances(collection);
                displayBeaconBuffer();
            }

            fillBeaconBuffer(collection);
            // Нахождение маячка, который ближе к пользователю
            if (initialized) {
                // Заполение beaconBuffer
                /*if(bufferFillPasses < distanceBufferSize){

                    fillBeaconBuffer(collection);
                    bufferFillPasses++;
                    return;
                }*/
                if (isRouteEnd == false) {
                    Collection<Beacon> filteredByGraph = filterByGraph(collection);
                    String strongestBeacon = getStrongestBeacon(filteredByGraph);
                    if (strongestBeacon.length() == 0)
                        return;
                    if (!strongestBeacon.equals(lastBeaconAddress)) {
                        onNewBeaconDetected(strongestBeacon);
                    }
                    if (!lastBeaconAddress.equals(strongestBeacon)) {
                        curentBeaconAddress = strongestBeacon;
                        if (!isSpeak) penultimateBeaconAddress = lastBeaconAddress;
                        lastBeaconAddress = curentBeaconAddress;
                    }
                }
            }
        }
    };


    /**
     * Показать все маяки, которые нашла библиотека
     * Работает только в {@link AppConstants#DEBUG_MODE} == true
     *
     * @param beacons Коллекция всех маяков для отображения
     */
    void updateDistances(Collection<Beacon> beacons) {

        int i = 0;
        String distances = String.format("Проходы %d /%d\n", bufferFillPasses, distanceBufferSize);
        String mark;
        for (Beacon beacon : beacons) {
            if (lastBeaconAddress.equals(beacon.getBluetoothAddress().substring(6)))
                mark = "-->#";
            else
                mark = "#";
            distances += mark + i + " " + beacon.getBluetoothAddress().substring(6) + " = " + String.valueOf(beacon.getRssi()) + "\n";
            i++;
        }
        distances += "Найдено " + beacons.size();
        tvDebug.setText(distances);
    }


    /**
     * Вывести буффер маяков
     * Работает только в {@link AppConstants#DEBUG_MODE} == true
     */
    void displayBeaconBuffer() {

        int j = 0;
        String mark;
        String debugBufferString = "";
        for (Map.Entry<String, int[]> beaconEntry : beaconBuffer.entrySet()) {
            int[] signalStrenth = beaconEntry.getValue();
            String mac = beaconEntry.getKey();
            if (lastBeaconAddress != null && lastBeaconAddress.equals(mac))
                mark = "*#";
            else
                mark = "#";
            String dst = "";
            int total = 0; // doesn't count negative values
            int validSiganlsNum = 0; //count of non negative values;
            for (int i = 0; i < distanceBufferSize; i++) {
                boolean isSelectedIndex = i == (distanceBufferIndex % distanceBufferSize);
                dst += (isSelectedIndex ? "*" : "") + String.format("%d", signalStrenth[i]) + ", ";
                if (signalStrenth[i] <= 0) {
                    total += signalStrenth[i];
                    validSiganlsNum++;
                }
            }
            if (validSiganlsNum == 0)          // total/validSiganlsNum; validSiganlsNum should not equal 0
                debugBufferString += mark + String.valueOf(j) + " " + mac + " : " + "[" + dst + "] = --\n";        // mac :[1,2,3,4] = total
            else
                debugBufferString += mark + String.valueOf(j) + " " + mac + " : " + "[" + dst + "] = " + String.format("%.2f", (double) total / validSiganlsNum) + "\n";        // mac :[1,2,3,4] = total
            j++;
        }
        // Calculate last beacon distance buffer and log decision value
        if (lastBeaconAddress.length() != 0) {
            double lastBeaconDistances = 0;
            int validDistances = 0;
            int[] lastBeaconBuffer = beaconBuffer.get(lastBeaconAddress);
            for (int i = 0; i < distanceBufferSize; i++) {
                if (lastBeaconBuffer[i] <= 0) {
                    lastBeaconDistances += lastBeaconBuffer[i];
                    validDistances++;
                }
            }
            //debugBufferString += "Новая метка должна быть ближе чем " + String.format("%.2f",(lastBeaconDistances/validDistances) + distanceDelta);
            debugBufferString += "Новая метка должна быть ближе чем " + distanceOfOneMeter;

        }
        tvDebugBuffer.setText(debugBufferString);
    }

    /**
     * Первоначальное заполнение буффера маяков {@link #beaconBuffer} из массива, пока bufferFillPasses < distanceBufferSize
     * Вызывается метод в callback-методе didRangeBeaconsInRegion, что в свою очередь часть интерфейса
     * {@link RangeNotifier} раз в секунду. Переданный массив маяков до маяков является массивом всех
     * доступных маяков на данный момент для устройства.
     *
     * <p>Все элементы массива по умолчанию заполняются значением {@link #defaultBufferValue}, то есть 1
     *
     * @param beacons список маяков, которыми будет заполняться буффер
     */
    void fillBeaconBuffer(Collection<Beacon> beacons) {
        for (Beacon beacon : beacons) {
            String mac = beacon.getBluetoothAddress().substring(6);
            if (!beaconBuffer.containsKey(mac) && pathFinder.isContainNode(mac)) {
                int[] distance = new int[distanceBufferSize];
                Arrays.fill(distance, defaultBufferValue);                     // 50 m?
                beaconBuffer.put(mac, distance);
            }
        }
        for (Map.Entry<String, int[]> entry : beaconBuffer.entrySet()) {
            int[] distance = entry.getValue();
            distance[distanceBufferIndex % distanceBufferSize] = defaultBufferValue;
        }

        for (Beacon beacon : beacons) {
            String mac = beacon.getBluetoothAddress().substring(6);
            int[] buffer = beaconBuffer.get(mac);
            if (buffer != null) {
                //if (beacon.getDistance() < minUpdateDistance)
                buffer[distanceBufferIndex % distanceBufferSize] = beacon.getRssi();
            }
        }
        distanceBufferIndex++;
    }

    /**
     * Получение ближайшего (или самого сильного) маяка из коллекции, переданной в качестве параметра,
     * самый сильный означает, что уровень rssi выше, чем у других маяков
     * <p> Также, происходит заполнение буффера маяками, а у уже имеющихся маяков обновляюся расстояния
     * Заполение происходит у всех маяков по одному индексу {@link #distanceBufferIndex}.
     * То есть у некоторых маяков оно может начаться не с 0 индекса, но буфер кольцевой, поэтому не страшно.
     * <p></p>
     * Нахождение сильнейшей метки происходит путем сложения сложения всех расстояний у каждого маяка
     *
     * @param beacons коллекция маяков, в которой будет производиться поиск самого сильного маяка
     * @return mac-адрес самого сильного маяка
     * если весь буфер был заполнен значением defaultBufferValue, то самый сильный будет пустым, а затем вернет последний
     */
    String getStrongestBeacon(Collection<Beacon> beacons) {

        Log.d("TTTTTT", String.valueOf(distanceOfOneMeter));

        /*for (Beacon beacon : beacons) {
            String mac = beacon.getBluetoothAddress().substring(6);
            if (!beaconBuffer.containsKey(mac)) {
                int[] distance = new int[distanceBufferSize];
                Arrays.fill(distance, defaultBufferValue);
                beaconBuffer.put(mac, distance);
            }
        }
        for (Map.Entry<String, int[]> entry : beaconBuffer.entrySet()) {
            int[] distance = entry.getValue();
            distance[distanceBufferIndex % distanceBufferSize] = defaultBufferValue;
        }*/

        for (Beacon beacon : beacons) {
            String mac = beacon.getBluetoothAddress().substring(6);
            int[] buffer = beaconBuffer.get(mac);
            if (buffer != null) {
                //if (beacon.getDistance() < minUpdateDistance)
                buffer[distanceBufferIndex % distanceBufferSize] = beacon.getRssi();
            }

        }
        distanceBufferIndex++;


        double strongestBeaconDistance = Integer.MIN_VALUE;
        String strongestBeaconMac = "";

        for (Map.Entry<String, int[]> entry : beaconBuffer.entrySet()) {
            String mac = entry.getKey();
            int[] distanceArray = entry.getValue();
            double currentDistance = 0;
            int validCount = 0;
            for (double distance : distanceArray) {
                if (distance <= 0) {
                    currentDistance += distance;
                    validCount++;
                }
            }
            if (validCount == 0)
                continue;
            currentDistance = currentDistance / validCount;       // take an average
            if (currentDistance > strongestBeaconDistance) {
                strongestBeaconDistance = currentDistance;
                strongestBeaconMac = mac;
            }
        }
        // На первой итерации lastBeaconAddress пуст.
        if (lastBeaconAddress.length() == 0) {

            // если весь буфер был заполнен -1 самым сильным будет ничего
            if (strongestBeaconDistance > distanceOfOneMeter)
                if (strongestBeaconMac.length() != 0)
                    return strongestBeaconMac;
                else return "";

            if (!flag) {
                outputText = "Метка не обнаруженна, поэтому подождите пока происходит обнаружение следующей метки";
                textToSpeech.speak(outputText);

                flag = !flag;
            }
            return "";
        }

        // если весь буфер был заполнен значением defaultBufferValue, то самый сильный будет пустым,
        // а затем вернет последний сильнейший адрес
        if (strongestBeaconMac.length() == 0) {
            return lastBeaconAddress;
        }
        // На следующих итерациях найти наиболее ближайшие
        double lastBeaconDistances = 0;
        int validCount = 0;
        for (int rsii : beaconBuffer.get(lastBeaconAddress)) {
            if (rsii <= 0) {
                lastBeaconDistances += rsii;
                validCount++;
            }
        }

        if (validCount == 0)                                 // если validCount == 0, это означает, что буфер пуст, и сигнал должен быть минимально возможным значением
            lastBeaconDistances = Integer.MIN_VALUE;
        else
            lastBeaconDistances = lastBeaconDistances / validCount;

        if (strongestBeaconDistance > distanceOfOneMeter/*lastBeaconDistances + distanceDelta*/) {
            return strongestBeaconMac;
        } else
            return lastBeaconAddress;
    }

    /**
     * Фильтрация маяков по Пути. Все узлы, которые не на пути, следует игнорировать
     *
     * @param beacons Список, где будет производится фильтр
     * @return Отобранные маячки
     * @see PathFinder#getNeighborsMac(String)
     */
    private Collection<Beacon> filterByGraph(Collection<Beacon> beacons) {
        // Фильтрация маяков по Пути. Все узлы, которые не на пути, следует игнорировать
        Collection<Beacon> filteredBeacons = new ArrayList<>();

        if (lastBeaconAddress.length() != 0) {
            // Фильтрация маяков по пути
            ArrayList<String> neighbors = pathFinder.getNeighborsMac(lastBeaconAddress);
            neighbors.add(lastBeaconAddress); // add last node
            for (Beacon beacon : beacons) {
                for (String mac : neighbors) {
                    if (beacon.getBluetoothAddress().substring(6).equals(mac)) {
                        filteredBeacons.add(beacon);
                        break;
                    }
                }
            }
        } else {
            for (Beacon beacon : beacons) {
                if (pathFinder.isContainNode(beacon.getBluetoothAddress().substring(6))) {
                    filteredBeacons.add(beacon);
                }
            }
        }
        return filteredBeacons;

    }

    /**
     * Вызывается, когда сильнейший маяк не равен последнему сильнейшему маяку,
     * То есть пользователь ушел от старого маяка и перешел к новому.
     * <p></p>
     * В методе переприсваивается текущая вершина (становиться та, в которую пользователь пришел),
     * А также следующая вершина {@link #nextNode}, куда пользователь должен прийти следуя по пути, что
     * возвращает {@link PathFinder#Dijkstra(String, String, String)}
     * <p></p>
     * Если пользователь ушел с построенного пути (Приблизился к неожидаемой метке, не из возвращенного списка),
     * То происходит перестройка пути.
     * <p> Вызывается в {@link #mRangeNotifier}
     *
     * @param mostNearBeaconMac mac-адрес сильнейшего (ближайшего к пользователю) маяка
     * @see PathFinder
     */


    private void onNewBeaconDetected(String mostNearBeaconMac) {
        try {
            previousBeaconAddress = penultimateBeaconAddress;
            String whereAmI = mostNearBeaconMac;
            if (whereAmI.equals(destinationNode)) {
                // Если пользователь находиться у последнего маяка в пути (пункте назначения)
                String s = "Вы пришли. ";
                //s+= path.get(pathIndex).getRouteText();
                textToSpeech.speak(s);
                isRouteEnd = true;
                return;
            }
            if (whereAmI.equals(nextNode)) {
                // Все как и должно быть, пользователь подошел к вершине из списка,
                // что возвращает PathFinder#Dijkstra
                outputText = path.get(pathIndex).getRouteText();

                /*textToSpeech.speak(outputText, TextToSpeech.QUEUE_FLUSH, null, "");
                textToSpeech.wait(N);*/

                nextNode = path.get(++pathIndex).node.mac;
                while (nextNode.split(AppConstants.PHANTOM_DELIMETR)[0].equals(AppConstants.PHANTOM_NAME)) {
                    //penultimateBeaconAddress = lastBeaconAddress;
                    lastBeaconAddress = nextNode;
                    outputText += " " + path.get(pathIndex).getRouteText();

                    if (!nextNode.equals(destinationNode)) {
                        pathIndex++;
                        nextNode = path.get(pathIndex).node.mac;
                    } else {
                        outputText += "И вы пришли";
                        isRouteEnd = true;
                        textToSpeech.speak(outputText);
                        return;
                    }
                }
                textToSpeech.speak(outputText);
                /*
                 curentPath.speach();
                 Going next
                 Convert text and speach it
                 Change here nextNode
               */
            } else {
                //Если следующая вершина пуста (еще не инициализирована), происходит в начале),
                //Происходит ее инициализация
                if (nextNode.isEmpty()) {
                    Log.i(TAG, "NextNode is not initialized");
                    path = pathFinder.Dijkstra(whereAmI, destinationNode, "");
                    PathItem curentPath = path.get(pathIndex);
                    outputText = path.get(pathIndex).getRouteText();


                    nextNode = path.get(++pathIndex).node.mac;
                    while (nextNode.split(AppConstants.PHANTOM_DELIMETR)[0].equals(AppConstants.PHANTOM_NAME)) {
                        //penultimateBeaconAddress = lastBeaconAddress;
                        lastBeaconAddress = nextNode;

                        outputText += " " + path.get(pathIndex).getRouteText();
                        /*textToSpeech.speak(path.get(pathIndex).getRouteText(), TextToSpeech.QUEUE_FLUSH, null, "");
                        textToSpeech.wait(N);*/
                        if (!nextNode.equals(destinationNode)) {
                            pathIndex++;
                            nextNode = path.get(pathIndex).node.mac;
                        } else {
                            outputText += "И вы пришли";
                            isRouteEnd = true;
                            textToSpeech.speak(outputText);
                            return;
                        }

                    }
                    textToSpeech.speak(outputText);
                    /*
                     curentPath.speach();
                     Set next node wich we are expecting
                     and speach text at item 0
                    */
                } else {
                    Log.i(TAG, String.format("Bad route. Expected {0} but we are at {1} ", nextNode, whereAmI));
                    /*
                     Пользователь пришел в неправильную вершину
                     Перестройка пути
                     start - Предыдущая вершина
                     stop - Конечная вершина (destinationNode)
                     point - Вершина, у которой пользователь находиться сейчас (whereAmI)
                    */
                    if (!whereAmI.equals(previousBeaconAddress)) {
                        path = pathFinder.Dijkstra(whereAmI, destinationNode, whereAmI);
                        pathIndex = 1;
                        PathItem curentPath = path.get(pathIndex);
                        nextNode = path.get(++pathIndex).node.mac;
                        textToSpeech.speak(curentPath.getRouteText());
                        isSpeak = false;
                    } else {
                        isSpeak = true;
                    }
                    /*
                     curentPath.speach();
                     rebuild path from where we are at
                     and speach
                    */
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        StopScan();
        if (wakeLock != null)
            wakeLock.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wakeLock != null)
            wakeLock.acquire();

        _bindBeaconManager();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("System","--------------------------------------------------------------");
        InitSettings();
        setContentView(R.layout.activity_route);
        InitReferences();
        UpdateView();
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, ":LOCK_" + TAG);
        textToSpeech = new TTS.Builder(this, getResources().getConfiguration().locale)
                .setSpeechRate(0.85f)
                .setSignSeparation("@")
                .setPauseDurationMc(1000)
                .build();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (initialized && !outputText.isEmpty()) {
                    textToSpeech.speak(outputText);
                }
            }
        });

        if (getIntent() != null) {
            try {
                destinationNode = getIntent().getStringExtra("destination");
                GraphResponse graphResponse = new Gson().fromJson(getIntent().getStringExtra("location"), GraphResponse.class);
                pathFinder = new PathFinder(NetworkConverter.toGraph(graphResponse), graphResponse.getTurnType());
                initialized = true;
            } catch (Exception e) {
                Log.e(TAG, "INTENT KORYAVEI?" + e.toString());
            }
        }
    }

    /**
     * Привязка объектов к xml
     */
    private void InitReferences() {
        btn = findViewById(R.id.route_repeat_btn);
        tvDebug = findViewById(R.id.route_debug_text);
        tvDebugBuffer = findViewById(R.id.route_debug_buffer_text);
        llDebugLayout = findViewById(R.id.route_debug_layout);
    }

    /**
     * Метод, который в зависимости от режима {@link AppConstants#DEBUG_MODE},
     * Показывает все debug view, если debug режим включен, или скрывает оные
     */
    private void UpdateView() {
        if (AppConstants.DEBUG_MODE) {
            // Показать все debug view
            llDebugLayout.setVisibility(View.VISIBLE);
            btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        } else {
            // Скрыть все, что связано с debug и показать только Repeat Button;
            llDebugLayout.setVisibility(View.GONE);
            btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        }
    }

    /**
     * Получить значения из SharedPreferences,
     * Нужно, так как в приложении есть возможность настраивать некоторые значения для работы
     * в {@link SettingsActivity}
     */
    private void InitSettings() {
        SharedPreferences preferences = getSharedPreferences(AppConstants.SETTINGS_NAME, Context.MODE_PRIVATE);
        minUpdateDistance = preferences.getFloat(SettingsActivity.SETTINGS_MIN_DISTANCE, AppConstants.MIN_UPDATE_DISTANCE);
        distanceBufferSize = preferences.getInt(SettingsActivity.SETTINGS_BUFFER_SIZE, AppConstants.DISTANCE_BUFFER_SIZE);
        distanceOfOneMeter = preferences.getInt(SettingsActivity.SETTINGS_DISTANCE_OF_ONE_METER, AppConstants.DISTANCE_BUFFER_DEFAULT_VALUE);
        distanceDelta = preferences.getFloat(SettingsActivity.SETTINGS_DELTA, AppConstants.AVERAGE_DISTANCE_DELTA);
        updateTime = preferences.getInt(SettingsActivity.SETTINGS_UPDATE_TIME, AppConstants.UPDATE_TIME);
    }

    /**
     * Привязка beacon manager,
     * установка нужных для работы параметров
     * О каждом можно узнать из https://altbeacon.github.io/android-beacon-library/
     *
     * @see BeaconManager
     * @see ArmaRssiFilter
     */
    protected void _bindBeaconManager() {
        ArmaRssiFilter.setDEFAULT_ARMA_SPEED(0.35f);
        BeaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);
        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        mBeaconManager.getBeaconParsers().clear();
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.URI_BEACON_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        mBeaconManager.setForegroundScanPeriod(updateTime);

        mBeaconManager.bind(this);
    }

    /**
     * Callback on service connect
     * <p>
     * Добавление {@link #mRangeNotifier} для нахождения всех доступных маяков
     *
     * @see RangeNotifier
     */
    @Override
    public void onBeaconServiceConnect() {

        /*
         * Range notifier
         */
        mBeaconManager.addRangeNotifier(mRangeNotifier);

        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //StopScan();
        SharedPreferences sharedPreferences2;
        sharedPreferences2 = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences2.edit();
        editor.putBoolean("isFirstLaunch", true);
        editor.apply();
        if (textToSpeech != null)
            textToSpeech.shutdown();
    }

    /**
     * Остановка фонового сканирования пространства для поиска маяков
     * Удаление {@link #mRangeNotifier}
     *
     * @see BeaconManager
     */
    public void StopScan() {
        try {
            mBeaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.removeRangeNotifier(mRangeNotifier);
        //mBeaconManager.unbind(this);

    }

    /**
     * Callback метод для инициализации {@link #textToSpeech}
     * Также здесь выполняется метод {@link #_bindBeaconManager()}
     *
     * @param status Статус инициализации
     * @see TextToSpeech
     */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            _bindBeaconManager();

        } else
            throw new IllegalArgumentException("TextToSPeach cannot be initiallized");
    }


    //it should be be in another place, but now let it be here
    private String readFileFromRawDirectory(int resourceId) {
        InputStream iStream = getResources().openRawResource(resourceId);
        ByteArrayOutputStream byteStream = null;
        try {
            byte[] buffer = new byte[iStream.available()];
            iStream.read(buffer);
            byteStream = new ByteArrayOutputStream();
            byteStream.write(buffer);
            byteStream.close();
            iStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteStream.toString();
    }

}