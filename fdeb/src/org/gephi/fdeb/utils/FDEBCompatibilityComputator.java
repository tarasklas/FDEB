/*
 * In future this will be class, included in Bundler with ability to turn off\on some measures. 
 * So far it just keep compatibility functions
 * 
 */
package org.gephi.fdeb.utils;

import java.awt.geom.Point2D;
import org.gephi.graph.api.Edge;
import processing.core.PVector;

/**
 *
 * @author megaterik
 */
public class FDEBCompatibilityComputator {

    public static double calculateCompatibility(Edge aEdge, Edge bEdge) {
        PVector a = new PVector(aEdge.getTarget().getNodeData().x() - aEdge.getSource().getNodeData().x(),
                aEdge.getTarget().getNodeData().y() - aEdge.getSource().getNodeData().y());
        PVector b = new PVector(bEdge.getTarget().getNodeData().x() - bEdge.getSource().getNodeData().x(),
                bEdge.getTarget().getNodeData().y() - bEdge.getSource().getNodeData().y());
        double compatibility = angleCompatibility(a, b) * scaleCompatibility(a, b) * positionCompatibility(a, b, aEdge, bEdge)
                 * visibilityCompatibility(aEdge, bEdge);
        //System.err.println(compatibility);
        return compatibility;
    }

    public static double angleCompatibility(PVector a, PVector b) {
        double compatiblity = Math.abs(a.dot(b) / (a.mag() * b.mag()));

        return compatiblity;
    }

    public static double scaleCompatibility(PVector a, PVector b) {
        double lavg = (a.mag() + b.mag()) / 2;
        double compatibility = 2.0 / (lavg / Math.min(a.mag(), b.mag()) + Math.max(a.mag(), b.mag()) / lavg);
        return compatibility;
    }

    public static double positionCompatibility(PVector a, PVector b, Edge ae, Edge be) {
        PVector aMid = new PVector((ae.getSource().getNodeData().x() + ae.getTarget().getNodeData().x()) / 2,
                (ae.getSource().getNodeData().y() + ae.getTarget().getNodeData().y()) / 2);
        PVector bMid = new PVector((be.getSource().getNodeData().x() + be.getTarget().getNodeData().x()) / 2,
                (be.getSource().getNodeData().y() + be.getTarget().getNodeData().y()) / 2);
        double lavg = (a.mag() + b.mag()) / 2;
        double compatibility = lavg / (lavg + aMid.dist(bMid));

        return compatibility;
    }

    public static double visibilityCompatibility(Edge aEdge, Edge bEdge) {
        Point2D.Float as = new Point2D.Float(aEdge.getSource().getNodeData().x(), aEdge.getSource().getNodeData().y());
        Point2D.Float af = new Point2D.Float(aEdge.getTarget().getNodeData().x(), aEdge.getTarget().getNodeData().y());

        Point2D.Float bs = new Point2D.Float(bEdge.getSource().getNodeData().x(), bEdge.getSource().getNodeData().y());
        Point2D.Float bf = new Point2D.Float(bEdge.getTarget().getNodeData().x(), bEdge.getTarget().getNodeData().y());
        double compatibility = Math.min(visibilityCompatibility(as, af, bs, bf), visibilityCompatibility(bs, bf, as, af));

        return compatibility;
    }

    public static double visibilityCompatibility(Point2D.Float as, Point2D.Float af, Point2D.Float bs, Point2D.Float bf) {
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
}
