package com.hestia.sixthsense.ui.routing;
import com.hestia.sixthsense.data.network.model.beacon.GraphResponse;
import com.hestia.sixthsense.utils.PathFinder.PathFinder;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.RangeNotifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Некоторые вспомогательные функции для работы BeaconBroadcastListener
 *
 * @see BeaconBroadcastListener
 */
public class BeaconBroadcastListenerHelper {
    private GraphResponse locationGraph = null;
    private Map<String,int[]> beaconBuffer = new HashMap<>();
    private int distanceBufferIndex = 0;
    private int distanceBufferSize = 5;
    private int defaultBufferValue = 1;
    private int minUpdateDistance = -110;
    private double distanceDelta = 10;
    public int maxFillIterations = distanceBufferSize;
    public int filledIterationCount = 0;
    public String lastBeaconAddress = "";
    public boolean isFilled = false;


    public BeaconBroadcastListenerHelper(GraphResponse locationGraph,int distanceBufferSize, int defaultBufferValue, int minUpdateDistance, double distanceDelta) {
        this.locationGraph = locationGraph;
        this.distanceBufferSize = distanceBufferSize;
        this.defaultBufferValue = defaultBufferValue;
        this.minUpdateDistance = -110;
        this.distanceDelta = distanceDelta;
        this.maxFillIterations = distanceBufferSize;
    }
    public void fillBeaconBuffer(Collection<Beacon> filteredBeacons){
        if(!isFilled){
            updateBeaconBuffer(filteredBeacons);
            filledIterationCount++;
        }
        if(filledIterationCount > maxFillIterations)
            isFilled = true;
    }

    /**
     * Получение ближайшего (или самого сильного) маяка из коллекции, переданной в качестве параметра,
     *  самый сильный означает, что уровень rssi выше, чем у других маяков
     *  <p> Также, происходит заполнение буффера маяками, а у уже имеющихся маяков обновляюся расстояния
     *  Заполение происходит у всех маяков по одному индексу {@link #distanceBufferIndex}.
     *  То есть у некоторых маяков оно может начаться не с 0 индекса, но буфер кольцевой, поэтому не страшно.
     *  <p></p>
     *  Нахождение сильнейшей метки происходит путем сложения сложения всех расстояний у каждого маяка
     *  @param filteredBeacons коллекция маяков, в которой будет производиться поиск самого сильного маяка
     *  @return mac-адрес самого сильного маяка
     *  если весь буфер был заполнен значением defaultBufferValue, то самый сильный будет пустым, а затем вернет последний
     */
    public String getStrongestBeacon(Collection<Beacon> filteredBeacons) {
        updateBeaconBuffer(filteredBeacons);
        double strongestBeaconDistance = Integer.MIN_VALUE;
        String strongestBeaconMac = "";

        for (Map.Entry<String, int[]> entry : beaconBuffer.entrySet()) {
            String mac = entry.getKey();
            int[] distanceArray = entry.getValue();
            double currentDistance = 0;
            int validCount = 0;
            for (double distance : distanceArray) {
                if(distance <= 0){
                    currentDistance += distance;
                    validCount++;
                }
            }
            if(validCount== 0)
                continue;
            currentDistance = currentDistance/validCount;       // take an average
            if (currentDistance > strongestBeaconDistance) {
                strongestBeaconDistance = currentDistance;
                strongestBeaconMac = mac;
            }
        }
        // At first iteration, lastBeaconAddress is empty
        if (lastBeaconAddress.length() == 0) {
            // if all buffer was filled with -1 strongest will be empty
            if(strongestBeaconMac.length() == 0){
                return "";
            }else
                return strongestBeaconMac;
        }

        // if all buffer was filled with defaultBufferValue strongest will be empty then return last one
        if(strongestBeaconMac.length() == 0){
            return lastBeaconAddress;
        }
        // On next Iterations find most nearest
        double lastBeaconDistances = 0;
        int validCount = 0;
        for (int rsii : beaconBuffer.get(lastBeaconAddress)) {
            if(rsii <=0){
                lastBeaconDistances += rsii;
                validCount++;
            }
        }

        if(validCount == 0)                                 // if validCount == 0 that mean buffer empty and signal should be min possible value
            lastBeaconDistances = Integer.MIN_VALUE;
        else
            lastBeaconDistances = lastBeaconDistances/validCount;

        if (strongestBeaconDistance > lastBeaconDistances + distanceDelta) {
            lastBeaconAddress = strongestBeaconMac;
            return strongestBeaconMac;
        } else
            return lastBeaconAddress;
    }

    /**
     * Первоначальное заполнение буффера маяков {@link #beaconBuffer} из массива, пока bufferFillPasses < distanceBufferSize
     * Вызывается метод в callback-методе didRangeBeaconsInRegion, что в свою очередь часть интерфейса
     * {@link RangeNotifier} раз в секунду. Переданный массив маяков до маяков является массивом всех
     * доступных маяков на данный момент для устройства.
     *
     * <p>Все элементы массива по умолчанию заполняются значением {@link #defaultBufferValue}, то есть 1
     * @param beacons список маяков, которыми будет заполняться буффер
     */
    private void updateBeaconBuffer(Collection<Beacon> beacons){
        for (Beacon beacon : beacons) {
            String mac = beacon.getBluetoothAddress();
            if (!beaconBuffer.containsKey(mac)) {
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
            String mac = beacon.getBluetoothAddress();
            int[] buffer = beaconBuffer.get(mac);
            if(buffer != null){
                buffer[distanceBufferIndex % distanceBufferSize] = beacon.getRssi();
            }
        }
        distanceBufferIndex++;
    }

    /**
     * Фильтрация маяков по Пути. Все узлы, которые не на пути, следует игнорировать
     * @param collection Список, где будет производится фильтр
     * @return Отобранные маячки
     *
     * @see PathFinder#getNeighborsMac(String)
     */
    public Collection<Beacon> filterByLocation(Collection<Beacon> collection) {
        Collection<Beacon> filtered = new ArrayList<>();
        for(Beacon beacon: collection){
            if(locationGraph.isBeaconContain(beacon.getBluetoothAddress()))
                filtered.add(beacon);
        }
        return filtered;
    }


    public Collection<Beacon> filterByBroadcast(Collection<Beacon> collection) {
        Collection<Beacon> filtered = new ArrayList<>();
        for(Beacon beacon: collection){
            if(locationGraph.isBeaconBroadcast(beacon.getBluetoothAddress()))
                filtered.add(beacon);
        }
        return filtered;
    }

}
