package org.gephi.bundler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.gephi.edgelayout.spi.AbstractEdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayoutBuilder;
import org.gephi.edgelayout.spi.EdgeLayoutProperty;
import org.gephi.fdeb.FDEBCompatibilityRecord;
import org.gephi.fdeb.FDEBLayoutData;
import org.gephi.fdeb.demo.multithreading.FDEBCompatibilityRecordsTask;
import org.gephi.fdeb.demo.multithreading.FDEBForceCalculationTask;
import org.gephi.fdeb.utils.FDEBUtilities;
import org.gephi.graph.api.Edge;
import org.openide.util.Exceptions;

/**
 *
 * @author megaterik
 */
public class FDEBBundlerMultithreading extends AbstractEdgeLayout implements EdgeLayout {

    private static final double EPS = 1e-7;
    private int cycle;
    private double subdivisionPointsPerEdge;

    public FDEBBundlerMultithreading(EdgeLayoutBuilder layoutBuilder) {
        super(layoutBuilder);
        resetPropertiesValues();
    }

    /*
     * Get parameters and init structures
     */
    int numberOfTasks = 8;
    ExecutorService executor;

    @Override
    public void initAlgo() {
        executor = Executors.newCachedThreadPool();

        for (Edge edge : graphModel.getGraph().getEdges()) {
            edge.getEdgeData().setLayoutData(
                    new FDEBLayoutData(edge.getSource().getNodeData().x(), edge.getSource().getNodeData().y(),
                    edge.getTarget().getNodeData().x(), edge.getTarget().getNodeData().y()));
        }
        cycle = 1;
        setConverged(false);
        sprintConstant = FDEBUtilities.calculateSprintConstant(graphModel.getGraph());
        stepSize = stepSizeAtTheBeginning;
        iterationsPerCycle = iterationsPerCycleAtTheBeginning;
        subdivisionPointsPerEdge = 1;//start and end doesnt count
        System.out.println("K " + sprintConstant);

        createCompatibilityLists();
    }

    /*
     * Use similar method to ForceAtlas-2
     */
    @Override
    public void goAlgo() {

        for (Edge edge : graphModel.getGraph().getEdges()) {
            ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).newSubdivisionPoints = Arrays.copyOf(((FDEBLayoutData) edge.getEdgeData().getLayoutData()).subdivisionPoints,
                    ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).subdivisionPoints.length);
        }

        System.err.println("Next iteration");
        for (int step = 0; step < iterationsPerCycle; step++) {

            Future[] calculationTasks = new Future[numberOfTasks];
            int cedges = graphModel.getGraph().getEdgeCount();
            Edge[] edges = graphModel.getGraph().getEdges().toArray();
            for (int i = 0; i < numberOfTasks; i++) {
                calculationTasks[i] = executor.submit(new FDEBForceCalculationTask(edges, cedges * i / numberOfTasks,
                        Math.min(cedges, cedges * (i + 1) / numberOfTasks), sprintConstant, stepSize));
            }

            for (int i = 0; i < calculationTasks.length; i++) {
                try {
                    if (calculationTasks[i] == null) {
                        System.err.println("o_O");
                    }
                    calculationTasks[i].get();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            for (Edge edge : edges) {
                FDEBLayoutData data = edge.getEdgeData().getLayoutData();
                System.arraycopy(data.newSubdivisionPoints, 0, data.subdivisionPoints, 0, data.newSubdivisionPoints.length);
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
        executor.shutdown();
    }

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
        Future[] tasks = new Future[numberOfTasks];
        int cedges = graphModel.getGraph().getEdgeCount();
        Edge[] edges = graphModel.getGraph().getEdges().toArray();
        for (int i = 0; i < numberOfTasks; i++) {
            tasks[i] = executor.submit(new FDEBCompatibilityRecordsTask(edges, cedges * i / numberOfTasks,
                    Math.min(cedges, cedges * (i + 1) / numberOfTasks), compatibilityThreshold, graphModel.getGraph()));
        }

        for (int i = 0; i < numberOfTasks; i++) {
            try {
                tasks[i].get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }


        int totalEdges = graphModel.getGraph().getEdgeCount() * graphModel.getGraph().getEdgeCount();
        int passedEdges = 0;
        for (Edge edge : graphModel.getGraph().getEdges()) {
            if (((FDEBLayoutData) edge.getEdgeData().getLayoutData()).similarEdges != null) {
                passedEdges += ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).similarEdges.length;
            }
        }
        System.err.println("total: " + totalEdges + " passed " + passedEdges
                + " fraction " + ((double) passedEdges) / totalEdges);
    }

    @Override
    public void modifyAlgo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    ///////////////////////////////////////
    private double stepSize, stepSizeAtTheBeginning; //S
    private int iterationsPerCycle, iterationsPerCycleAtTheBeginning;//I
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
    public EdgeLayoutProperty[] getProperties() {
        List<EdgeLayoutProperty> properties = new ArrayList<EdgeLayoutProperty>();
        /*
         * try { properties.add(EdgeLayoutProperty.createProperty( this,
         * Double.class, "tratata", null, "angle", "this is tratata",
         * "getAngle", "setAngle")); } catch (Exception e) {
         * e.printStackTrace(); }
         */
        try {
            properties.add(EdgeLayoutProperty.createProperty(this, Integer.class,
                    "Number of cycles",
                    null,
                    null,
                    "getNumCycles", "setNumCycles"));

            properties.add(EdgeLayoutProperty.createProperty(this, Integer.class,
                    "iterationsPerCycle",
                    null, null,
                    "getIterationsPerCycle", "setIterationsPerCycle"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    "StepDampingFactor",
                    null, null,
                    "getStepDampingFactor", "setStepDampingFactor"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    "stepSize",
                    null, null,
                    "getStepSize", "setStepSize"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    "compatibilityThreshold",
                    null, null,
                    "getCompatibilityThreshold", "setCompatibilityThreshold"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    "SubdivisionPointIncreaseRate",
                    null, null,
                    "getSubdivisionPointIncreaseRate", "setSubdivisionPointIncreaseRate"));

        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return properties.toArray(new EdgeLayoutProperty[0]);
    }
}
