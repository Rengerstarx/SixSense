package com.hestia.sixthsense.utils.PathFinder;

import java.util.ArrayList;


/**
 * Модель вершины, используемая в PathFinder
 *
 * Также в приложении есть модели другого вида (Сетевые), {@link com.hestia.sixthsense.data.network.model.beacon.GraphResponse},
 *  которые используются для работы с сетью.
 *  Чтобы перевести Сетевые модели в PathFinder-модели используется {@link NetworkConverter}
 *
 * @see Edge Модель ребра
 *
 */
public class Node {
    ArrayList<Edge> edges;
    String events;
    int distance = Integer.MAX_VALUE;
    String parent = "";
    public String name;
    public String mac;
    public Point coordinates;

    /**
     * При проходе через данную ноду, нужно ли произносить направление или нет
     */
    public boolean isTurnVerbose = true;

    public Node(String name,String mac,Point coords,ArrayList<Edge> edges,String events, boolean isTurnVerbose){
        this.edges = edges;
        this.name = name;
        this.mac = mac;
        this.coordinates = coords;
        this.events = events;
        this.isTurnVerbose = isTurnVerbose;
    }
}

