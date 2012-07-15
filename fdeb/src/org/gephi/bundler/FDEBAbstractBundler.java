/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.bundler;

import java.util.ArrayList;
import java.util.List;
import org.gephi.edgelayout.spi.AbstractEdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayoutBuilder;
import org.gephi.edgelayout.spi.EdgeLayoutProperty;
import org.gephi.fdeb.utils.FDEBCompatibilityComputator;
import org.gephi.graph.api.Edge;
import org.gephi.preview.api.PreviewController;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author megaterik
 */
public abstract class FDEBAbstractBundler extends AbstractEdgeLayout implements EdgeLayout, LongTask {

    //settable variables
    protected double stepSize, stepSizeAtTheBeginning; //S
    protected int iterationsPerCycle, iterationsPerCycleAtTheBeginning;//I
    protected double sprintConstant;//K
    protected double compatibilityThreshold;
    protected int numCycles;
    protected double stepDampingFactor;
    protected double subdivisionPointIncreaseRate;
    protected int refreshRate;
    protected boolean useUserConstant;
    //inner variables
    protected static final double EPS = 1e-7;
    protected int cycle;
    protected double subdivisionPointsPerEdge;
    protected ProgressTicket progressTicket;
    protected FDEBCompatibilityComputator computator;
    protected boolean cancel;
    protected boolean useInverseQuadraticModel;

    public FDEBAbstractBundler(EdgeLayoutBuilder layoutBuilder) {
        super(layoutBuilder);
        resetPropertiesValues();
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
        refreshRate = 1;
        useInverseQuadraticModel = false;
        computator = new FDEBCompatibilityComputator();

        Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().putValue("subdividededge.alpha", 0.1);
        Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().putValue("subdividededge.thickness", 1.0);
    }

    @Override
    public void removeLayoutData() {
        for (Edge edge : graphModel.getGraph().getEdges()) {
            edge.getEdgeData().setLayoutData(null);
        }
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

    /*
     * Getters and setters for reflection in properties
     */
    public Boolean isAngleCompatibility() {
        return computator.isAngleCompatibility();
    }

    public void setAngleCompatibility(Boolean angleCompatibility) {
        computator.setAngleCompatibility(angleCompatibility);
    }

    public Boolean isPositionCompatibility() {
        return computator.isPositionCompatibility();
    }

    public void setPositionCompatibility(Boolean positionCompatibility) {
        computator.setPositionCompatibility(positionCompatibility);
    }

    public Boolean isScaleCompatibility() {
        return computator.isScaleCompatibility();
    }

    public void setScaleCompatibility(Boolean scaleCompatibility) {
        computator.setScaleCompatibility(scaleCompatibility);
    }

    public Boolean isVisibilityCompatibility() {
        return computator.isVisibilityCompatibility();
    }

    public void setVisibilityCompatibility(Boolean visibilityCompatibility) {
        computator.setVisibilityCompatibility(visibilityCompatibility);
    }

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

    public Integer getRefreshRate() {
        return refreshRate;
    }

    public void setRefreshRate(Integer refreshRate) {
        this.refreshRate = refreshRate;
    }

    public Boolean isVisibilityApply() {
        return computator.isVisibilityApply();
    }

    public void setVisibilityApply(Boolean visibilityApply) {
        computator.setVisibilityApply(visibilityApply);
    }

    public Boolean isUseUserConstant() {
        return useUserConstant;
    }

    public void setUseUserConstant(Boolean useUserConstant) {
        this.useUserConstant = useUserConstant;
    }

    public void setThickness(Double thinkness) {
        Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().putValue("subdividededge.thickness", thinkness);
    }

    public Double getThickness() {
        return Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getDoubleValue("subdividededge.thickness");
    }

    public void setAlpha(Double alpha) {
        Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().putValue("subdividededge.alpha", alpha);
    }

    public Double getAlpha() {
        return Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getDoubleValue("subdividededge.alpha");
    }

    public Boolean isAngleCompatibilityAffectedByDirection() {
        return computator.isAffectedByDirection();
    }

    public void setAngleCompatibilityAffectedByDirection(Boolean angleCompatibilityAffectedByDirection) {
        computator.setAffectedByDirection(angleCompatibilityAffectedByDirection);
    }

    public void setUseInverseQuadraticModel(Boolean useInverseQuadraticModel) {
        this.useInverseQuadraticModel = useInverseQuadraticModel;
    }

    public Boolean isUseInverseQuadraticModel() {
        return useInverseQuadraticModel;
    }

    @Override
    public EdgeLayoutProperty[] getProperties() {
        List<EdgeLayoutProperty> properties = new ArrayList<EdgeLayoutProperty>();
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

            properties.add(EdgeLayoutProperty.createProperty(this, Integer.class,
                    "Refresh after every nth cycle",
                    null, null,
                    "getRefreshRate", "setRefreshRate"));


            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "Angle compatibility",
                    null, null,
                    "isAngleCompatibility", "setAngleCompatibility"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "Scale compatibility",
                    null, null,
                    "isScaleCompatibility", "setScaleCompatibility"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "Position compatibility",
                    null, null,
                    "isPositionCompatibility", "setPositionCompatibility"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "Visibility compatibility",
                    null, null,
                    "isVisibilityCompatibility", "setVisibilityCompatibility"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "Apply visibility compatibility only when everything else is high",
                    null, null,
                    "isVisibilityApply", "setVisibilityApply"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    "Set sprint constant",
                    null, null,
                    "getSprintConstant", "setSprintConstant"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "UseUserConstant",
                    null, null,
                    "isUseUserConstant", "setUseUserConstant"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    "Edge thinkness",
                    null, null,
                    "getThickness", "setThickness"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    "Edge transparency",
                    null, null,
                    "getAlpha", "setAlpha"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "angleCompatibilityAffectedByDirection",
                    null, null,
                    "isAngleCompatibilityAffectedByDirection", "setAngleCompatibilityAffectedByDirection"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "UseInverseQuadraticModel",
                    null, null,
                    "isUseInverseQuadraticModel", "setUseInverseQuadraticModel"));

        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return properties.toArray(new EdgeLayoutProperty[0]);
    }

    @Override
    public boolean shouldRefreshPreview() {
        if (refreshRate == 0) {
            return true;
        } else {
            return (cycle % refreshRate == 0);
        }
    }
}
