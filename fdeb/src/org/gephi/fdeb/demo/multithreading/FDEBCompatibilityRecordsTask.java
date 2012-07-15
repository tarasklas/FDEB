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
public class FDEBCompatibilityRecordsTask implements Runnable {

    Edge[] edges;
    int from;
    int to;
    Graph graph;
    double compatibilityThreshold;
    FDEBCompatibilityComputator computator;

    public FDEBCompatibilityRecordsTask(Edge[] edges, int from, int to, double compatibilityThreshold, Graph graph, FDEBCompatibilityComputator computator) {
        this.edges = edges;
        this.from = from;
        this.to = to;
        this.compatibilityThreshold = compatibilityThreshold;
        this.graph = graph;
        this.computator = computator;
    }

    @Override
    public void run() {
        for (int i = from; i < to; i++) {
            FDEBUtilities.createCompatibilityRecords(edges[i], compatibilityThreshold, graph, computator);
        }
    }
}
