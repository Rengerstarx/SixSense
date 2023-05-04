package com.hestia.sixthsense.utils.PathFinder.Path;

import com.hestia.sixthsense.utils.PathFinder.Node;

import java.util.ArrayList;


/**
 * {@link com.hestia.sixthsense.utils.PathFinder.PathFinder} при постройке маршрута, возвращает массив PathItem.
 *
 */
public class PathItem {
    public Node node;
    public ArrayList<String> path;
    public PathItem(Node node, ArrayList<String> path) {
        this.node = node;
        this.path = path;
    }

    public PathItem(Node node, String path) {
        this.node = node;
        this.path = new ArrayList<>();
        this.path.add(path);
    }

    public String getRouteText()
    {
        String route="";
        for(String r : path)
        {
            if(r != null && !r.isEmpty())
               route += r + "!";
        }
        return route;
    }

}
