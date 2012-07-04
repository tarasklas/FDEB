package org.gephi.bundler;

import org.gephi.fdeb.utils.FDEBUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.gephi.edgelayout.plugin.AbstractEdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayoutBuilder;
import org.gephi.fdeb.FDEBBundlerParameters;
import org.gephi.fdeb.FDEBCompatibilityRecord;
import org.gephi.fdeb.FDEBLayoutData;
import org.gephi.graph.api.Edge;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;

/**
 *
 * @author megaterik
 */
public class FDEBBundler extends AbstractEdgeLayout implements EdgeLayout, LongTask {

    private static final double EPS = 1e-7;
    private int cycle;
    private double subdivisionPointsPerEdge;
    private ProgressTicket progressTicket;
    private boolean cancel;

    public FDEBBundler(EdgeLayoutBuilder layoutBuilder) {
        super(layoutBuilder);
        resetPropertiesValues();
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
        sprintConstant = FDEBUtilities.calculateSprintConstant(graphModel.getGraph());
        subdivisionPointsPerEdge = 1;//start and end doesnt count
        stepSize = stepSizeAtTheBeginning;
        iterationsPerCycle = iterationsPerCycleAtTheBeginning;
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
                if (cancel)
                    return;
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

        if (cycle == numCycles) {
            setConverged(true);
        } else {
            prepareForTheNextStep();
        }
    }

    void prepareForTheNextStep() {
        cycle++;
        stepSize *= (1.0 - stepDampingFactor);
        iterationsPerCycle = (iterationsPerCycle * 2) / 3;
        divideEdges();
    }

    void divideEdges() {
        subdivisionPointsPerEdge *= subdivisionPointIncreaseRate;
        for (Edge edge : graphModel.getGraph().getEdges()) {
            FDEBUtilities.divideEdge(edge, subdivisionPointsPerEdge);
        }
    }

    @Override
    public void endAlgo() {
    }

    /*
     * Reflection doesn't work with inner classes, so as a temponary solution I
     * have moved bundler parameters to bundler Probably I will make an static
     * class with default parameters for all bundlers, or abstract class to
     * extend by all bundlers
     */
    @Override
    public void resetPropertiesValues() {
        numCycles = 10;
        stepSizeAtTheBeginning = 0.1;
        iterationsPerCycleAtTheBeginning = 1000;
        //sprintConstant = 0.5;
        stepDampingFactor = 0.5;
        compatibilityThreshold = 0.1;
        subdivisionPointIncreaseRate = 1.6;
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
    ///////////////////////////////////////
    private double stepSize,stepSizeAtTheBeginning; //S
    private int iterationsPerCycle,iterationsPerCycleAtTheBeginning;//I
    private double sprintConstant;//K
    private double compatibilityThreshold;
    private int numCycles;
    private double stepDampingFactor;
    private double subdivisionPointIncreaseRate;

    public Integer getIterationsPerCycle() {
        return iterationsPerCycleAtTheBeginning;
    }

    public void setIterationsPerCycle(Integer iterationsPerCycle) {
        this.iterationsPerCycle = iterationsPerCycle;
        this.iterationsPerCycleAtTheBeginning = iterationsPerCycle;
    }

    public Integer getNumCycles() {
        return numCycles;
    }

    public void setNumCycles(Integer numCycles) {
        this.numCycles = numCycles;
    }

    public Double getSprintConstant() {
        return sprintConstant;
    }

    public void setSprintConstant(Double sprintConstant) {
        this.sprintConstant = sprintConstant;
    }

    public Double getStepDampingFactor() {
        return stepDampingFactor;
    }

    public void setStepDampingFactor(Double stepDampingFactor) {
        this.stepDampingFactor = stepDampingFactor;
    }

    public Double getStepSize() {
        return stepSizeAtTheBeginning;
    }

    public void setStepSize(Double stepSize) {
        this.stepSize = stepSize;
        this.stepSizeAtTheBeginning = stepSize;
    }

    public Double getCompatibilityThreshold() {
        return compatibilityThreshold;
    }

    public void setCompatibilityThreshold(Double compatibilityThreshold) {
        this.compatibilityThreshold = compatibilityThreshold;
    }

    public Double getSubdivisionPointIncreaseRate() {
        return subdivisionPointIncreaseRate;
    }

    public void setSubdivisionPointIncreaseRate(Double subdivisionPointIncreaseRate) {
        this.subdivisionPointIncreaseRate = subdivisionPointIncreaseRate;
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        /*
         * try { properties.add(LayoutProperty.createProperty( this,
         * Double.class, "tratata", null, "angle", "this is tratata",
         * "getAngle", "setAngle")); } catch (Exception e) {
         * e.printStackTrace(); }
         */
        try {
            properties.add(LayoutProperty.createProperty(this, Integer.class,
                    "Number of cycles",
                    null,
                    null,
                    "getNumCycles", "setNumCycles"));

            properties.add(LayoutProperty.createProperty(this, Integer.class,
                    "iterationsPerCycle",
                    null, null,
                    "getIterationsPerCycle", "setIterationsPerCycle"));

            properties.add(LayoutProperty.createProperty(this, Double.class,
                    "StepDampingFactor",
                    null, null,
                    "getStepDampingFactor", "setStepDampingFactor"));

            properties.add(LayoutProperty.createProperty(this, Double.class,
                    "stepSize",
                    null, null,
                    "getStepSize", "setStepSize"));

            properties.add(LayoutProperty.createProperty(this, Double.class,
                    "compatibilityThreshold",
                    null, null,
                    "getCompatibilityThreshold", "setCompatibilityThreshold"));
            
            properties.add(LayoutProperty.createProperty(this, Double.class, 
                    "SubdivisionPointIncreaseRate",
                    null, null,
                    "getSubdivisionPointIncreaseRate", "setSubdivisionPointIncreaseRate"));

        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return properties.toArray(new LayoutProperty[0]);
    }
}
