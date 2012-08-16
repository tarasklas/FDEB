/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.fdeb.demo.multithreading;

import org.gephi.fdeb.utils.FDEBCompatibilityComputator;
import org.gephi.fdeb.utils.FDEBUtilities;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;

/**
 *
 * @author megaterik
 */
public class FDEBForceCalculationTask implements Runnable {

    Edge[] edge;
    int from;
    int to;
    double sprintConstant;
    double stepSize;
    boolean useInverseQuadratic;
    boolean lowMemoryMode;
    FDEBCompatibilityComputator computator;
    double compatibilityThreshold;
    Graph graph;

    public FDEBForceCalculationTask(Edge[] edge, int from, int to, double sprintConstant, double stepSize, boolean useInverseQuadratic) {
        this.edge = edge;
        this.from = from;
        this.to = to;
        this.sprintConstant = sprintConstant;
        this.stepSize = stepSize;
        this.useInverseQuadratic = useInverseQuadratic;
        this.lowMemoryMode = false;
    }

    /*
     * Low memory mode
     */
    public FDEBForceCalculationTask(Edge[] edge, int from, int to, double sprintConstant, double stepSize, boolean useInverseQuadratic,
            FDEBCompatibilityComputator computator, double compatibilityThreshold, Graph graph) {
        this.edge = edge;
        this.from = from;
        this.to = to;
        this.sprintConstant = sprintConstant;
        this.stepSize = stepSize;
        this.useInverseQuadratic = useInverseQuadratic;
        this.lowMemoryMode = true;
        this.compatibilityThreshold = compatibilityThreshold;
        this.computator = computator;
        this.graph = graph;
    }

    @Override
    public void run() {
        for (int i = from; i < to; i++) {
            if (!lowMemoryMode) {
                FDEBUtilities.updateNewSubdivisionPoints(edge[i], sprintConstant, stepSize, useInverseQuadratic);
            } else {
                FDEBUtilities.updateNewSubdivisionPointsInLowMemoryMode(edge[i], sprintConstant, stepSize, useInverseQuadratic, graph, computator, compatibilityThreshold);
            }
        }
    }
}
