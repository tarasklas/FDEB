/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.fdeb;

/**
 *
 * @author megaterik
 */
public class FDEBBundlerParameters {

    private int subdivisionPointsPerEdge;//P
    private double stepSize; //S
    private int iterationsPerCycle;//I
    private double sprintConstant;//K
    private int numCycles;
    private double stepDampingFactor;
    private double edgeCompatibilityThreshold;
    private boolean directionAffectsCompatibility;
    private boolean binaryCompatibility;
    private boolean useInverseQuadraticModel;
    private boolean useRepulsionForOppositeEdges; // for compatible edges going into opposite directions
    private boolean useSimpleCompatibilityMeasure;
    private boolean edgeValueAffectsAttraction;
    private double repulsionAmount;
    private double subdivisionPointsCycleIncreaseRate;
    private boolean updateViewAfterEachStep = true;

    public FDEBBundlerParameters() {
        setParametersToDefault();
    }
    
    void setParametersToDefault() {
        numCycles = 10;
        subdivisionPointsPerEdge = 1;
        stepSize = 1.0;
        iterationsPerCycle = 100;
        sprintConstant = 0.5;
        repulsionAmount = 1.0;
        stepDampingFactor = 0.5;
        edgeCompatibilityThreshold = 0.60;
        directionAffectsCompatibility = true;
        binaryCompatibility = false;
        useInverseQuadraticModel = false;
        useRepulsionForOppositeEdges = false;
        useSimpleCompatibilityMeasure = false;
        edgeValueAffectsAttraction = false;
        subdivisionPointsCycleIncreaseRate = 1.3;
    }

    public boolean isBinaryCompatibility() {
        return binaryCompatibility;
    }

    public void setBinaryCompatibility(boolean binaryCompatibility) {
        this.binaryCompatibility = binaryCompatibility;
    }

    public boolean isDirectionAffectsCompatibility() {
        return directionAffectsCompatibility;
    }

    public void setDirectionAffectsCompatibility(boolean directionAffectsCompatibility) {
        this.directionAffectsCompatibility = directionAffectsCompatibility;
    }

    public double getEdgeCompatibilityThreshold() {
        return edgeCompatibilityThreshold;
    }

    public void setEdgeCompatibilityThreshold(double edgeCompatibilityThreshold) {
        this.edgeCompatibilityThreshold = edgeCompatibilityThreshold;
    }

    public boolean isEdgeValueAffectsAttraction() {
        return edgeValueAffectsAttraction;
    }

    public void setEdgeValueAffectsAttraction(boolean edgeValueAffectsAttraction) {
        this.edgeValueAffectsAttraction = edgeValueAffectsAttraction;
    }

    public int getIterationsPerCycle() {
        return iterationsPerCycle;
    }

    public void setIterationsPerCycle(int iterationsPerCycle) {
        this.iterationsPerCycle = iterationsPerCycle;
    }

    public int getNumCycles() {
        return numCycles;
    }

    public void setNumCycles(int numCycles) {
        this.numCycles = numCycles;
    }

    public double getRepulsionAmount() {
        return repulsionAmount;
    }

    public void setRepulsionAmount(double repulsionAmount) {
        this.repulsionAmount = repulsionAmount;
    }

    public double getSprintConstant() {
        return sprintConstant;
    }

    public void setSprintConstant(double sprintConstant) {
        this.sprintConstant = sprintConstant;
    }

    public double getStepDampingFactor() {
        return stepDampingFactor;
    }

    public void setStepDampingFactor(double stepDampingFactor) {
        this.stepDampingFactor = stepDampingFactor;
    }

    public double getStepSize() {
        return stepSize;
    }

    public void setStepSize(double stepSize) {
        this.stepSize = stepSize;
    }

    public double getSubdivisionPointsCycleIncreaseRate() {
        return subdivisionPointsCycleIncreaseRate;
    }

    public void setSubdivisionPointsCycleIncreaseRate(double subdivisionPointsCycleIncreaseRate) {
        this.subdivisionPointsCycleIncreaseRate = subdivisionPointsCycleIncreaseRate;
    }

    public int getSubdivisionPointsPerEdge() {
        return subdivisionPointsPerEdge;
    }

    public void setSubdivisionPointsPerEdge(int subdivisionPointsPerEdge) {
        this.subdivisionPointsPerEdge = subdivisionPointsPerEdge;
    }

    public boolean isUpdateViewAfterEachStep() {
        return updateViewAfterEachStep;
    }

    public void setUpdateViewAfterEachStep(boolean updateViewAfterEachStep) {
        this.updateViewAfterEachStep = updateViewAfterEachStep;
    }

    public boolean isUseInverseQuadraticModel() {
        return useInverseQuadraticModel;
    }

    public void setUseInverseQuadraticModel(boolean useInverseQuadraticModel) {
        this.useInverseQuadraticModel = useInverseQuadraticModel;
    }

    public boolean isUseRepulsionForOppositeEdges() {
        return useRepulsionForOppositeEdges;
    }

    public void setUseRepulsionForOppositeEdges(boolean useRepulsionForOppositeEdges) {
        this.useRepulsionForOppositeEdges = useRepulsionForOppositeEdges;
    }

    public boolean isUseSimpleCompatibilityMeasure() {
        return useSimpleCompatibilityMeasure;
    }

    public void setUseSimpleCompatibilityMeasure(boolean useSimpleCompatibilityMeasure) {
        this.useSimpleCompatibilityMeasure = useSimpleCompatibilityMeasure;
    }
}
