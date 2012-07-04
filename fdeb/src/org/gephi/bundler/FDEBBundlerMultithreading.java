package org.gephi.bundler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.gephi.edgelayout.plugin.AbstractEdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayoutBuilder;
import org.gephi.fdeb.FDEBBundlerParameters;
import org.gephi.fdeb.FDEBCompatibilityRecord;
import org.gephi.fdeb.FDEBLayoutData;
import org.gephi.fdeb.demo.multithreading.FDEBCompatibilityRecordsTask;
import org.gephi.fdeb.demo.multithreading.FDEBForceCalculationTask;
import org.gephi.fdeb.utils.FDEBUtilities;
import org.gephi.graph.api.Edge;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.Exceptions;

/**
 *
 * @author megaterik
 */
public class FDEBBundlerMultithreading extends AbstractEdgeLayout implements EdgeLayout {
    
    private static final double EPS = 1e-7;
    private int cycle;
    private double stepSize;   // S
    private int iterationsPerCycle;    // I
    private double sprintConstant; // K
    private double compatibilityThreshold;
    private FDEBBundlerParameters parameters;
    private double subdivisionPointsPerEdge;
    
    public FDEBBundlerMultithreading(EdgeLayoutBuilder layoutBuilder, FDEBBundlerParameters parameters) {
        super(layoutBuilder);
        this.parameters = parameters;
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
        stepSize = parameters.getStepSize();
        sprintConstant = parameters.getSprintConstant();
        iterationsPerCycle = parameters.getIterationsPerCycle();
        compatibilityThreshold = parameters.getCompatibilityThreshold();
        sprintConstant = FDEBUtilities.calculateSprintConstant(graphModel.getGraph());
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
        executor.shutdown();
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
}
