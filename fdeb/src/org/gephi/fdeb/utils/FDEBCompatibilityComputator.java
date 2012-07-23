package org.gephi.fdeb.utils;

import java.awt.geom.Point2D;
import org.gephi.graph.api.Edge;
import processing.core.PVector;

/**
 *
 * @author megaterik
 */
public class FDEBCompatibilityComputator {

    private boolean angleCompatibility = true;
    private boolean scaleCompatibility = true;
    private boolean positionCompatibility = true;
    private boolean visibilityCompatibility = true;

    public double calculateCompatibility(Edge aEdge, Edge bEdge) {
        PVector a = new PVector(aEdge.getTarget().getNodeData().x() - aEdge.getSource().getNodeData().x(),
                aEdge.getTarget().getNodeData().y() - aEdge.getSource().getNodeData().y());
        PVector b = new PVector(bEdge.getTarget().getNodeData().x() - bEdge.getSource().getNodeData().x(),
                bEdge.getTarget().getNodeData().y() - bEdge.getSource().getNodeData().y());
        double compatibility = 1.0;
        if (angleCompatibility) {
            if (affectedByDirection) {
                compatibility *= angleCompatibilityAffectedByDirection(a, b);
            } else {
                compatibility *= angleCompatibility(a, b);
            }
        }
        if (scaleCompatibility) {
            compatibility *= scaleCompatibility(a, b);
        }
        if (positionCompatibility) {
            compatibility *= positionCompatibility(a, b, aEdge, bEdge);
        }
        if (visibilityCompatibility) {
            if (!visibilityApply || compatibility >= 0.9) {
                compatibility *= visibilityCompatibility(aEdge, bEdge);
            }
        }
        //System.err.println(compatibility);
        return compatibility;
    }

    private double angleCompatibility(PVector a, PVector b) {
        double compatiblity = Math.abs(a.dot(b) / (a.mag() * b.mag()));

        return compatiblity;
    }

    private double angleCompatibilityAffectedByDirection(PVector a, PVector b) {
        double compatibility = (a.dot(b) / (a.mag() * b.mag()) + 1.0) / 2.0;
        assert (compatibility >= 0);
        assert (compatibility <= 1);
        return compatibility;
    }

    private double scaleCompatibility(PVector a, PVector b) {
        double lavg = (a.mag() + b.mag()) / 2;
        double compatibility = 2.0 / (lavg / Math.min(a.mag(), b.mag()) + Math.max(a.mag(), b.mag()) / lavg);
        assert (compatibility >= 0);
        assert (compatibility <= 1);
        return compatibility;
    }

    private double positionCompatibility(PVector a, PVector b, Edge ae, Edge be) {
        PVector aMid = new PVector((ae.getSource().getNodeData().x() + ae.getTarget().getNodeData().x()) / 2,
                (ae.getSource().getNodeData().y() + ae.getTarget().getNodeData().y()) / 2);
        PVector bMid = new PVector((be.getSource().getNodeData().x() + be.getTarget().getNodeData().x()) / 2,
                (be.getSource().getNodeData().y() + be.getTarget().getNodeData().y()) / 2);
        double lavg = (a.mag() + b.mag()) / 2;
        double compatibility = lavg / (lavg + aMid.dist(bMid));
        assert (compatibility >= 0);
        assert (compatibility <= 1);
        return compatibility;
    }

    private double visibilityCompatibility(Edge aEdge, Edge bEdge) {
        Point2D.Float as = new Point2D.Float(aEdge.getSource().getNodeData().x(), aEdge.getSource().getNodeData().y());
        Point2D.Float af = new Point2D.Float(aEdge.getTarget().getNodeData().x(), aEdge.getTarget().getNodeData().y());

        Point2D.Float bs = new Point2D.Float(bEdge.getSource().getNodeData().x(), bEdge.getSource().getNodeData().y());
        Point2D.Float bf = new Point2D.Float(bEdge.getTarget().getNodeData().x(), bEdge.getTarget().getNodeData().y());
        double compatibility = Math.min(visibilityCompatibility(as, af, bs, bf), visibilityCompatibility(bs, bf, as, af));

        try {
            assert (compatibility >= 0);
            assert (compatibility <= 1);
        } catch (AssertionError ex) {
            return 0;
        }
        return compatibility;
    }

    private double visibilityCompatibility(Point2D.Float as, Point2D.Float af, Point2D.Float bs, Point2D.Float bf) {
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

    public boolean isAngleCompatibility() {
        return angleCompatibility;
    }

    public void setAngleCompatibility(boolean angleCompatibility) {
        this.angleCompatibility = angleCompatibility;
    }

    public boolean isPositionCompatibility() {
        return positionCompatibility;
    }

    public void setPositionCompatibility(boolean positionCompatibility) {
        this.positionCompatibility = positionCompatibility;
    }

    public boolean isScaleCompatibility() {
        return scaleCompatibility;
    }

    public void setScaleCompatibility(boolean scaleCompatibility) {
        this.scaleCompatibility = scaleCompatibility;
    }

    public boolean isVisibilityCompatibility() {
        return visibilityCompatibility;
    }

    public void setVisibilityCompatibility(boolean visibilityCompatibility) {
        this.visibilityCompatibility = visibilityCompatibility;
    }
    private boolean visibilityApply = false;
    private boolean affectedByDirection = false;

    public boolean isVisibilityApply() {
        return visibilityApply;
    }

    public void setVisibilityApply(boolean visibilityApply) {
        this.visibilityApply = visibilityApply;
    }

    public boolean isAffectedByDirection() {
        return affectedByDirection;
    }

    public void setAffectedByDirection(boolean affectedByDirection) {
        this.affectedByDirection = affectedByDirection;
    }
}
