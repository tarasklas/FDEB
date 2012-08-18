/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.bundler;

import java.awt.geom.Point2D;
import java.util.*;
import org.gephi.edgelayout.spi.*;
import org.gephi.fdeb.FDEBLayoutData;
import org.gephi.fdeb.utils.FDEBCompatibilityComputator;
import org.gephi.fdeb.utils.FDEBUtilities;
import org.gephi.graph.api.Edge;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperty;
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
    public static final String SPRING_CONSTANT_OPTIONS = "Spring constant options";
    //settable variables
    protected double stepSize, stepSizeAtTheBeginning; //S
    protected double iterationsPerCycle, iterationsPerCycleAtTheBeginning;//I
    protected double springConstant;//K
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
    protected boolean useLowMemoryMode;
    protected double iterationIncreaseRate;

    public FDEBAbstractBundler(EdgeLayoutBuilder layoutBuilder) {
        super(layoutBuilder);
        resetPropertiesValues();
    }

    @Override
    public void resetPropertiesValues() {
        numCycles = 6;
        stepSizeAtTheBeginning = graphModel != null ? FDEBUtilities.calculateInitialStepSize(graphModel.getGraph()) : 1;
        iterationsPerCycleAtTheBeginning = 50;
        springConstant = 0.1;
        stepDampingFactor = 0.5;
        compatibilityThreshold = 0.5;
        subdivisionPointIncreaseRate = 2;
        useInverseQuadraticModel = false;
        useLowMemoryMode = false;
        computator = new FDEBCompatibilityComputator();
        iterationIncreaseRate = 0.667;
    }

    @Override
    public void removeLayoutData() {
        for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
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

    public Double getIterationsPerCycle() {
        return iterationsPerCycleAtTheBeginning;
    }

    public void setIterationsPerCycle(Double iterationsPerCycle) {
        iterationsPerCycle = iterationsPerCycle > 0 ? iterationsPerCycle : 1;
        this.iterationsPerCycle = iterationsPerCycle;
        this.iterationsPerCycleAtTheBeginning = iterationsPerCycle;
    }

    public Integer getNumCycles() {
        return numCycles;
    }

    public void setNumCycles(Integer numCycles) {
        numCycles = numCycles > 0 ? numCycles : 1;
        this.numCycles = numCycles;
    }

    public Double getSpringConstant() {
        return springConstant;
    }

    public void setSpringConstant(Double springConstant) {
        springConstant = springConstant >= 0 ? springConstant : 0;
        this.springConstant = springConstant;
    }

    public Double getStepDampingFactor() {
        return stepDampingFactor;
    }

    public void setStepDampingFactor(Double stepDampingFactor) {
        stepDampingFactor = stepDampingFactor >= 0 ? stepDampingFactor : 0;
        stepDampingFactor = stepDampingFactor <= 1 ? stepDampingFactor : 1;
        this.stepDampingFactor = stepDampingFactor;
    }

    public Double getStepSize() {
        return stepSizeAtTheBeginning;
    }

    public void setStepSize(Double stepSize) {
        stepSize = stepSize >= 0 ? stepSize : 0;
        this.stepSize = stepSize;
        this.stepSizeAtTheBeginning = stepSize;
    }

    public Double getCompatibilityThreshold() {
        return compatibilityThreshold;
    }

    public void setCompatibilityThreshold(Double compatibilityThreshold) {
        compatibilityThreshold = compatibilityThreshold >= 0 ? compatibilityThreshold : 0;
        compatibilityThreshold = compatibilityThreshold <= 1 ? compatibilityThreshold : 1;
        this.compatibilityThreshold = compatibilityThreshold;
    }

    public Double getSubdivisionPointIncreaseRate() {
        return subdivisionPointIncreaseRate;
    }

    public void setSubdivisionPointIncreaseRate(Double subdivisionPointIncreaseRate) {
        subdivisionPointIncreaseRate = subdivisionPointIncreaseRate >= 1 ? subdivisionPointIncreaseRate : 1;
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

    public Boolean isUseLowMemoryMode() {
        return useLowMemoryMode;
    }

    public void setUseLowMemoryMode(Boolean useLowMemoryMode) {
        this.useLowMemoryMode = useLowMemoryMode;
    }

    public Double getIterationIncreaseRate() {
        return iterationIncreaseRate;
    }

    public void setIterationIncreaseRate(Double iterationIncreaseRate) {
        iterationIncreaseRate = iterationIncreaseRate >= 0 ? iterationIncreaseRate : 0;
        this.iterationIncreaseRate = iterationIncreaseRate;
    }

    @Override
    public EdgeLayoutProperty[] getProperties() {
        List<EdgeLayoutProperty> properties = new ArrayList<EdgeLayoutProperty>();
        try {
            properties.add(EdgeLayoutProperty.createProperty(this, Integer.class,
                    "Number of cycles",
                    GENERAL_OPTIONS, "",
                    "getNumCycles", "setNumCycles"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
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
                    "Spring constant",
                    SPRING_CONSTANT_OPTIONS, "Check \"Use user constant\" to change it",
                    "getSpringConstant", "setSpringConstant"));

//            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
//                    "Use user constant",
//                    SPRING_CONSTANT_OPTIONS, "Use user value for spring constant",
//                    "isUseUserConstant", "setUseUserConstant"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "Angle compatibility affected by direction",
                    COMPATIBLITY_OPTIONS, "",
                    "isAngleCompatibilityAffectedByDirection", "setAngleCompatibilityAffectedByDirection"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "Use inverse quadratic model",
                    GENERAL_OPTIONS, "",
                    "isUseInverseQuadraticModel", "setUseInverseQuadraticModel"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    "Low memory mode",
                    GENERAL_OPTIONS, "Do not store compatibility lists. Gives huge time increase, use only when gephi crashes without it.",
                    "isUseLowMemoryMode", "setUseLowMemoryMode"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    "Iteration increase rate",
                    GENERAL_OPTIONS, "Recommened <= 1",
                    "getIterationIncreaseRate", "setIterationIncreaseRate"));

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
        for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
            if (cancel) {
                break;
            }
            FDEBLayoutData data = edge.getEdgeData().getLayoutData();
            intensity.add(data.intensity + 1);
            if (Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getBooleanValue(PreviewProperty.EDGE_LAYOUT_PRECALCULATE_POINTS)) {
                for (double x : data.intensities) {
                    intensities.add(x);
                }
            }
            maxIntensity = Math.max(maxIntensity, data.intensity + 1);

        }
        if (Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getBooleanValue(PreviewProperty.EDGE_LAYOUT_PRECALCULATE_POINTS)) {
            Collections.sort(intensities);
        }
        Collections.sort(intensity);
        double threshold = intensity.get((int) Math.min(intensity.size() - 1, intensity.size()
                * Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getDoubleValue("subdividededge.threshold")));

        int x = intensity.size();
        System.err.println("boundaries: " + intensity.get(x / 5) + " " + intensity.get(x * 2 / 5) + " " + intensity.get(x * 3 / 5) + " " + intensity.get(x * 4 / 5));
        System.err.println("threshold " + threshold);
        for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
            if (cancel) {
                break;
            }
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
            for (Edge edge1 : graphModel.getGraph().getEdges().toArray()) {
                if (cancel) {
                    break;
                }
                FDEBLayoutData data1 = edge1.getEdgeData().getLayoutData();
                if (data1.intensities == null || data1.intensities.length != data1.getSubdivisonPoints().length) {
                    data1.intensities = new double[data1.getSubdivisonPoints().length];
                } else {
                    Arrays.fill(data1.intensities, 0.0);
                }
            }
            fastIntensityCalculation();
            System.err.println("end algo!2 " + System.currentTimeMillis());
        }
        modifyAlgo();
        System.err.println("I AM DONE " + System.currentTimeMillis());
    }
    
    protected void createCompatibilityLists() {
        if (useLowMemoryMode) {
            return;
        }
//        for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
//            if (cancel) {
//                return;
//            }
//            FDEBUtilities.createCompatibilityRecords(edge, compatibilityThreshold, graphModel.getGraph(), computator);
//        }
        
        FDEBUtilities.createCompatibilityRecords(compatibilityThreshold, graphModel.getGraph(), computator);
//        
//        int totalEdges = graphModel.getGraph().getEdgeCount() * graphModel.getGraph().getEdgeCount();
//        int passedEdges = 0;
//        double csum = 0;
//        for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
//            if (cancel) {
//                return;
//            }
//            passedEdges += ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).similarEdges.length;
//            for (FDEBCompatibilityRecord record : ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).similarEdges) {
//                csum += record.compatibility;
//            }
//        }
//        System.err.println("total: " + totalEdges + " passed " + passedEdges + " sum of compatibility " + csum
//                + " fraction " + ((double) passedEdges) / totalEdges);
    }

    private void slowIntensityCalculation() {
        for (Edge edge1 : graphModel.getGraph().getEdges().toArray()) {
            if (cancel) {
                break;
            }
            for (Edge edge2 : graphModel.getGraph().getEdges().toArray()) {
                if (cancel) {
                    break;
                }
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
                            if (distance <= radius1 * 2) {
                                data1.intensities[i]++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void fastIntensityCalculation() {
        ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
        for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
            points.addAll(Arrays.asList(((EdgeLayoutData) edge.getEdgeData().getLayoutData()).getSubdivisonPoints()));
        }
        double minX = Integer.MAX_VALUE;
        double minY = Integer.MAX_VALUE;
        double maxX = Integer.MIN_VALUE;
        double maxY = Integer.MIN_VALUE;
        for (Point2D.Double point : points) {
            minX = Math.min(minX, point.x);
            maxX = Math.max(maxX, point.x);

            minY = Math.min(minY, point.y);
            maxY = Math.max(maxY, point.y);
        }
        QuadNode root = new QuadNode(points.toArray(new Point2D.Double[0]));
        for (Edge edge1 : graphModel.getGraph().getEdges().toArray()) {
            FDEBLayoutData data1 = edge1.getEdgeData().getLayoutData();
            Point2D.Double[] points1 = data1.getSubdivisonPoints();
            for (int i = 0; i < points1.length - 1; i++) {
                double xm = (points1[i].x + points1[i + 1].x) / 2;
                double ym = (points1[i].y + points1[i + 1].y) / 2;
                double radius = Point2D.Double.distance(points1[i].x, points1[i].y, points1[i + 1].x, points1[i + 1].y);
                data1.intensities[i] = root.getNumberOfPointsInRange(points1[i], 2 * getRadius(points1, i)) - 4;
            }
        }
    }

    protected double getRadius(Point2D.Double[] points, int i) {
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