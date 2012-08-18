package org.gephi.bundler;

import java.util.Arrays;
import org.gephi.edgelayout.spi.EdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayoutBuilder;
import org.gephi.fdeb.FDEBLayoutData;
import org.gephi.fdeb.utils.FDEBUtilities;
import org.gephi.graph.api.Edge;
import org.gephi.utils.longtask.spi.LongTask;

/**
 *
 * @author megaterik
 */
public class FDEBBundler extends FDEBAbstractBundler implements EdgeLayout, LongTask {

    public FDEBBundler(EdgeLayoutBuilder layoutBuilder) {
        super(layoutBuilder);
    }
    long startTime;
    long endTime;
    /*
     * Get parameters and init structures
     */

    @Override
    public void initAlgo() {
        startTime = System.currentTimeMillis();
        for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
            edge.getEdgeData().setLayoutData(
                    new FDEBLayoutData(edge.getSource().getNodeData().x(), edge.getSource().getNodeData().y(),
                    edge.getTarget().getNodeData().x(), edge.getTarget().getNodeData().y()));
        }
        cycle = 1;
        setConverged(false);
        subdivisionPointsPerEdge = 1;//start and end doesnt count
        stepSize = stepSizeAtTheBeginning;
        iterationsPerCycle = iterationsPerCycleAtTheBeginning;
        System.out.println("K " + springConstant);

        createCompatibilityLists();
    }

    @Override
    public void goAlgo() {
        if (cancel) {
            return;
        }

        System.err.println("Next iteration");
        for (int step = 0; step < iterationsPerCycle; step++) {
            for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
                if (cancel) {
                    return;
                }
                ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).newSubdivisionPoints = Arrays.copyOf(((FDEBLayoutData) edge.getEdgeData().getLayoutData()).subdivisionPoints,
                        ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).subdivisionPoints.length);
            }

            for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
                if (cancel) {
                    return;
                }
                if (!useLowMemoryMode) {
                    FDEBUtilities.updateNewSubdivisionPoints(edge, springConstant, stepSize, useInverseQuadraticModel);
                } else {
                    FDEBUtilities.updateNewSubdivisionPointsInLowMemoryMode(edge, springConstant, stepSize, useInverseQuadraticModel,
                            graphModel.getGraph(), computator, compatibilityThreshold);
                }
            }

            for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
                ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).subdivisionPoints = ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).newSubdivisionPoints;
                ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).newSubdivisionPoints = null;
            }

            if (cancel) {
                return;
            }
        }

        if (cycle == numCycles) {
            setConverged(true);
            endTime = System.currentTimeMillis();
            System.err.println(endTime - startTime + " execution time");
        } else {
            prepareForTheNextStep();
        }
    }

    void prepareForTheNextStep() {
        cycle++;
        stepSize *= (1.0 - stepDampingFactor);
        iterationsPerCycle = (iterationsPerCycle * iterationIncreaseRate);
        divideEdges();
    }

    void divideEdges() {
        subdivisionPointsPerEdge *= subdivisionPointIncreaseRate;
        for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
            FDEBUtilities.divideEdge(edge, subdivisionPointsPerEdge);
        }
    }
}
