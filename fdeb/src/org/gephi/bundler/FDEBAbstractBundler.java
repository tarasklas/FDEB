/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.bundler;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JPanel;
import org.gephi.edgelayout.api.EdgeLayoutController;
import org.gephi.edgelayout.spi.*;
import org.gephi.fdeb.FDEBLayoutData;
import org.gephi.fdeb.utils.FDEBCompatibilityComputator;
import org.gephi.graph.api.Edge;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.ui.components.gradientslider.GradientSlider;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author megaterik
 */
public abstract class FDEBAbstractBundler extends AbstractEdgeLayout implements EdgeLayout, LongTask {

    public static final String GENERAL_OPTIONS = "General options";
    public static final String COMPATIBLITY_OPTIONS = "Compatibility options";
    public static final String SPRINT_CONSTANT_OPTIONS = "Sprint constant options";
    //settable variables
    protected double stepSize, stepSizeAtTheBeginning; //S
    protected int iterationsPerCycle, iterationsPerCycleAtTheBeginning;//I
    protected double sprintConstant;//K
    protected double compatibilityThreshold;
    protected int numCycles;
    protected double stepDampingFactor;
    protected double subdivisionPointIncreaseRate;
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
        numCycles = 6;
        stepSizeAtTheBeginning = 0.4;
        iterationsPerCycleAtTheBeginning = 50;
        //sprintConstant = 0.5;
        stepDampingFactor = 0.5;
        compatibilityThreshold = 0;
        subdivisionPointIncreaseRate = 2;
        useInverseQuadraticModel = false;
        computator = new FDEBCompatibilityComputator();

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
                    GENERAL_OPTIONS, "",
                    "getNumCycles", "setNumCycles"));

            properties.add(EdgeLayoutProperty.createProperty(this, Integer.class,
                    "Iterations per cycle",
                    GENERAL_OPTIONS, "Iterations in the first cycle",
                    "getIterationsPerCycle", "setIterationsPerCycle"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    "Step damping factor",
                    GENERAL_OPTIONS, "Step damping factor every cycle iteration",
                    "getStepDampingFactor", "setStepDampingFactor"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    "Step size",
                    GENERAL_OPTIONS, "Step size at the beginning",
                    "getStepSize", "setStepSize"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    "Compatibility threshold",
                    GENERAL_OPTIONS, "Ignore edges with compatibility <= threshold",
                    "getCompatibilityThreshold", "setCompatibilityThreshold"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    "Subdivision point increase rate",
                    GENERAL_OPTIONS, "Subdivision points increase rate every cycle iteration",
                    "getSubdivisionPointIncreaseRate", "setSubdivisionPointIncreaseRate"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "Angle compatibility",
                    COMPATIBLITY_OPTIONS, "",
                    "isAngleCompatibility", "setAngleCompatibility"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "Scale compatibility",
                    COMPATIBLITY_OPTIONS, "",
                    "isScaleCompatibility", "setScaleCompatibility"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "Position compatibility",
                    COMPATIBLITY_OPTIONS, "",
                    "isPositionCompatibility", "setPositionCompatibility"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "Visibility compatibility",
                    COMPATIBLITY_OPTIONS, "",
                    "isVisibilityCompatibility", "setVisibilityCompatibility"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "Apply visibility for similar edges",
                    COMPATIBLITY_OPTIONS, "Apply visibility compatibility only when compatibility >= 0.9",
                    "isVisibilityApply", "setVisibilityApply"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    "Sprint constant",
                    SPRINT_CONSTANT_OPTIONS, "Check \"Use user contant\" to change it",
                    "getSprintConstant", "setSprintConstant"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "Use user contant",
                    SPRINT_CONSTANT_OPTIONS, "Use user value for sprint constant",
                    "isUseUserConstant", "setUseUserConstant"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "Angle compatibility affected by direction",
                    COMPATIBLITY_OPTIONS, "",
                    "isAngleCompatibilityAffectedByDirection", "setAngleCompatibilityAffectedByDirection"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "Use inverse quadratic model",
                    GENERAL_OPTIONS, "",
                    "isUseInverseQuadraticModel", "setUseInverseQuadraticModel"));

        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return properties.toArray(new EdgeLayoutProperty[0]);
    }

    @Override
    public boolean shouldRefreshPreview(int refreshRate) {
        if (refreshRate <= 0) {
            return true;
        } else {
            return (cycle % refreshRate == 0);
        }
    }

    @Override
    public void modifyAlgo() {
        double maxIntensity = 0;
        ArrayList<Double> intensity = new ArrayList<Double>();
        ArrayList intensities = new ArrayList();
        for (Edge edge : graphModel.getGraph().getEdges()) {
            FDEBLayoutData data = edge.getEdgeData().getLayoutData();
            intensity.add(data.intensity + 1);
            if (Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getBooleanValue(PreviewProperty.EDGE_LAYOUT_PRECALCULATE_POINTS)) {
                for (double x : data.intensities) {
                    intensities.add(x);
                }
            }
            maxIntensity = Math.max(maxIntensity, data.intensity + 1);
        }
        Collections.sort(intensity);
        double threshold = intensity.get((int) Math.min(intensity.size() - 1, intensity.size()
                * Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getDoubleValue("subdividededge.threshold")));

        int x = intensity.size();
        System.err.println("boundaries: " + intensity.get(x / 5) + " " + intensity.get(x * 2 / 5) + " " + intensity.get(x * 3 / 5) + " " + intensity.get(x * 4 / 5));
        System.err.println("threshold " + threshold);
        for (Edge edge : graphModel.getGraph().getEdges()) {
            FDEBLayoutData data = edge.getEdgeData().getLayoutData();
            data.updateColor(intensity);
            if (Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getBooleanValue(PreviewProperty.EDGE_LAYOUT_PRECALCULATE_POINTS)) {
                data.updateColors(intensities);
            }
        }
        System.err.println("modifycomplete");
    }

    @Override
    public void endAlgo() {
        if (Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getBooleanValue(PreviewProperty.EDGE_LAYOUT_PRECALCULATE_POINTS)) {
            System.err.println("end algo!1 " + System.currentTimeMillis());
            for (Edge edge1 : graphModel.getGraph().getEdges()) {
                FDEBLayoutData data1 = edge1.getEdgeData().getLayoutData();
                if (data1.intensities == null || data1.intensities.length != data1.getSubdivisonPoints().length) {
                    data1.intensities = new double[data1.getSubdivisonPoints().length];
                } else {
                    Arrays.fill(data1.intensities, 0.0);
                }
            }
            for (Edge edge1 : graphModel.getGraph().getEdges()) {
                for (Edge edge2 : graphModel.getGraph().getEdges()) {
                    if (edge1.getEdgeData().getLayoutData() != null && edge2.getEdgeData().getLayoutData() != null && edge1 != edge2) {
                        FDEBLayoutData data1 = edge1.getEdgeData().getLayoutData();
                        FDEBLayoutData data2 = edge2.getEdgeData().getLayoutData();
                        Point2D.Double[] points1 = data1.getSubdivisonPoints();
                        Point2D.Double[] points2 = data2.getSubdivisonPoints();
                        for (int i = 0; i < points1.length; i++) {
                            for (int j = 0; j < points2.length; j++) {
                                double distance = Point2D.Double.distance(points1[i].x, points1[i].y, points2[j].x, points2[j].y);
                                double radius1 = getRadius(points1, i);
                                double radius2 = getRadius(points2, j);
                                if (distance <= radius1 + radius2) {
                                    data1.intensities[i]++;
                                }
                            }
                        }
                    }
                }
            }
            System.err.println("end algo!2 " + System.currentTimeMillis());
        }
        modifyAlgo();
    }

    private double getRadius(Point2D.Double[] points, int i) {
        if (i == 0) {
            return Point2D.Double.distance(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y);
        } else if (i == points.length - 1) {
            return Point2D.Double.distance(points[i].x, points[i].y, points[i - 1].x, points[i - 1].y);
        } else {
            return (Point2D.Double.distance(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y)
                    + Point2D.Double.distance(points[i].x, points[i].y, points[i - 1].x, points[i - 1].y)) / 2;
        }
    }
}
