/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.fdeb;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.util.ArrayList;
import java.util.Collections;
import org.gephi.edgelayout.spi.EdgeLayoutData;
import org.gephi.graph.spi.LayoutData;
import org.gephi.preview.api.PreviewController;
import org.openide.util.Lookup;

public class FDEBLayoutData implements EdgeLayoutData {

    public Point.Double[] newSubdivisionPoints;//to store changes before merging them
    public Point.Double[] subdivisionPoints;
    public double length;
    public static final double eps = 1e-7;
    public FDEBCompatibilityRecord[] similarEdges;
    public double intensity;
    public Color color;

    public FDEBLayoutData(double startPointX, double startPointY, double endPointX, double endPointY) {
        length = Point.Double.distance(startPointX, startPointY, endPointX, endPointY);
        if (length < eps) {
            length = 0;
        }
        subdivisionPoints = new Point.Double[3];
        subdivisionPoints[0] = new Point.Double(startPointX, startPointY);
        subdivisionPoints[1] = new Point.Double((startPointX + endPointX) / 2, (startPointY + endPointY) / 2);
        subdivisionPoints[2] = new Point.Double(endPointX, endPointY);
        color = Color.BLACK;
    }

    private Color generateGradient(Color a, Color b, double ratio) {
        if (ratio > 1) {
            ratio = 1; // to shift gradient to b
        }
        return new Color(
                (int) (a.getRed() * (1.0 - ratio) + b.getRed() * ratio),
                (int) (a.getGreen() * (1.0 - ratio) + b.getGreen() * ratio),
                (int) (a.getBlue() * (1.0 - ratio) + b.getBlue() * ratio),
                 (int) (255 * Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getDoubleValue("subdividededge.alpha")));
    }

    private Color pickGradient(double f) {
        if (f <= 0.2) {
            return generateGradient(Color.WHITE, Color.BLUE, f / 0.2);
        } else if (f <= 0.4) {
            return generateGradient(Color.BLUE, Color.PINK, (f - 0.2) / 0.2);
        } else if (f <= 0.6) {
            return generateGradient(Color.PINK, Color.RED, (f - 0.4) / 0.2);
        } else {
            return generateGradient(Color.RED, Color.YELLOW, (f - 0.6) / 0.2);
        }
    }

    public void updateColor(ArrayList<Double> find) {
        double f = (double) Math.abs(Collections.binarySearch(find, intensity + 1)) / find.size();
        color = pickGradient(f);
    }

    @Override
    public Point.Double[] getSubdivisonPoints() {
        return subdivisionPoints;
    }

    @Override
    public double getEdgeSortOrder() {
        return intensity;
    }

    @Override
    public Color getEdgeColor() {
        return color;
    }
}
