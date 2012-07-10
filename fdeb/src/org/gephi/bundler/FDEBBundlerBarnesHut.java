package org.gephi.bundler;

import java.awt.geom.Point2D;
import org.gephi.fdeb.utils.FDEBUtilities;
import java.util.Arrays;
import org.gephi.barnes_hut.QuadNode;
import org.gephi.edgelayout.spi.AbstractEdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayoutBuilder;
import org.gephi.edgelayout.spi.EdgeLayoutProperty;
import org.gephi.fdeb.FDEBBundlerParameters;
import org.gephi.fdeb.FDEBLayoutData;
import org.gephi.graph.api.Edge;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author megaterik
 */
public class FDEBBundlerBarnesHut extends AbstractEdgeLayout implements EdgeLayout, LongTask {

    private static final double EPS = 1e-7;
    private int cycle;
    private double stepSize;   // S
    private int iterationsPerCycle;    // I
    private double sprintConstant; // K
    private double compatibilityThreshold;
    private FDEBBundlerParameters parameters;
    private double subdivisionPointsPerEdge;
    private QuadNode root;
    private boolean cancel;
    private ProgressTicket progressTicket;

    public FDEBBundlerBarnesHut(EdgeLayoutBuilder layoutBuilder, FDEBBundlerParameters parameters) {
        super(layoutBuilder);
        this.parameters = parameters;
    }

    /*
     * Get parameters and init structures
     */
    @Override
    public void initAlgo() {
        for (Edge edge : graphModel.getGraph().getEdges()) {
            edge.getEdgeData().setLayoutData(
                    new FDEBLayoutData(edge.getSource().getNodeData().x(), edge.getSource().getNodeData().y(),
                    edge.getTarget().getNodeData().x(), edge.getTarget().getNodeData().y()));
        }
        cycle = 1;
        setConverged(false);
        stepSize = parameters.getStepSize();
        sprintConstant = parameters.getSprintConstant();
        iterationsPerCycle = parameters.getIterationsPerCycle();
        compatibilityThreshold = parameters.getCompatibilityThreshold();
        sprintConstant = FDEBUtilities.calculateSprintConstant(graphModel.getGraph());
        subdivisionPointsPerEdge = 1;//start and end doesnt count
        System.out.println("K " + sprintConstant);

        buildAQuadTree(graphModel.getGraph().getEdges().toArray());
    }

    @Override
    public void goAlgo() {
        System.err.println("Next iteration");
        if (cancel) {
            return;
        }

        for (int step = 0; step < iterationsPerCycle; step++) {
            for (Edge edge : graphModel.getGraph().getEdges()) {
                ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).newSubdivisionPoints = Arrays.copyOf(((FDEBLayoutData) edge.getEdgeData().getLayoutData()).subdivisionPoints,
                        ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).subdivisionPoints.length);
            }

            for (Edge edge : graphModel.getGraph().getEdges()) {
                if (cancel) {
                    return;
                }
                FDEBUtilities.updateNewSubdivisionPointsWithBarnesHutOptimization(edge, sprintConstant, stepSize, root, compatibilityThreshold);
            }
            if (step == 0 && cycle == 1) {
                System.err.println(FDEBUtilities.passed + " " + FDEBUtilities.passedValue);
                System.err.println(FDEBUtilities.total + " " + FDEBUtilities.visited + " " + ((double) FDEBUtilities.visited) / FDEBUtilities.total);
            }

            for (Edge edge : graphModel.getGraph().getEdges()) {
                ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).subdivisionPoints = ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).newSubdivisionPoints;
                ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).newSubdivisionPoints = null;
            }
            if (cancel) {
                return;
            }
        }

        if (cycle == parameters.getNumCycles()) {
            setConverged(true);
        } else {
            prepareForTheNextStep();
        }
    }

    void prepareForTheNextStep() {
        cycle++;
        stepSize *= (1.0 - parameters.getStepDampingFactor());
        iterationsPerCycle = (iterationsPerCycle * 2) / 3;
        divideEdges();
    }

    void divideEdges() {
        subdivisionPointsPerEdge *= parameters.getSubdivisionPointIncreaseRate();
        for (Edge edge : graphModel.getGraph().getEdges()) {
            FDEBUtilities.divideEdge(edge, subdivisionPointsPerEdge);
        }
    }

    @Override
    public void endAlgo() {
    }

    @Override
    public void resetPropertiesValues() {
    }

    @Override
    public void removeLayoutData() {
        for (Edge edge : graphModel.getGraph().getEdges()) {
            edge.getEdgeData().setLayoutData(null);
        }
    }

    public void buildAQuadTree(Edge[] edges) {
        float minX, minY, maxX, maxY;
        minX = maxX = edges[0].getSource().getNodeData().x();
        minY = maxY = edges[0].getSource().getNodeData().y();
        for (Edge edge : edges) {
            minX = Math.min(minX, edge.getSource().getNodeData().x());
            maxX = Math.max(maxX, edge.getSource().getNodeData().x());

            minY = Math.min(minY, edge.getSource().getNodeData().y());
            maxY = Math.max(maxY, edge.getSource().getNodeData().y());

            minX = Math.min(minX, edge.getTarget().getNodeData().x());
            maxX = Math.max(maxX, edge.getTarget().getNodeData().x());

            minY = Math.min(minY, edge.getTarget().getNodeData().y());
            maxY = Math.max(maxY, edge.getTarget().getNodeData().y());
        }
        root = new QuadNode(minX, minY, maxX, maxY);
        for (Edge edge : edges) {
            root.push(new Point2D.Float((edge.getSource().getNodeData().x() + edge.getTarget().getNodeData().x()) / 2f,
                    (edge.getSource().getNodeData().y()) + edge.getTarget().getNodeData().y() / 2f), edge);
        }
    }

    @Override
    public void modifyAlgo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean cancel() {
        this.cancel = true;
        setConverged(true);
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    @Override
    public EdgeLayoutProperty[] getProperties() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean shouldRefreshPreview() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}