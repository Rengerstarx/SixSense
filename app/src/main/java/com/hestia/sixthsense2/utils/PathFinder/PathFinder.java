package com.hestia.sixthsense2.utils.PathFinder;

import com.hestia.sixthsense2.utils.PathFinder.Path.PathItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.lang.Math;
import java.util.Stack;

/**
 * Класс для построения маршрута в графе, а также несколько дополнительных методов для работы с путем
 */
public class PathFinder {

    Map<String, Node> InitalGraph;

    /**
     * Вид направления пользователя
     * <p>
     * <ul>
     * <li>Если true - направление происходит с помощью слов, например "Двигйатесь влево/вправо"
     * <li>Если false - направление происходит с помощью часов, например "Поверните на Х часов"
     * </ul>
     */
    boolean isHeadingByDirections = true;

    /**
     * @param graph Граф, в котором будет производиться поиск пути
     */
    public PathFinder(Map<String, Node> graph) {
        this.InitalGraph = graph;
    }

    /**
     * @param graph Граф, в котором будет производиться поиск пути
     * @param directionType Если true - направление происходит с помощью слов, например "Двигйатесь влево/вправо"
     * Если false - направление происходит с помощью часов, например "Поверните на Х часов"
     */
    public PathFinder(Map<String, Node> graph, boolean directionType) {
        this.InitalGraph = graph;
        isHeadingByDirections = directionType;
    }

    /**
     * Реалзицация алгоритма Дейкстры для поиска пути в графе
     * @param start Стартовая вершина, откуда строится путь
     * @param stop Конечная вершина, куда должен привести путь
     * @param point Текущая вершина, у которой пользователь находится сейчас, используется только если
     *              пользователь заблудился для перестройки маршрута, если пользователь идет по
     *              правильному пути, то передавать пустую строку
     * @return Список {@link PathItem}, вершин до точки назначения
     */
    public ArrayList<PathItem> Dijkstra(String start, String stop, String point) {
        Map<String, Node> graph = new HashMap<String, Node>(this.InitalGraph);
        for (Node node : graph.values()) {
            node.distance = Integer.MAX_VALUE;
            node.parent = "";
        }


        graph.get(start).distance = 0;
        graph.get(start).parent = start;

        PriorityQueue<QueueItem> queue = new PriorityQueue<QueueItem>(graph.size() + 1,
                (Comparator<? super QueueItem>) new Comparator<QueueItem>() {
                    public int compare(QueueItem item1, QueueItem item2) {
                        return item1.distance - item2.distance;
                    }
                });


        queue.add(new QueueItem(0, start));
        while (!queue.isEmpty()) {
            QueueItem currentItem = queue.poll();
            String v = currentItem.name;
            int dist = currentItem.distance;

            if (graph.get(v).distance < dist)
                continue;
            for (Edge edge : graph.get(v).edges) {
                if (graph.get(edge.nodeName).distance > graph.get(v).distance + edge.weight) {
                    graph.get(edge.nodeName).distance = graph.get(v).distance + edge.weight;
                    queue.add(new QueueItem(graph.get(edge.nodeName).distance, edge.nodeName));
                    graph.get(edge.nodeName).parent = v;
                }
            }
        }
        ArrayList<String> nodeSequence = new ArrayList<>();
        nodeSequence.add(stop);

        Node item = graph.get(stop);

        while (!item.parent.equals(item.name)) {
            nodeSequence.add(item.parent);
            item = graph.get(item.parent);
        }


        Collections.reverse(nodeSequence);

        //массив евентов для самой первой ноды маршрута
        ArrayList<String> route1 = new ArrayList<>();
        ArrayList<PathItem> path = new ArrayList<>();


        //Если пользователь пришел к неправильной вершине (заблудился)
        if (point.length() != 0) {

            ArrayList<String> backRoute = new ArrayList<>();
            String edgeEvents = "";

            //добавляем вес и запоминаем пограничные события
            //это сделано для того, чтобы сохранить порядок путей
            backRoute.add("Двигайтесь Назад");

            for (Edge edge : graph.get(point).edges) {
                if (edge.nodeName.equals(start)) {
                    edgeEvents = edge.events;
                    break;
                }
            }
            backRoute.add(graph.get(point).events);
            backRoute.add(edgeEvents);
            path.add(new PathItem(graph.get(point), backRoute));

        }
        // Beacon Event
        route1.add(graph.get(nodeSequence.get(0)).events);

        // Направление для первой вершины маршрута
        /*if (point.length() > 0)
            //Если пользователь заблудился и маршрут перестраивается,
            // направление до последней вершины от которой он ушел
            route1.add(getDirection(graph.get(point), graph.get(nodeSequence.get(0)), graph.get(nodeSequence.get(1))));
        else if(graph.get(nodeSequence.get(0)).isTurnVerbose)
            //При начале пути
            route1.add("Двигайтесь Прямо");*/

        // Edges events
        for (Edge edge : graph.get(nodeSequence.get(0)).edges) {
            if (edge.nodeName.equals(nodeSequence.get(1))) {
                route1.add(edge.events);
            }
        }

        //Добавить первую вершину к пути
        path.add(new PathItem(item, route1));

        for (int i = 1; i < nodeSequence.size() - 1; i++) {
            ArrayList<String> route = new ArrayList<>();
            // Node events
            route.add(graph.get(nodeSequence.get(i)).events);
            // Направление
            //route.add(getDirection(graph.get(nodeSequence.get(i - 1)), graph.get(nodeSequence.get(i)), graph.get(nodeSequence.get(i + 1))));

            for (Edge edge : graph.get(nodeSequence.get(i)).edges) {
                if (edge.nodeName.equals(nodeSequence.get(i + 1))) {
                    route.add(edge.events);
                }
            }
            path.add(new PathItem(graph.get(nodeSequence.get(i)), route));
        }

        Node lastNode = graph.get(nodeSequence.get(nodeSequence.size() - 1));
        path.add(new PathItem(lastNode, lastNode.events));
        return path;

    }

    /**
     * Фантомная ли метка.
     * <p>
     * Ее не возможно поймать.
     * Такие метки используются в основном там, где невозможно или не целесообразно ставить физические метки.
     * Хотя эти метки невозможно поймать, но они как и обычные метки используются для введения по маршруту.
     * Например, если маяки идут по коридору и мы хотим где-то повернуть направо или налево,
     * то на повороте можно сделать не физический маячок, а просто на графе отметить его как точку
     * к которой нужно попасть, у которой есть своя инструкция
     * Поворот и т.п)
     * @param mac mac-адрес проверяемой метки
     */
    public boolean isPhantom(String mac) {
        Node node = InitalGraph.get(mac);
        if (node.mac.split("#")[0].equals("PHANTOM")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Определение соседних к данной вершин
     * <p>
     * Используется в {@link com.hestia.sixthsense2.ui.route.RouteActivity} для фильтрации
     * найденных из пространства маяков по текущему графу
     *
     * @param mac mac-адрес вершины, у которой ищутся соседние вершины
     * @return Список mac-адресов соседних к данной вершин
     */
    public ArrayList<String> getNeighborsMac(String mac) {
        ArrayList<String> neighborsMAC = new ArrayList<>();
        if (isContainNode(mac)) {
            Node node = InitalGraph.get(mac);
            for (Edge edge : node.edges) {
                // Mac узла, который связан с текущим узлом
                if (isPhantom(edge.nodeName)) {
                    // Если текущий узел является фантомным, рекурсивно пройти по всем узлам и найти не фантомный
                    Stack<Node> stack = new Stack<>();
                    stack.push(InitalGraph.get(edge.nodeName));
                    HashSet<String> visitedNodes = new HashSet<>();

                    while (!stack.empty()) {
                        Node currentNode = stack.pop();
                        for (Edge phantomEdge : currentNode.edges) {
                            String nodeMac = phantomEdge.nodeName;
                            if (isPhantom(phantomEdge.nodeName)) {
                                if (!visitedNodes.contains(nodeMac)) {
                                    stack.push(InitalGraph.get(phantomEdge.nodeName));
                                }
                            } else {
                                // Если не равен самому себе
                                if (!phantomEdge.nodeName.equals(node.mac))
                                    neighborsMAC.add(phantomEdge.nodeName);
                            }
                        }
                        visitedNodes.add(currentNode.mac);
                        // Проверка, посещена ли вершина
                    }
                } else {
                    neighborsMAC.add(edge.nodeName);
                }
            }
        }
        return neighborsMAC;
    }

    /**
     * Определение направления поворота пользователя к следующей вершине пути
     * <p>
     * По трем вершинам считается примерный угол поворота,
     * который необходимо сделать пользователю, что направиться к нужной вершине (следующей в пути).
     * <p>
     * Посчитанный угол конвертируется в строку вида "Двигйатесь влево/вправо" или "Поверните на Х часов"
     * в зависимости от значения переменной {@link #isHeadingByDirections}, по умолчанию первое, методами
     * {@link #ConvertDegreesToWordDirection(double)} и {@link #ConvertDegreesToHourDirection(double)}
     * @param node1 Вершина от которой происходит направление (Например текущая)
     * @param node2 Вершина к которой происходит направление
     * @param node3 Следующая за node2 вершина
     * @return Направление в виде строки
     */
    public String getDirection(Node node1, Node node2, Node node3) {
        if(!node2.isTurnVerbose)
            return "";              // if direction hint is disabled, return empty direction
        double x1 = node1.coordinates.x;
        double x2 = node2.coordinates.x;
        double x3 = node3.coordinates.x;
        double y1 = node1.coordinates.y;
        double y2 = node2.coordinates.y;
        double y3 = node3.coordinates.y;

        Point nodeA = new Point((int) (x2 - x1), (int) (y2 - y1));
        Point nodeB = new Point((int) (x3 - x2), (int) (y3 - y2));

        double sinLength = (nodeA.x * nodeB.y - nodeA.y * nodeB.x) / (nodeA.length() * nodeB.length());
        double cosLength = (nodeA.x * nodeB.x + nodeA.y * nodeB.y) / (nodeA.length() * nodeB.length());
        double degrees=0;
        if(sinLength > 0 && cosLength > 0)
            degrees = Math.asin(sinLength)*180/Math.PI;
        else if( sinLength <=0 && cosLength > 0)
            degrees = Math.asin(sinLength)*180/Math.PI;
        else if(sinLength > 0 && cosLength <=0)
            degrees = Math.acos(cosLength)*180/Math.PI;
        else if(sinLength<=0 && cosLength <=0 )
            degrees = -Math.acos(cosLength)*180/Math.PI;

        if(isHeadingByDirections)
            return ConvertDegreesToWordDirection(degrees);
        else
            return ConvertDegreesToHourDirection(degrees);
    }

    /**
     * Конвертация градусов поворота в форму часов, "Поверните на Х часов"
     * @param Degrees Углы для конвертации
     * @return Описание направления поворота в виде часов
     */
    private String ConvertDegreesToHourDirection(double Degrees)
    {
        int hours;
        if (Degrees > 0)
            hours = (int)Math.round(Degrees / 30);
        else
            hours = 12 - (int)Math.round(Math.abs(Degrees) / 30);

        if (hours != 12 || hours != 0)
            return "Поверните на " + hours + " часов";
        return "Двигайтесь Прямо";
    }

    /**
     * Конвертация градусов поворота в словесную форму, "Двигйатесь влево/вправо"
     * @param Degrees Углы для конвертации
     * @return Описание направления в виде слов направления
     */
    private String ConvertDegreesToWordDirection(double Degrees)
    {
        if (Math.abs(Degrees) < 30)
            return "Двигайтесь Прямо";
        else if (Math.abs(Degrees) > 135)
            return "Развернитесь";
        else if (Math.abs(Degrees) < 60) {
            if (Degrees > 0)
                return "Двигайтесь по диагонали вправо";
            else
                return "Двигайтесь по диагонали влево";
        }else{
            if (Degrees > 0)
                return "Поверните Направо";
            else
                return "Поверните Налево";
        }
    }

    /**
     * Содержит ли данная локация переданную вершину
     * @param mac Вершина, для которой определяется содержится ли она в графе
     */
    public boolean isContainNode(String mac) {
        return InitalGraph.containsKey(mac);
    }

    public Double getAngle(Point node1, Point node2, Point node3) {
        double x1 = node1.x;
        double x2 = node2.x;
        double x3 = node3.x;
        double y1 = node1.y;
        double y2 = node2.y;
        double y3 = node3.y;
        if ((node2.x - node1.x) * (node3.y - node2.y) - (node3.x - node2.x) * (node2.y - node1.y) > 0) {
            return Math.acos(((x2 - x1) * (x3 - x2) + (y2 - y1) * (y3 - y2)) / Math.sqrt((Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)) * (Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)))) * 180 / Math.PI;
        } else {
            return -Math.acos(((x2 - x1) * (x3 - x2) + (y2 - y1) * (y3 - y2)) / Math.sqrt((Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)) * (Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)))) * 180 / Math.PI;
        }
    }
}


