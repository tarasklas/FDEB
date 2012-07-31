/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.edgelayout;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.imageio.ImageIO;
import org.gephi.edgelayout.api.SubdividedEdgeItem;
import org.gephi.edgelayout.spi.EdgeLayoutData;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.PreviewController;
import org.openide.util.Lookup;
import org.w3c.dom.css.RGBColor;

/**
 *
 * Primitive and slow class to observe described in paper export method,
 * unlikely to be used afterwards
 */
public class FDEBSimpleBitmapExport {

    double getX(double x) {
        return Math.round(((double) (x - minX)) * image.getWidth() / (maxX - minX));
    }

    double getY(double y) {
        return Math.round(((double) (y - minY)) * image.getHeight() / (maxY - minY));
    }

    void incPoint(int x, int y) {
        for (int i = -0; i <= 0; i++) {
            for (int j = -0; j <= 0; j++) {
                if (x + i >= 0 && y + j >= 0 && x + i < image.getWidth() && y + j < image.getHeight()) {
                    points[x + i][y + j] += 1.0 / (1.0 + Math.abs(i) + Math.abs(j));
                    //1 for center, less otherwise, kind of smoothing
                }

            }
        }
    }
    int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
    BufferedImage image;
    float[][] points;

    void makeLine(double x1, double y1, double x2, double y2) {
        double len = Math.sqrt((getX(x1) - getX(x2)) * (getX(x1) - getX(x2))
                + (getY(y1) - getY(y2)) * (getY(y1) - getY(y2)));
        double incx = (getX(x2) - getX(x1)) / len;//move one pixel per turn
        double incy = (getY(y2) - getY(y1)) / len;
        x1 = getX(x1);
        y1 = getY(y1);
        for (int i = 0; i < len; i++) {
            x1 += incx;
            y1 += incy;
            incPoint((int) x1, (int) y1);
        }
    }

    public void export(Graph graph, String filename) throws IOException {
        if (graph.getNodeCount() <= 1) {
            return;
        }

        for (Node node : graph.getNodes()) {
            minX = Math.min((int) node.getNodeData().x(), minX);
            minY = Math.min((int) node.getNodeData().y(), minY);

            maxX = Math.max((int) node.getNodeData().x(), maxX);
            maxY = Math.max((int) node.getNodeData().y(), maxY);
        }
        //int width = 1024 * 4;
        int width = maxX - minX;
        width *= 4;
        int height = width * (maxY - minY) / (maxX - minX); //scale
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        points = new float[image.getWidth()][image.getHeight()];

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                image.setRGB(i, j, Color.WHITE.getRGB());
            }
        }

        for (Edge edge : graph.getEdges()) {
            EdgeLayoutData data = edge.getEdgeData().getLayoutData();
            for (int i = 0; i < data.getSubdivisonPoints().length - 1; i++) {
                makeLine(data.getSubdivisonPoints()[i].x, data.getSubdivisonPoints()[i].y, data.getSubdivisonPoints()[i + 1].x, data.getSubdivisonPoints()[i + 1].y);
            }
        }

        ArrayList<Float> sort = new ArrayList<Float>();
        float max = 0;
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (points[i][j] > 1) {
                    sort.add(points[i][j]);
                }
                max = Math.max(max, points[i][j]);
            }
        }

        // max = 25;

        /*
         * for (int i = 0; i < image.getWidth(); i++) { for (int j = 0; j <
         * image.getHeight(); j++) { if (points[i][j] < max / 3) { //
         * points[i][j] = 0; } } }
         */

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (points[i][j] > 0) {
                    float f = (float) Math.abs(Collections.binarySearch(sort, points[i][j])) / sort.size();
                    //image.setRGB(i, j, new Color(0f, 0f, Math.min(1f, 1f * (float) Math.sqrt(points[i][j] / max)), 1.0f).getRGB());
                    image.setRGB(i, j, generateGradient(f).getRGB());
                }
            }
        }

        System.err.println("Export to png: " + ImageIO.write(image, "png", new File(filename + ".png")));
    }

    private Color generateGradient(float f) {
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
}
