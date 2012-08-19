/*
 Copyright 2008-2012 Gephi
 Authors : Taras Klaskovsky <megaterik@gmail.com>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
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
 * @author Taras Klaskovsky <megaterik@gmail.com>
 */
public abstract class FDEBAbstractBundler extends AbstractEdgeLayout implements EdgeLayout, LongTask {

    public static final String GENERAL_OPTIONS = org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "general_options");
    public static final String COMPATIBLITY_OPTIONS = org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "compatibility_options");
    public static final String SPRING_CONSTANT_OPTIONS = org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "spring_constant_options");
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
                    org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "number_of_cycles.name"),
                    GENERAL_OPTIONS, org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "number_of_cycles.desc"),
                    "getNumCycles", "setNumCycles"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "iterations_per_cycle.name"),
                    GENERAL_OPTIONS, org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "iterations_per_cycle.desc"),
                    "getIterationsPerCycle", "setIterationsPerCycle"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "step_damping_factor.name"),
                    GENERAL_OPTIONS, org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "step_damping_factor.desc"),
                    "getStepDampingFactor", "setStepDampingFactor"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "step_size.name"),
                    GENERAL_OPTIONS, org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "step_size.desc"),
                    "getStepSize", "setStepSize"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "compatibility_threshold.name"),
                    GENERAL_OPTIONS, org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "compatibility_threshold.desc"),
                    "getCompatibilityThreshold", "setCompatibilityThreshold"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "subdivision_points_increase_rate.name"),
                    GENERAL_OPTIONS, org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "subdivision_points_increase_rate.desc"),
                    "getSubdivisionPointIncreaseRate", "setSubdivisionPointIncreaseRate"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "angle_compatibility.name"),
                    COMPATIBLITY_OPTIONS, org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "angle_compatibility.desc"),
                    "isAngleCompatibility", "setAngleCompatibility"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "scale_compatibility.name"),
                    COMPATIBLITY_OPTIONS, org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "scale.compatibility.desc"),
                    "isScaleCompatibility", "setScaleCompatibility"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "position_compatibility.name"),
                    COMPATIBLITY_OPTIONS, org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "position_compatibility.desc"),
                    "isPositionCompatibility", "setPositionCompatibility"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "visibility_compatibility.name"),
                    COMPATIBLITY_OPTIONS, org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "visibility_compatibility.desc"),
                    "isVisibilityCompatibility", "setVisibilityCompatibility"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "apply_visibility_for_similar_edges.name"),
                    COMPATIBLITY_OPTIONS, org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "apply_visibility_for_similar_edges.desc"),
                    "isVisibilityApply", "setVisibilityApply"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "spring_constant.name"),
                    SPRING_CONSTANT_OPTIONS, org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "spring_constant.desc"),
                    "getSpringConstant", "setSpringConstant"));

//            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
//                    "Use user constant",
//                    SPRING_CONSTANT_OPTIONS, "Use user value for spring constant",
//                    "isUseUserConstant", "setUseUserConstant"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "angle_compatibility_affected.name"),
                    COMPATIBLITY_OPTIONS, org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "angle_compatibility_affected.desc"),
                    "isAngleCompatibilityAffectedByDirection", "setAngleCompatibilityAffectedByDirection"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "inverse_quadratic_model.name"),
                    GENERAL_OPTIONS, org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "inverse_quadratic_model.desc"),
                    "isUseInverseQuadraticModel", "setUseInverseQuadraticModel"));

            properties.add(EdgeLayoutProperty.createProperty(this, Boolean.class,
                    org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "low_memory_mode.name"),
                    GENERAL_OPTIONS, org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "low_memory_mode.desc"),
                    "isUseLowMemoryMode", "setUseLowMemoryMode"));

            properties.add(EdgeLayoutProperty.createProperty(this, Double.class,
                    org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "iteration_increase_rate.name"),
                    GENERAL_OPTIONS, org.openide.util.NbBundle.getMessage(FDEBAbstractBundler.class, "iteration_increase_rate.desc"),
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
    }

    @Override
    public void endAlgo() {
        if (Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getBooleanValue(PreviewProperty.EDGE_LAYOUT_PRECALCULATE_POINTS)) {
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
        }
        modifyAlgo();
    }

    protected void createCompatibilityLists() {
        if (useLowMemoryMode) {
            return;
        }
        FDEBUtilities.createCompatibilityRecords(compatibilityThreshold, graphModel.getGraph(), computator);
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

    protected int calculateSumOfIterations(double iterationsPerCycle, int numCycles, double iterationsIncreaseRate, double subdivisionPoints, double subdivisionPointsIncreaseRate) {
        int res = 0;
        while (numCycles-- > 0) {
            res += ((int) iterationsPerCycle) * ((int) subdivisionPoints);
            iterationsPerCycle *= iterationsIncreaseRate;
            subdivisionPoints *= subdivisionPointsIncreaseRate;
        }
        return res;
    }
}