/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.fdeb;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import org.gephi.graph.api.*;
import org.jfree.data.gantt.Task;

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
    static public int totalEdges = 0;
    static public int passedEdges = 0;

    static public void createCompatibilityRecords(Edge edge, double compatibilityThreshold, Graph graph) {
        ArrayList<FDEBCompatibilityRecord> similar = new ArrayList<FDEBCompatibilityRecord>();
        if (edge.isSelfLoop()) {
            return;
        }
        for (Edge probablySimilarEdge : graph.getEdges()) {
            if (probablySimilarEdge.isSelfLoop() || probablySimilarEdge == edge) {
                continue;
            }
            double compatibility = FDEBCompatibilityComputator.calculateCompatibility(edge, probablySimilarEdge);
            //System.err.println(compatibility + " " + compatibilityThreshold);
            totalEdges++;
            if (compatibility < compatibilityThreshold) {
                continue;
            }
            passedEdges++;
            similar.add(new FDEBCompatibilityRecord(compatibility, probablySimilarEdge));
        }
        ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).similarEdges = new FDEBCompatibilityRecord[similar.size()];
        for (int i = 0; i < similar.size(); i++) {
            ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).similarEdges[i] = similar.get(i);
        }
    }

    static public void updateNewSubdivisionPoints(Edge edge, double sprintConstant, double stepSize) {
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
                    double m = (record.compatibility / Math.sqrt(len_sq));

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
}
