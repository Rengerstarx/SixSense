package com.hestia.sixthsense.utils.PathFinder;


/**
 * Модель ребра, используемая в PathFinder
 *
 * Также в приложении есть модели другого вида (Сетевые), {@link com.hestia.sixthsense.data.network.model.beacon.GraphResponse},
 *  которые используются для работы с сетью.
 *  Чтобы перевести Сетевые модели в PathFinder-модели используется {@link NetworkConverter}
 *
 * @see Node Модель вершины
 *
 */
public class Edge {
    // Node MAC address
    String nodeName;
    int weight;
    String events;

    public Edge(String nodeName, int weight, String events) {
        this.nodeName = nodeName;
        this.weight = weight;

        // change later initialization from constructor
        this.events = events;
    }
}
