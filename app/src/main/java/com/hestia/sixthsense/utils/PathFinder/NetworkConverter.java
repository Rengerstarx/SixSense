package com.hestia.sixthsense.utils.PathFinder;

import com.hestia.sixthsense.data.network.model.beacon.EdgeResponse;
import com.hestia.sixthsense.data.network.model.beacon.GraphResponse;
import com.hestia.sixthsense.data.network.model.beacon.NodeResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Класс который переводит сетевые модели в модели которые используются в PathFinder
 *
 * @see Node
 * @see Edge
 * @see GraphResponse
 * @see NodeResponse
 * @see EdgeResponse
 */
public class NetworkConverter {
     public static Map<String,Node> toGraph (GraphResponse graphResponse) throws Exception {
         Map<String,Node> newGraph = new HashMap<String, Node>();
         for(NodeResponse node: graphResponse.getNodes())
         {
             ArrayList<Edge> edges = new ArrayList<Edge>();
             for(EdgeResponse edge : graphResponse.getEdges())
             {
                 //Get all edges where start is current node
                 if(edge.getStartNode(graphResponse).equals(node.getMac()))
                 {
                     String edgeEvents = edge.getEvents();
                     Edge newEdge = new Edge(edge.getStopNode(graphResponse),edge.getWeight(),edgeEvents);
                     edges.add(newEdge);
                 }
             }
             String nodeEvents = node.getEvents();
             newGraph.put(node.getMac(),
                     new Node(node.getMac(),
                             node.getMac(),
                             new Point(node.getX(), node.getY()),
                             edges,
                             nodeEvents,
                             node.isTurnsVerbose()));
         }

         return newGraph;
     }

}
