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

    private double stepSize; //S
    private int iterationsPerCycle;//I
    private double sprintConstant;//K
    private double compatibilityThreshold;
    private int numCycles;
    private double stepDampingFactor;
    private double subdivisionPointIncreaseRate;

    public FDEBBundlerParameters() {
        setParametersToDefault();
    }

    void setParametersToDefault() {
        numCycles = 5;
         stepSize = 0.04;
        iterationsPerCycle = 50;
        //sprintConstant = 0.5;
        stepDampingFactor = 0.5;
        compatibilityThreshold = 0;
        subdivisionPointIncreaseRate = 2;
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

    public double getCompatibilityThreshold() {
        return compatibilityThreshold;
    }

    public void setCompatibilityThreshold(double compatibilityThreshold) {
        this.compatibilityThreshold = compatibilityThreshold;
    }

    public double getSubdivisionPointIncreaseRate() {
        return subdivisionPointIncreaseRate;
    }

    public void setSubdivisionPointIncreaseRate(double subdivisionPointIncreaseRate) {
        this.subdivisionPointIncreaseRate = subdivisionPointIncreaseRate;
    }
    
    
}
