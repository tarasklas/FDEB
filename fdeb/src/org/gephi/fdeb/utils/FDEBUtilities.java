/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.fdeb.utils;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import org.gephi.barnes_hut.QuadNode;
import org.gephi.fdeb.FDEBCompatibilityRecord;
import org.gephi.fdeb.FDEBLayoutData;
import org.gephi.graph.api.*;
import processing.core.PVector;

/**
 *
 * @author megaterik
 */
public class FDEBUtilities {

    static final double EPS = 1e-6;
    /*
     * Dynamical calculing of K, in jflowmap K =
     * AxisMarks.ordAlpha(Math.min(xStats.getMax() - xStats.getMin(),
     * yStats.getMax() - yStats.getMin()) / 1000);
     */

    static public double calculateSprintConstant(Graph graph) {
        double minX = Integer.MAX_VALUE;
        double maxX = Integer.MIN_VALUE;
        double minY = Integer.MAX_VALUE;
        double maxY = Integer.MIN_VALUE;
        for (Node node : graph.getNodes()) {
            minX = Math.min(minX, node.getNodeData().x());
            minY = Math.min(minY, node.getNodeData().y());

            maxX = Math.max(maxX, node.getNodeData().x());
            maxY = Math.max(maxY, node.getNodeData().y());
        }
        return Math.min(maxX - minX, maxY - minY) / 1000;
    }

    static public void divideEdge(Edge edge, double subdivisionPointsPerEdge) {
        if (edge.isSelfLoop()) {
            return;
        }
        FDEBLayoutData data = (FDEBLayoutData) edge.getEdgeData().getLayoutData();
        Point.Double[] subdivisionPoints = new Point.Double[(int) (subdivisionPointsPerEdge + 2)];//+source/target
        double totalLength = 0.0;
        for (int i = 1; i < data.subdivisionPoints.length; i++) {
            totalLength += Point.Double.distance(data.subdivisionPoints[i - 1].x, data.subdivisionPoints[i - 1].y,
                    data.subdivisionPoints[i].x, data.subdivisionPoints[i].y);
        }
        double lengthPerPoint = totalLength / (((int) subdivisionPointsPerEdge) + 1);
        double remainingLengthPerPoint = lengthPerPoint;
        int curSubdivisionPoint = 1;
        for (int i = 1; i < data.subdivisionPoints.length; i++) {
            double lengthOfEdge = Point.Double.distance(data.subdivisionPoints[i - 1].x, data.subdivisionPoints[i - 1].y,
                    data.subdivisionPoints[i].x, data.subdivisionPoints[i].y);
            double remainingLengthOfEdge = lengthOfEdge;
            while (remainingLengthPerPoint < remainingLengthOfEdge) {
                remainingLengthOfEdge -= remainingLengthPerPoint;
                double coef = (lengthOfEdge - remainingLengthOfEdge) / (lengthOfEdge);//0.0--source, 1.0 -- target
                subdivisionPoints[curSubdivisionPoint] = new Point2D.Double(
                        data.subdivisionPoints[i - 1].x
                        + coef * (data.subdivisionPoints[i].x - data.subdivisionPoints[i - 1].x),
                        data.subdivisionPoints[i - 1].y
                        + coef * (data.subdivisionPoints[i].y - data.subdivisionPoints[i - 1].y));
                curSubdivisionPoint++;
                remainingLengthPerPoint = lengthPerPoint;
            }
            remainingLengthPerPoint -= remainingLengthOfEdge;
        }
        subdivisionPoints[0] = data.subdivisionPoints[0];//source
        subdivisionPoints[subdivisionPoints.length - 1] = data.subdivisionPoints[data.subdivisionPoints.length - 1];//target

        data.subdivisionPoints = subdivisionPoints;
    }

    static public void createCompatibilityRecords(Edge edge, double compatibilityThreshold, Graph graph, FDEBCompatibilityComputator computator) {
        ArrayList<FDEBCompatibilityRecord> similar = new ArrayList<FDEBCompatibilityRecord>();
        if (edge.isSelfLoop()) {
            ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).similarEdges = new FDEBCompatibilityRecord[0];
        }
        for (Edge probablySimilarEdge : graph.getEdges()) {
            if (probablySimilarEdge.isSelfLoop() || probablySimilarEdge == edge) {
                continue;
            }
            double compatibility = computator.calculateCompatibility(edge, probablySimilarEdge);
            //System.err.println(compatibility + " " + compatibilityThreshold);
            if (compatibility < compatibilityThreshold) {
                continue;
            }
            similar.add(new FDEBCompatibilityRecord(compatibility, probablySimilarEdge));
        }
        ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).similarEdges = new FDEBCompatibilityRecord[similar.size()];
        for (int i = 0; i < similar.size(); i++) {
            ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).similarEdges[i] = similar.get(i);
        }
    }

    static public void updateNewSubdivisionPoints(Edge edge, double sprintConstant, double stepSize, boolean useInverseQuadraticModel) {
        if (edge.isSelfLoop()) {
            return;
        }
        FDEBLayoutData data = edge.getEdgeData().getLayoutData();
        double k = sprintConstant / (data.length * (data.subdivisionPoints.length - 1));
        for (int i = 1; i < data.subdivisionPoints.length - 1; i++)//first and last are fixed
        {
            double Fsi_x = (data.subdivisionPoints[i - 1].x - data.subdivisionPoints[i].x)
                    + (data.subdivisionPoints[i + 1].x - data.subdivisionPoints[i].x);
            double Fsi_y = (data.subdivisionPoints[i - 1].y - data.subdivisionPoints[i].y)
                    + (data.subdivisionPoints[i + 1].y - data.subdivisionPoints[i].y);

            if (Math.abs(k) <= 1) {
                Fsi_x *= k;
                Fsi_y *= k;
            }
            double Fei_x = 0;
            double Fei_y = 0;

            for (FDEBCompatibilityRecord record : (((FDEBLayoutData) edge.getEdgeData().getLayoutData())).similarEdges) {
                Edge moveEdge = record.edgeWith;
                if (moveEdge.isSelfLoop()) {
                    continue;
                }

                FDEBLayoutData moveData = moveEdge.getEdgeData().getLayoutData();
                double v_x = moveData.subdivisionPoints[i].x - data.subdivisionPoints[i].x;
                double v_y = moveData.subdivisionPoints[i].y - data.subdivisionPoints[i].y;

                if (Math.abs(v_x) > EPS || Math.abs(v_y) > EPS) {
                    double len_sq = v_x * v_x + v_y * v_y;
                    double m;
                    if (!useInverseQuadraticModel) {
                        m = (record.compatibility / Math.sqrt(len_sq));
                    } else {
                        m = (record.compatibility / len_sq);
                    }

                    v_x *= m;
                    v_y *= m;

                    Fei_x += v_x;
                    Fei_y += v_y;
                }
            }
            /*
             * store new coordinates to update them simultaniously
             */
            data.newSubdivisionPoints[i] = new Point.Double(data.subdivisionPoints[i].x + stepSize * (Fei_x + Fsi_x),
                    data.subdivisionPoints[i].y + stepSize * (Fei_y + Fsi_y));
        }
    }
    public static int passed = 0;
    public static double passedValue = 0;
    public static int total = 0;
    public static int visited = 0;

    public static void updateNewSubdivisionPointsWithBarnesHutOptimization(Edge edge, double sprintConstant, double stepSize, QuadNode root, double compatibilityThreshold, FDEBCompatibilityComputator computator) {
        if (edge.isSelfLoop()) {
            return;
        }
        FDEBLayoutData data = edge.getEdgeData().getLayoutData();
        double k = sprintConstant / (data.length * (data.subdivisionPoints.length - 1));

        total += root.size;
        for (int i = 1; i < data.subdivisionPoints.length - 1; i++)//first and last are fixed
        {
            double Fsi_x = (data.subdivisionPoints[i - 1].x - data.subdivisionPoints[i].x)
                    + (data.subdivisionPoints[i + 1].x - data.subdivisionPoints[i].x);
            double Fsi_y = (data.subdivisionPoints[i - 1].y - data.subdivisionPoints[i].y)
                    + (data.subdivisionPoints[i + 1].y - data.subdivisionPoints[i].y);

            if (Math.abs(k) <= 1) {
                Fsi_x *= k;
                Fsi_y *= k;
            }
            data.newSubdivisionPoints[i] = new Point.Double(data.subdivisionPoints[i].x + stepSize * (Fsi_x),
                    data.subdivisionPoints[i].y + stepSize * (Fsi_y));
        }
        appendSimilarEdgesUsingBarnesHut(root, edge, compatibilityThreshold, stepSize, computator);
    }

    private static void appendSimilarEdgesUsingBarnesHut(QuadNode node, Edge edge, double compatibilityThreshold, double stepSize, FDEBCompatibilityComputator computator) {
        visited++;
        if (node.isLeaf) {
            if (node.storedElement != null) {
                Edge moveEdge = (Edge) node.storedElement;
                FDEBLayoutData data = edge.getEdgeData().getLayoutData();
                double compatibility = computator.calculateCompatibility(edge, moveEdge);
                if (compatibility < compatibilityThreshold) {
                    return;
                }
                if (moveEdge.isSelfLoop()) {
                    return;
                }
                if (moveEdge == edge) {
                    return;
                }

                passed++;
                passedValue += compatibility;
                for (int i = 1; i < data.subdivisionPoints.length - 1; i++) {
                    FDEBLayoutData moveData = moveEdge.getEdgeData().getLayoutData();
                    double v_x = moveData.subdivisionPoints[i].x - data.subdivisionPoints[i].x;
                    double v_y = moveData.subdivisionPoints[i].y - data.subdivisionPoints[i].y;

                    if (Math.abs(v_x) > EPS || Math.abs(v_y) > EPS) {
                        double len_sq = v_x * v_x + v_y * v_y;
                        double m = (compatibility / Math.sqrt(len_sq));

                        v_x *= m * stepSize;
                        v_y *= m * stepSize;
                        data.newSubdivisionPoints[i].x += v_x;
                        data.newSubdivisionPoints[i].y += v_y;
                    }
                }
            }
        } else {
            Point2D.Float projection = FDEBCompatibilityComputator.projectPointToLine(edge.getSource().getNodeData().x(), edge.getSource().getNodeData().y(),
                    edge.getTarget().getNodeData().x(), edge.getTarget().getNodeData().y(), node.center.x, node.center.y);
            double distanceToCenter = Point2D.distance(node.center.x, node.center.y, projection.x, projection.y);
            if (projection.x > Math.max(edge.getSource().getNodeData().x(), edge.getTarget().getNodeData().x())
                    || (projection.x < Math.min(edge.getSource().getNodeData().x(), edge.getTarget().getNodeData().x()))) {
                if (projection.y > Math.max(edge.getSource().getNodeData().y(), edge.getTarget().getNodeData().y())
                        || (projection.y < Math.min(edge.getSource().getNodeData().y(), edge.getTarget().getNodeData().y()))) {
                    //projection outside edge, therefore minimum will be at one of the ends
                    distanceToCenter = Math.min(
                            Point2D.distance(node.center.x, node.center.y, edge.getSource().getNodeData().x(), edge.getSource().getNodeData().y()),
                            Point2D.distance(node.center.x, node.center.y, edge.getTarget().getNodeData().x(), edge.getTarget().getNodeData().y()));
                }
            }

            if ((node.xr - node.xl + node.yr - node.yl) / distanceToCenter < 0.25) {
                return;
            }
            appendSimilarEdgesUsingBarnesHut(node.ld, edge, compatibilityThreshold, stepSize, computator);
            appendSimilarEdgesUsingBarnesHut(node.lu, edge, compatibilityThreshold, stepSize, computator);
            appendSimilarEdgesUsingBarnesHut(node.rd, edge, compatibilityThreshold, stepSize, computator);
            appendSimilarEdgesUsingBarnesHut(node.ru, edge, compatibilityThreshold, stepSize, computator);
        }
    }
}
