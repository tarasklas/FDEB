package org.gephi.fdeb;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 *
 * @author megaterik
 */
public class FDEBBundler extends AbstractLayout implements Layout {

    private static final double EPS = 1e-7;
    private int cycle;
    private double stepSize;   // S
    private int iterationsPerCycle;    // I
    private double sprintConstant; // K
    private FDEBBundlerParameters parameters;

    FDEBBundler(LayoutBuilder layoutBuilder, FDEBBundlerParameters parameters) {
        super(layoutBuilder);
        this.parameters = parameters;
    }

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

    }

    @Override
    public void goAlgo() {
        for (int step = 0; step < stepSize; step++) {

            for (Edge edge : graphModel.getGraph().getEdges()) {
                ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).newSubdivisionPoints = Arrays.copyOf(((FDEBLayoutData) edge.getEdgeData().getLayoutData()).subdivisionPoints,
                        ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).subdivisionPoints.length);
            }

            for (Edge edge : graphModel.getGraph().getEdges()) {
                if (edge.isSelfLoop()) {
                    continue;
                }
                FDEBLayoutData data = edge.getEdgeData().getLayoutData();
                double k = sprintConstant / (data.length * (data.subdivisionPoints.length - 1));
                for (int i = 1; i < data.subdivisionPoints.length - 1; i++)//first and last are fixed
                {
                    double Fsi_x = (data.subdivisionPoints[i - 1].x - data.subdivisionPoints[i].x)
                            + (data.subdivisionPoints[i + 1].x - data.subdivisionPoints[i].x);
                    double Fsi_y = (data.subdivisionPoints[i - 1].y - data.subdivisionPoints[i].y)
                            + (data.subdivisionPoints[i + 1].y - data.subdivisionPoints[i].y);

                    if (Math.abs(k) <= 1) //todo: investigate when it happens
                    {
                        Fsi_x *= k;
                        Fsi_y *= k;
                    }

                    double Fei_x = 0;
                    double Fei_y = 0;
                    for (Edge moveEdge : graphModel.getGraph().getEdges()) {
                        if (moveEdge.isSelfLoop()) {
                            continue;
                        }
                        FDEBLayoutData moveData = moveEdge.getEdgeData().getLayoutData();
                        double v_x = moveData.subdivisionPoints[i].x - data.subdivisionPoints[i].x;
                        double v_y = moveData.subdivisionPoints[i].y - data.subdivisionPoints[i].y;
                        if (Math.abs(v_x) > EPS || Math.abs(v_y) > EPS) {
                            double len_sq = v_x * v_x + v_y * v_y;
                            double m = (1.0 / len_sq);// /len^2
                            v_x *= m;
                            v_y *= m;
                            Fei_x += v_x;
                            Fei_y += v_y;
                        }
                    }

                    data.newSubdivisionPoints[i] = new Point.Double(data.subdivisionPoints[i].x + stepSize * (Fei_x + Fsi_x),
                            data.subdivisionPoints[i].y + stepSize * (Fei_y + Fsi_y));
                }
            }

            for (Edge edge : graphModel.getGraph().getEdges()) {
                ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).subdivisionPoints = ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).newSubdivisionPoints;
                ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).newSubdivisionPoints = null;
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

    /*
     * Flowmap uses more complicated version with arbitrary multiplier of
     * subdivision points, but for this prototype I think it's fine to just
     * double them.
     */
    void divideEdges() {
        for (Edge edge : graphModel.getGraph().getEdges()) {
            if (edge.isSelfLoop()) {
                continue;
            }

            FDEBLayoutData data = (FDEBLayoutData) edge.getEdgeData().getLayoutData();
            Point.Double[] subdivisionPoints = new Point.Double[(data.subdivisionPoints.length * 2 - 1)];
            for (int i = 0; i < subdivisionPoints.length; i++) {
                if (i % 2 == 0) {
                    subdivisionPoints[i] = data.subdivisionPoints[i / 2];
                }
            }

            for (int i = 0; i < subdivisionPoints.length; i++) {
                if (i % 2 == 1) {
                    subdivisionPoints[i] = new Point.Double((subdivisionPoints[i - 1].x + subdivisionPoints[i + 1].x) / 2,
                            (subdivisionPoints[i - 1].y + subdivisionPoints[i + 1].y) / 2);
                }
            }
            data.subdivisionPoints = subdivisionPoints;
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
}
