package com.hestia.sixthsense.utils.PathFinder;

/**
 * Просто точка (x, y)
 * Используется в {@link PathFinder#getDirection(Node, Node, Node)}
 */
public class Point {
    int x=0;
    int y=0;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double length() {
        return Math.sqrt(x*x+y*y);
    }
}
