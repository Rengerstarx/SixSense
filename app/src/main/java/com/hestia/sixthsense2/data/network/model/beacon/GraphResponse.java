package com.hestia.sixthsense2.data.network.model.beacon;

import com.google.gson.annotations.SerializedName;
import com.hestia.sixthsense2.utils.AppConstants;

import java.util.List;

/**
 * Сетевая модель одной локации
 * (сущность которая включает в себя набор нод и ребер и привязан к определенной местности (локации).
 * Например локация "Корпус Д" или локация "Ростов ЮФУ").
 *
 * <p>
 * Сетевые модели - модели с сервера (классы оканчивающийся на Response).
 * Также есть модели для PathFinder (нахождение пути) {@link com.hestia.sixthsense2.utils.PathFinder},
 *  имеют такие же названия, но без Response.
 * Для создания подходяшего графа для PathFinder, исопльзуется {@link com.hestia.sixthsense2.utils.PathFinder.NetworkConverter}
 * </p>
 *
 * @see BeaconResponse Сетевая модель маячка (Beacon)
 * @see EdgeResponse Сетевая модель ребра
 * @see NodeResponse Сетевая модель ноды (вершины графа)
 *
 * @author Dmitry Abakumov <killerinshadow2@gmail.com>
 */
public class GraphResponse {
    @SerializedName("id")
    int id;
    @SerializedName("name")
    String name;


    @SerializedName("text")
    String text;

    @SerializedName("azimuth")
    int azimuth;

    /**
     * Повороты либо в часах (==false) либо просто направление (==true) (влево вправо, прямо по диагонали)
     */
    @SerializedName("is_old_turns")
    boolean isOldTurns = true;

    @SerializedName("nodes")
    private List<NodeResponse> nodes;

    @SerializedName("edges")
    private List<EdgeResponse> edges;

    private boolean isPhantomFormated = false;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public int getAzimuth() {
        return azimuth;
    }

    /**
     * @return список вершин графа
     */
    public List<NodeResponse> getNodes() {
        // Проверить, были ли узлы уже отформатированы, если нет - отформатировать, иначе вернуть отформатированный список
        if (!isPhantomFormated) {
            int i = 0;
            for (NodeResponse node : nodes) {
                if (node.isPhantom()) {
                    node.setMac(AppConstants.PHANTOM_NAME + AppConstants.PHANTOM_DELIMETR + i);         // Make phantom's mac like PHANTOM#5
                    i++;
                }
            }
            isPhantomFormated = true;
        }
        return nodes;
    }

    /**
     * @return true if old type/ false otherwise
     */
    public boolean getTurnType() {
        return isOldTurns;
    }

    public List<EdgeResponse> getEdges() {
        return edges;
    }


    public NodeResponse getNodeByMac(String mac) {
        for (NodeResponse node : nodes) {
            if (node.getMac().equals(mac))
                return node;
        }
        return null;
    }

    public boolean isBeaconContain(String mac) {
        for (NodeResponse node : nodes) {
            if (node.getMac().equals(mac))
                return true;
        }
        return false;
    }

    /**
     * Проверка, является ли маяк широковещательным, есть ли у него текст для произношения
     * (НЕ ИСПОЛЬЗУЕТСЯ)
     */
    public boolean isBeaconBroadcast(String mac) {
        for (NodeResponse node : nodes) {
            if (node.getMac().equals(mac)) {
                if (node.getBroadcastText().trim().length() > 0)
                    return true;
                else
                    return false;
            }
        }
        return false;
    }
}
