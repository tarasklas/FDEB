package org.gephi.fdeb;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import processing.core.PVector;

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
    private double compatibilityThreshold;
    private FDEBBundlerParameters parameters;
    private double subdivisionPointsPerEdge;

    FDEBBundler(LayoutBuilder layoutBuilder, FDEBBundlerParameters parameters) {
        super(layoutBuilder);
        this.parameters = parameters;
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
        stepSize = parameters.getStepSize();
        sprintConstant = parameters.getSprintConstant();
        iterationsPerCycle = parameters.getIterationsPerCycle();
        compatibilityThreshold = parameters.getCompatibilityThreshold();
        sprintConstant = calculateSprintConstant();
        subdivisionPointsPerEdge = 1;//start and end doesnt count
        System.out.println("K " + sprintConstant);
        
        createCompatibilityLists();
    }

    /*
     * Dynamical calculing of K, in jflowmap K =
     * AxisMarks.ordAlpha(Math.min(xStats.getMax() - xStats.getMin(),
     * yStats.getMax() - yStats.getMin()) / 1000);
     */
    double calculateSprintConstant() {
        double minX = Integer.MAX_VALUE;
        double maxX = Integer.MIN_VALUE;
        double minY = Integer.MAX_VALUE;
        double maxY = Integer.MIN_VALUE;
        for (Node node : graphModel.getGraph().getNodes()) {
            minX = Math.min(minX, node.getNodeData().x());
            minY = Math.min(minY, node.getNodeData().y());

            maxX = Math.max(maxX, node.getNodeData().x());
            maxY = Math.max(maxY, node.getNodeData().y());
        }
        return Math.min(maxX - minX, maxY - minY) / 1000;
    }

    @Override
    public void goAlgo() {
        System.err.println("Next iteration");
        for (int step = 0; step < iterationsPerCycle; step++) {
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

                    if (Math.abs(k) <= 1) {
                        Fsi_x *= k;
                        Fsi_y *= k;
                    }
                    double Fei_x = 0;
                    double Fei_y = 0;

                    for (FDEBCompatibilityRecord record: (((FDEBLayoutData)edge.getEdgeData().getLayoutData())).similarEdges) {
                        Edge moveEdge = record.edgeWith;
                        if (moveEdge.isSelfLoop()) {
                            continue;
                        }

                        FDEBLayoutData moveData = moveEdge.getEdgeData().getLayoutData();
                        double v_x = moveData.subdivisionPoints[i].x - data.subdivisionPoints[i].x;
                        double v_y = moveData.subdivisionPoints[i].y - data.subdivisionPoints[i].y;

                        if (Math.abs(v_x) > EPS || Math.abs(v_y) > EPS) {
                            double len_sq = v_x * v_x + v_y * v_y;
                            double m = (record.compatibility / Math.sqrt(len_sq));

                            v_x *= m;
                            v_y *= m;

                            Fei_x += v_x;
                            Fei_y += v_y;
                        }
                    }
                    /*
                     * store new coordinates to update them simultaniously
                     */
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

    void divideEdges() {
        subdivisionPointsPerEdge *= parameters.getSubdivisionPointIncreaseRate();
        for (Edge edge : graphModel.getGraph().getEdges()) {
            if (edge.isSelfLoop()) {
                continue;
            }
            FDEBLayoutData data = (FDEBLayoutData) edge.getEdgeData().getLayoutData();
            Point.Double[] subdivisionPoints = new Point.Double[(int) (subdivisionPointsPerEdge + 2)];//+source/target
            double totalLength = 0.0;
            for (int i = 1; i < data.subdivisionPoints.length; i++) {
                totalLength += Point.Double.distance(data.subdivisionPoints[i - 1].x, data.subdivisionPoints[i - 1].y,
                        data.subdivisionPoints[i].x, data.subdivisionPoints[i].y);
            }
            double lengthPerPoint = totalLength / (((int) subdivisionPointsPerEdge) + 1);
            double remainingLengthPerPoint = lengthPerPoint;
            int curSubdivisionPoint = 1;
            for (int i = 1; i < data.subdivisionPoints.length; i++) {
                double lengthOfEdge = Point.Double.distance(data.subdivisionPoints[i - 1].x, data.subdivisionPoints[i - 1].y,
                        data.subdivisionPoints[i].x, data.subdivisionPoints[i].y);
                double remainingLengthOfEdge = lengthOfEdge;
                while (remainingLengthPerPoint < remainingLengthOfEdge) {
                    remainingLengthOfEdge -= remainingLengthPerPoint;
                    double coef = (lengthOfEdge - remainingLengthOfEdge) / (lengthOfEdge);//0.0--source, 1.0 -- target
                    subdivisionPoints[curSubdivisionPoint] = new Point2D.Double(
                            data.subdivisionPoints[i - 1].x
                            + coef * (data.subdivisionPoints[i].x - data.subdivisionPoints[i - 1].x),
                            data.subdivisionPoints[i - 1].y
                            + coef * (data.subdivisionPoints[i].y - data.subdivisionPoints[i - 1].y));
                    curSubdivisionPoint++;
                    remainingLengthPerPoint = lengthPerPoint;
                }
                remainingLengthPerPoint -= remainingLengthOfEdge;
            }
            subdivisionPoints[0] = data.subdivisionPoints[0];//source
            subdivisionPoints[subdivisionPoints.length - 1] = data.subdivisionPoints[data.subdivisionPoints.length - 1];//target
            
            data.subdivisionPoints = subdivisionPoints;
        }
    }

    double calculateCompatibility(Edge aEdge, Edge bEdge) {
        PVector a = new PVector(aEdge.getTarget().getNodeData().x() - aEdge.getSource().getNodeData().x(),
                aEdge.getTarget().getNodeData().y() - aEdge.getSource().getNodeData().y());
        PVector b = new PVector(bEdge.getTarget().getNodeData().x() - bEdge.getSource().getNodeData().x(),
                bEdge.getTarget().getNodeData().y() - bEdge.getSource().getNodeData().y());
        double compatibility = angleCompatibility(a, b) * scaleCompatibility(a, b) * positionCompatibility(a, b, aEdge, bEdge);
        if (compatibility > .9) {
            compatibility *= visibilityCompatibility(aEdge, bEdge);
        }
        return compatibility;
    }

    double angleCompatibility(PVector a, PVector b) {
        double compatiblity = Math.abs(a.dot(b) / (a.mag() * b.mag()) + 1.0) / 2.0;
        if (compatiblity < EPS) {
            compatiblity = 0;
        }
        if (compatiblity > 1 - EPS) {
            compatiblity = 1;
        }

        return compatiblity;
    }

    double scaleCompatibility(PVector a, PVector b) {
        double lavg = (a.mag() + b.mag()) / 2;
        double compatibility = 2.0 / (lavg / Math.min(a.mag(), b.mag()) + Math.max(a.mag(), b.mag()) / lavg);
        return compatibility;
    }

    double positionCompatibility(PVector a, PVector b, Edge ae, Edge be) {
        PVector aMid = new PVector((ae.getSource().getNodeData().x() + ae.getTarget().getNodeData().x()) / 2,
                (ae.getSource().getNodeData().y() + ae.getTarget().getNodeData().y()) / 2);
        PVector bMid = new PVector((be.getSource().getNodeData().x() + be.getTarget().getNodeData().x()) / 2,
                (be.getSource().getNodeData().y() + be.getTarget().getNodeData().y()) / 2);
        double lavg = (a.mag() + b.mag()) / 2;
        double compatibility = lavg / (lavg + aMid.dist(bMid));


        return compatibility;
    }

    double visibilityCompatibility(Edge aEdge, Edge bEdge) {
        Point2D.Float as = new Point2D.Float(aEdge.getSource().getNodeData().x(), aEdge.getSource().getNodeData().y());
        Point2D.Float af = new Point2D.Float(aEdge.getTarget().getNodeData().x(), aEdge.getTarget().getNodeData().y());

        Point2D.Float bs = new Point2D.Float(bEdge.getSource().getNodeData().x(), bEdge.getSource().getNodeData().y());
        Point2D.Float bf = new Point2D.Float(bEdge.getTarget().getNodeData().x(), bEdge.getTarget().getNodeData().y());
        double compatibility = Math.min(visibilityCompatibility(as, af, bs, bf), visibilityCompatibility(bs, bf, as, af));


        return compatibility;
    }

    double visibilityCompatibility(Point2D.Float as, Point2D.Float af, Point2D.Float bs, Point2D.Float bf) {
        Point2D.Float i1 = projectPointToLine(as.x, as.y, af.x, af.y, bs.x, bs.y);
        Point2D.Float i2 = projectPointToLine(as.x, as.y, af.x, af.y, bf.x, bf.y);
        Point2D.Float im = new Point2D.Float((i1.x + i2.x) / 2, (i1.y + i2.y) / 2);
        Point2D.Float bm = new Point2D.Float((bs.x + bf.x) / 2, (bs.y + bf.y) / 2);
        return Math.max(0, 1 - 2 * bm.distance(im) / (i1.distance(i2)));
    }

    /**
     * See http://www.exaflop.org/docs/cgafaq/cga1.html
     */
    public static Point2D.Float projectPointToLine(double x1, double y1, double x2, double y2, double x, double y) {
        double L = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        double r = ((y1 - y) * (y1 - y2) - (x1 - x) * (x2 - x1)) / (L * L);
        return new Point2D.Float((float) (x1 + r * (x2 - x1)), (float) (y1 + r * (y2 - y1)));
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

    private void createCompatibilityLists() {
        ArrayList<FDEBCompatibilityRecord> similar = new ArrayList<FDEBCompatibilityRecord>();
        double totalEdges = 0;
        double passedEdges = 0;
        for (Edge edge : graphModel.getGraph().getEdges())
        {
            similar.clear();
            if (edge.isSelfLoop())
                continue;
            for (Edge probablySimilarEdge : graphModel.getGraph().getEdges())
            {
                if (probablySimilarEdge.isSelfLoop() || probablySimilarEdge == edge)
                    continue;
                double compatibility = calculateCompatibility(edge, probablySimilarEdge);
                //System.err.println(compatibility + " " + compatibilityThreshold);
                totalEdges++;
                if (compatibility < compatibilityThreshold)
                    continue;
                passedEdges++;
                similar.add(new FDEBCompatibilityRecord(compatibility, probablySimilarEdge));
            }
            ((FDEBLayoutData)edge.getEdgeData().getLayoutData()).similarEdges = new FDEBCompatibilityRecord[similar.size()];
            for (int i = 0; i < similar.size(); i++)
                ((FDEBLayoutData)edge.getEdgeData().getLayoutData()).similarEdges[i] = similar.get(i);
        }
        System.err.println("total: " + totalEdges + " passed " + passedEdges + " fraction " + ((double) passedEdges) / totalEdges);
       
    }
}
