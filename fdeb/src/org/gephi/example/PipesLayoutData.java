/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.example;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import org.gephi.edgelayout.spi.ColorChooserController;
import org.gephi.edgelayout.spi.EdgeLayoutData;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.ui.components.gradientslider.GradientSlider;
import org.openide.util.Lookup;

/**
 *
 * @author megaterik
 */
public class PipesLayoutData implements EdgeLayoutData {

    private Point2D.Double[] points;
    private double totalLength;
    private double maxLength;//on whole graph

    PipesLayoutData(double x1, double y1, double x2, double y2) {
        points = new Point2D.Double[3];
        points[0] = new Point2D.Double(x1, y1);
        points[1] = new Point2D.Double(x2, y1);
        points[2] = new Point2D.Double(x2, y2);
        totalLength = Math.abs(x1 - x2) + Math.abs(y1 - y2);
        maxLength = totalLength;
    }

    @Override
    public Double[] getSubdivisonPoints() {
        return points;
    }

    @Override
    public double getEdgeSortOrder() {
        return totalLength; // first renderer : let's make edges with higher length higher priority
    }

    @Override
    public Color getEdgeColor() {
        /*
         * Use gradient slider that can modify user
         */
        double pos = (totalLength / maxLength);
        return Lookup.getDefault().lookup(ColorChooserController.class).getColor((float) pos);
    }

    @Override
    public double[] getSubdivisionEdgeSortOrder() {
        double[] sortOrder = new double[points.length];
        for (int i = 0; i < sortOrder.length; i++) {
            sortOrder[i] = totalLength;
        }
        return sortOrder;
    }

    @Override
    public Color[] getSubdivisionEdgeColor() {
        Color color = getEdgeColor();
        Color[] colors = new Color[points.length];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = color;
        }
        return colors;
    }

    public void updatePoints(Point2D.Double[] points) {
        totalLength = 0;
        this.points = points;
        for (int i = 0; i < points.length - 1; i++) {
            totalLength += Point2D.distance(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y);
        }
    }

    public double getTotalLength() {
        return totalLength;
    }

    public void setMaxLength(double maxLength) {
        this.maxLength = maxLength;
    }
}
