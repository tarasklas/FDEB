/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.fdeb;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import org.gephi.edgelayout.spi.EdgeLayoutData;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.ui.components.gradientslider.GradientSlider;
import org.openide.util.Lookup;

public class FDEBLayoutData implements EdgeLayoutData {

    public Point.Double[] newSubdivisionPoints;//to store changes before merging them
    public Point.Double[] subdivisionPoints;
    public double length;
    public static final double eps = 1e-7;
    public FDEBCompatibilityRecord[] similarEdges;
    public double intensity;//for edge
    public double[] intensities; //for each subdivision point
    public Color color; //for edge
    public Color[] colors; //for each subdivision point

    public FDEBLayoutData(double startPointX, double startPointY, double endPointX, double endPointY) {
        length = Point.Double.distance(startPointX, startPointY, endPointX, endPointY);
        if (length < eps) {
            length = 0;
        }
        subdivisionPoints = new Point.Double[3];
        subdivisionPoints[0] = new Point.Double(startPointX, startPointY);
        subdivisionPoints[1] = new Point.Double((startPointX + endPointX) / 2, (startPointY + endPointY) / 2);
        subdivisionPoints[2] = new Point.Double(endPointX, endPointY);
        color = null;
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

    private Color pickGradientManually(float f) {
        return new Color(
                (int) (Color.WHITE.getRed() * (1f - f) + Color.BLUE.getRed() * f),
                (int) (Color.WHITE.getGreen() * (1f - f) + Color.BLUE.getGreen() * f),
                (int) (Color.WHITE.getBlue() * (1f - f) + Color.BLUE.getBlue() * f));
    }

    private Color pickGradientFromLookup(double f) {
        /*
         * That is likely woudn't work with toolkit, since gradientslider is set
         * in EdgeLayoutWindowTopComponent.
         */
        GradientSlider gradientSlider = Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getValue(PreviewProperty.EDGE_LAYOUT_GRADIENT_SLIDER_LOCATION);
        assert (gradientSlider != null);
        return (Color) gradientSlider.getValue((float) f);
    }

    public void updateColor(ArrayList<Double> find) {
        double f;
        if (Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getBooleanValue(PreviewProperty.EDGE_LAYOUT_USE_PERCENTAGE_INSTEAD_OF_LINEAR_SCALE)) {
            f = (double) Math.abs(Collections.binarySearch(find, intensity + 1)) / find.size();
        } else {
            f = intensity / find.get(find.size() - 1);
        }
        if (f > 1) {
            f = 1;
        }
        color = pickGradientFromLookup(f);
    }

    public void updateColors(ArrayList<Double> find) {
        if (colors == null || colors.length != subdivisionPoints.length) {
            colors = new Color[subdivisionPoints.length];
        }
        for (int i = 0; i < colors.length; i++) {
            double f;
            if (Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getBooleanValue(PreviewProperty.EDGE_LAYOUT_USE_PERCENTAGE_INSTEAD_OF_LINEAR_SCALE)) {
                f = (double) Math.abs(Collections.binarySearch(find, intensities[i])) / find.size();
            } else {
                f = (intensities[i]) / find.get(find.size() - 1);
            }
            if (f > 1) {
                f = 1;
            }
            colors[i] = pickGradientFromLookup(f);
        }
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

    @Override
    public Color[] getSubdivisionEdgeColor() {
        return colors;
    }

    @Override
    public double[] getSubdivisionEdgeSortOrder() {
        return intensities;
    }
}
