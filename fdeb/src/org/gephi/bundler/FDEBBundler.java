package org.gephi.bundler;

import org.gephi.fdeb.utils.FDEBUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import oracle.jrockit.jfr.tools.ConCatRepository;
import org.gephi.edgelayout.plugin.AbstractEdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayoutBuilder;
import org.gephi.fdeb.FDEBBundlerParameters;
import org.gephi.fdeb.FDEBCompatibilityRecord;
import org.gephi.fdeb.FDEBLayoutData;
import org.gephi.graph.api.Edge;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author megaterik
 */
public class FDEBBundler extends AbstractEdgeLayout implements EdgeLayout, LongTask {

    private static final double EPS = 1e-7;
    private int cycle;
    private double stepSize;   // S
    private int iterationsPerCycle;    // I
    private double sprintConstant; // K
    private double compatibilityThreshold;
    private FDEBBundlerParameters parameters;
    private double subdivisionPointsPerEdge;
    private ProgressTicket progressTicket;
    private boolean cancel;

    public FDEBBundler(EdgeLayoutBuilder layoutBuilder, FDEBBundlerParameters parameters) {
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

        createCompatibilityLists();
    }

    @Override
    public void goAlgo() {
        if (cancel) {
            return;
        }

        System.err.println("Next iteration");
        for (int step = 0; step < iterationsPerCycle; step++) {
            for (Edge edge : graphModel.getGraph().getEdges()) {
                ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).newSubdivisionPoints = Arrays.copyOf(((FDEBLayoutData) edge.getEdgeData().getLayoutData()).subdivisionPoints,
                        ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).subdivisionPoints.length);
            }

            for (Edge edge : graphModel.getGraph().getEdges()) {
                FDEBUtilities.updateNewSubdivisionPoints(edge, sprintConstant, stepSize);
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
    public LayoutProperty[] getProperties() {
        return new LayoutProperty[0];
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

    private void createCompatibilityLists() {
        ArrayList<FDEBCompatibilityRecord> similar = new ArrayList<FDEBCompatibilityRecord>();
        for (Edge edge : graphModel.getGraph().getEdges()) {
            FDEBUtilities.createCompatibilityRecords(edge, compatibilityThreshold, graphModel.getGraph());
        }
        int totalEdges = graphModel.getGraph().getEdgeCount() * graphModel.getGraph().getEdgeCount();
        int passedEdges = 0;
        double csum = 0;
        for (Edge edge : graphModel.getGraph().getEdges()) {
            if (cancel) {
                return;
            }
            passedEdges += ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).similarEdges.length;
            for (FDEBCompatibilityRecord record : ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).similarEdges) {
                csum += record.compatibility;
            }
        }
        System.err.println("total: " + totalEdges + " passed " + passedEdges + " sum of compatibility " + csum
                + " fraction " + ((double) passedEdges) / totalEdges);
    }

    @Override
    public void modifyAlgo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean cancel() {
        this.cancel = true;
        this.setConverged(true);
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
