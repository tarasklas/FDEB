/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.renderer;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.gephi.fdeb.FDEBLayoutData;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 *
 * Primitive and slow class to observe described in paper export method, unlikely to be used afterwards
 */
public class FDEBSimpleBitmapExport {

    int getX(int x, int i) {
        return (int) Math.round(((double) (x - minX)) * image.getWidth() / (maxX - minX)) + i;
    }

    int getY(int y, int j) {
        return (int) Math.round(((double) (y - minY)) * image.getHeight() / (maxY - minY)) + j;
    }

    void incPoint(int x, int y) {
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                if (getX(x, i) >= 0 && getY(y, j) >= 0 && getX(x, i) < image.getWidth() && getY(y, j) < image.getHeight()) {
                    points[getX(x, i)][getY(y, j)] += 1.0 / (1.0 + Math.abs(i) + Math.abs(j));
                    //1 for center, less otherwise, kind of smoothing
                }

            }
        }
    }
    int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
    BufferedImage image;
    float[][] points;

    void makeLine(double x1, double y1, double x2, double y2) {
        double len = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        double incx = (x2 - x1) / len;//move one pixel per turn
        double incy = (y2 - y1) / len;
        for (int i = 0; i < len; i++) {
            x1 += incx;
            y1 += incy;
            incPoint((int) x1, (int) y1);
        }
    }

    public void export(Graph graph, String filename) throws IOException {
        for (Node node : graph.getNodes()) {
            minX = Math.min((int) node.getNodeData().x(), minX);
            minY = Math.min((int) node.getNodeData().y(), minY);

            maxX = Math.max((int) node.getNodeData().x(), maxX);
            maxY = Math.max((int) node.getNodeData().y(), maxY);
        }
        image = new BufferedImage(maxX - minX, (maxY - minY), BufferedImage.TYPE_INT_RGB);
        points = new float[image.getWidth()][image.getHeight()];

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                image.setRGB(i, j, Color.WHITE.getRGB());
            }
        }

        for (Edge edge : graph.getEdges()) {
            FDEBLayoutData data = edge.getEdgeData().getLayoutData();
            for (int i = 0; i < data.subdivisionPoints.length - 1; i++) {
                makeLine(data.subdivisionPoints[i].x, data.subdivisionPoints[i].y, data.subdivisionPoints[i + 1].x, data.subdivisionPoints[i + 1].y);
            }
        }

        float max = 0;
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                max = Math.max(max, points[i][j]);
            }
        }


        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (points[i][j] < max / 3) {
                   // points[i][j] = 0;
                }
            }
        }

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (points[i][j] > 0) {
                    image.setRGB(i, j, new Color(0f, 0f, 0.3f + 0.7f * points[i][j] / max, 1.0f).getRGB());
                }
            }
        }
        System.err.println("Export to png: " + ImageIO.write(image, "png", new File(filename + ".png")));
    }
}
