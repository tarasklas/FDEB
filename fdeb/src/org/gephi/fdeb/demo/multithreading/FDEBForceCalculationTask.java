/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.fdeb.demo.multithreading;

import org.gephi.fdeb.utils.FDEBUtilities;
import org.gephi.graph.api.Edge;

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

    public FDEBForceCalculationTask(Edge[] edge, int from, int to, double sprintConstant, double stepSize) {
        this.edge = edge;
        this.from = from;
        this.to = to;
        this.sprintConstant = sprintConstant;
        this.stepSize = stepSize;
    }

    @Override
    public void run() {
        for (int i = from; i < to; i++) {
            FDEBUtilities.updateNewSubdivisionPoints(edge[i], sprintConstant, stepSize);
        }
    }
}
