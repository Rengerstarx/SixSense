package com.hestia.sixthsense2.data.network.model.beacon;

import com.google.gson.annotations.SerializedName;

/**
 * Модель Edge (ребро графа), храниться на сервере в составе локации {@see {@link GraphResponse}}
 *
 * @see GraphResponse Сетевая модель локации
 * @see BeaconResponse Сетевая модель маячка (Beacon)
 * @see NodeResponse Сетевая модель ноды (вершины графа)
 *
 * @author Rengerstar <vip.bekezin@mail.ru>
 */
public class EdgeResponse {
    @SerializedName("start")
    private int start;

    @SerializedName("stop")
    private int stop;

    @SerializedName("weight")
    private int weight;

    @SerializedName("text")
    private String events;


    public int getStartId() {
        return start;
    }
    public int getStopId() {return stop;}

    /**
     * Получение стартовой вершины графа(локации)
     *
     * @param graph граф(локация)
     * @return mac-адрес стартовой вершины
     * @throws Exception Если нет стартовой вершины с таким id
     */
    public String getStartNode(GraphResponse graph) throws Exception {
        for(NodeResponse node: graph.getNodes()){
            if(node.getId() == this.start)
                return node.getMac();
        }
        // Handle here exception, when there is no node with specific ID
        throw new Exception("There is no such START node with ID="+this.start);
    }

    /**
     * Получение конечной вершины графа(локации)
     *
     * @param graph граф(локация)
     * @return mac-адрес конечной вершины
     * @throws Exception Если нет конечной вершины с таким id
     */
    public String getStopNode(GraphResponse graph) throws Exception {
        for(NodeResponse node: graph.getNodes()){
            if(node.getId() == this.stop)
                return node.getMac();
        }
        // Handle here exception, when there is no node with specific ID
        throw new Exception("There is no such STOP node with ID="+this.start);
    }

    public int getWeight() {
        return weight;
    }

    public String getEvents() {
        return events;
    }
}
