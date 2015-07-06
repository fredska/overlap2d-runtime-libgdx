package com.uwsoft.editor.renderer.utils;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.Segment;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by CyberJoe on 7/4/2015.
 */
public class PolygonUtils {

    public static Array<Vector2[]> mergeTouchingPolygons(Vector2[][] polys) {

        Set<Edge> uniqueEdges = new HashSet<>();
        Set<Edge> duplicateEdges = new HashSet<>();

        for(int i = 0; i < polys.length; i++) {
            for(int j = 0; j < polys[i].length; j++) {
                Edge edge = null;
                if(j < polys[i].length - 1) {
                    edge = new Edge(polys[i][j].x, polys[i][j].y, polys[i][j+1].x, polys[i][j+1].y);
                } else if(j == polys[i].length - 1) {
                    edge = new Edge(polys[i][j].x, polys[i][j].y, polys[i][0].x, polys[i][0].y);
                }
                if(uniqueEdges.contains(edge)) {
                    duplicateEdges.add(edge);
                } else {
                    uniqueEdges.add(edge);
                }
            }
        }

        uniqueEdges.removeAll(duplicateEdges);

        Array<Vector2[]> result = new Array<>();
        while(uniqueEdges.size() > 0) {
            Vector2[] mesh = extractClosedLoop(uniqueEdges);
            mesh = clearUnnecessaryVertices(mesh);
            result.add(mesh);
        }

        return result;
    }

    public static Vector2[] mergeTouchingPolygonsToOne(Vector2[][] polys) {
        Array<Vector2[]> result = mergeTouchingPolygons(polys);

        return result.get(0);
    }

    public static Vector2[] clearUnnecessaryVertices(Vector2[] points) {
        Array<Vector2> result = new Array<>();
        if(points.length < 3) return points;
        for(int i = 0; i < points.length; i++) {
            Vector2 currPoint = points[i];
            Vector2 prevPoint = points[points.length-1];
            Vector2 nextPoint = points[0];
            if (i > 0) prevPoint = points[i-1];
            if( i < points.length-1) nextPoint = points[i+1];
            if(Intersector.pointLineSide(prevPoint, nextPoint, currPoint) != 0) {
                result.add(currPoint);
            }
        }

        return result.toArray(Vector2.class);
    }

    public static Vector2[] extractClosedLoop(Set<Edge> edges) {
        ArrayList<Edge> sortedList = new ArrayList<>();
        Edge nextEdge = edges.stream().findFirst().get();
        sortedList.add(nextEdge);
        sortedList = recursivelySortChainPoints(edges, nextEdge, sortedList);
        edges.removeAll(sortedList);

        Vector2[] result = new Vector2[sortedList.size()];
        int iterator = 0;
        for(Edge edge: sortedList) {
            result[iterator++] = edge.start;
        }

        return result;
    }

    private static ArrayList<Edge> recursivelySortChainPoints(Set<Edge> edges, Edge edge, ArrayList<Edge> sortedList) {
        Edge nextEdge = findLink(edges, edge, edge.end);
        if(!edge.end.equals(nextEdge.start)) nextEdge.reverse();
        sortedList.add(nextEdge);
        if(sortedList.get(0).linkedTo(sortedList.get(sortedList.size() - 1)) && sortedList.size() >= 3) {
            //loop is closed
            return sortedList;
        }
        return recursivelySortChainPoints(edges, nextEdge, sortedList);
    }

    public static Edge findLink(Set<Edge> edges, Edge edge, Vector2 point) {
        for(Edge linkedEdge: edges) {
            if(!linkedEdge.equals(edge) && linkedEdge.linkedTo(point)) {
                return linkedEdge;
            }
        }

        return null;
    }

    public static class Edge {
        public Vector2 start;
        public Vector2 end;

        public Edge(float startX, float startY, float endX, float endY) {
            start = new Vector2(startX, startY);
            end = new Vector2(endX, endY);
        }

        public Edge(Vector2 start, Vector2 end) {
            this.start = start;
            this.end = end;
        }

        public boolean linkedTo(Vector2 point) {
            if(start.equals(point) || end.equals(point)) return true;
            return false;
        }

        public boolean linkedTo(Edge edge) {
            if(!this.equals(edge) && (start.equals(edge.end) || end.equals(edge.start) || end.equals(edge.end) || start.equals(edge.start))) return true;
            return false;
        }

        public void reverse() {
            Vector2 tmp = new Vector2(start);
            start = end;
            end = tmp;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Edge other = (Edge)obj;
            if(hashCode() == other.hashCode()) return true;
            return false;
        }

        @Override
        public int hashCode () {
            return start.hashCode() + end.hashCode();
        }
    }

}
