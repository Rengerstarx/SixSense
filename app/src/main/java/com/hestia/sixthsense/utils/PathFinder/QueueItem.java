package com.hestia.sixthsense.utils.PathFinder;

/**
 * Для внутреннего использования, просто 2 поля объеденены в 1 класс.
 * Используется в {@link PathFinder#Dijkstra(String, String, String)}
 */
public class QueueItem {
    int distance=0;
    String name="";

    public QueueItem(int distance, String name) {
        this.distance = distance;
        this.name = name;
    }
}

