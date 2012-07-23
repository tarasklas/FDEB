/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.edgelayout.api;

import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import org.gephi.edgelayout.spi.EdgeLayoutData;
import org.gephi.graph.api.Edge;
import org.gephi.preview.api.*;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;
import org.openide.util.lookup.ServiceProvider;
import processing.core.PGraphics;

@ServiceProvider(service = Renderer.class)
public class SubdividedEdgeGradientRenderer implements Renderer {

    private float thickness = 1f;
    private float alpha = 0.5f;

    @Override
    public String getDisplayName() {
        return "FDEB renderer with gradients";
    }
    private static final double eps = 0.0001;
    private static final double closeEnough = 1;

    private boolean intersects(Point2D.Double a1, Point2D.Double a2, Point2D.Double b1, Point2D.Double b2) {
        double a = (a2.y - a1.y);
        double b = (a2.x - a1.x);
        double c = -a * a1.x - b * a1.y;
        boolean res = false;
        //res |= Math.abs(a * b1.x + b * b1.y + c) < eps;
        //res |= Math.abs(a * b2.x + b * b2.y + c) < eps;
        res |= (a * b1.x + b * b1.y + c > 0) != (a * b2.x + b * b2.y + c > 0);
        return res;
    }

    /*
     * Simplified version, due to small lines
     */
    private double distanceToLine(Point2D.Double point, Point2D.Double v1, Point2D.Double v2) {
        return Math.min(Point2D.distance(point.x, point.y, v1.x, v1.y), Point2D.distance(point.x, point.y, v2.x, v2.y));
    }

    private boolean isLinesIntersecting(Point2D.Double a1, Point2D.Double a2, Point2D.Double b1, Point2D.Double b2) {
        boolean res = intersects(a1, a2, b1, b2) && intersects(b1, b2, a1, a2);
        if (!res) {
            if (Math.min(distanceToLine(a1, b1, b2), distanceToLine(a2, b1, b2)) < closeEnough) {
                res = true;
            }
        }
        return res;
    }

    private Color generateGradient(float f) {
       // if (f >= 0.5)
        f = Math.min(f * 6, 1);
        return new Color(1f * (1 - f), 1f * (1 - f), 1f, alpha); //white to blue
        //   }
    }

    @Override
    public void preProcess(PreviewModel previewModel) {
    }

    public void forcePreProcess(PreviewModel previewModel) {
        System.err.println("preprocessing active");
        alpha = (float) previewModel.getProperties().getDoubleValue("subdividededge.alpha");
        thickness = (float) previewModel.getProperties().getDoubleValue("subdividededge.thickness");
        System.err.println("for sometype " + previewModel.getItems("FDEB with gradient curve").length);
        int maxIntensity = 1;
        for (Item sitem : previewModel.getItems("FDEB with gradient curve")) {
            SubdividedEdgeWithGradientItem item = (SubdividedEdgeWithGradientItem) sitem;
            EdgeLayoutData data = ((EdgeLayoutData) item.getSource());
            if (data == null) {
                continue;
            }
            item.intervalIntensity = new int[data.getSubdivisonPoints().length];
            item.colors = new Color[data.getSubdivisonPoints().length];
            /*
             * Could optimized to O(n * log(n)) instead of O(n ^ 2) using
             * sweep-line
             */
            System.err.println("edge " + ((Edge) item.getData("edge")).toString());
            for (int i = 0; i < item.intervalIntensity.length - 1; i++) {
                for (Item anothersitem : previewModel.getItems("FDEB with gradient curve")) {
                    if (item.equals(anothersitem)) {
                        continue;
                    }

                    SubdividedEdgeWithGradientItem anotheritem = (SubdividedEdgeWithGradientItem) anothersitem;
                    EdgeLayoutData anotherdata = ((EdgeLayoutData) anotheritem.getSource());
                    for (int j = 0; j < anotherdata.getSubdivisonPoints().length - 1; j++) {
                        if (isLinesIntersecting(data.getSubdivisonPoints()[i], data.getSubdivisonPoints()[i + 1],
                                anotherdata.getSubdivisonPoints()[j], anotherdata.getSubdivisonPoints()[j + 1])) {
                            item.intervalIntensity[i]++;
                            System.err.println("Edge " + item.getData("edge") + " " + i + " with " + anotheritem.getData("edge") + " " + j);
                        }
                    }
                }
                System.err.print(" " + item.intervalIntensity[i]);
                maxIntensity = Math.max(maxIntensity, item.intervalIntensity[i] + 1);
            }
        }

        for (Item sitem : previewModel.getItems("FDEB with gradient curve")) {
            SubdividedEdgeWithGradientItem item = (SubdividedEdgeWithGradientItem) sitem;
            if (item.intervalIntensity != null) {
                for (int i = 0; i < item.intervalIntensity.length; i++) {
                    item.colors[i] = generateGradient((float) (item.intervalIntensity[i] + 1.0) / maxIntensity);
                }

            }
        }
        System.err.println("max is " + maxIntensity);
    }

    @Override
    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        if (item.getSource() == null) {
            return;
        }
        EdgeLayoutData data = (EdgeLayoutData) item.getSource();
        Point2D.Double[] points = data.getSubdivisonPoints();
        startStraightEdge(target);
        assert (item instanceof SubdividedEdgeWithGradientItem);
        Color[] colors = ((SubdividedEdgeWithGradientItem) item).colors;
        if (colors == null || colors[0] == null) {
            return;
        }
        for (int i = 0; i < points.length - 1; i++) {
            float x1 = (float) points[i].x;
            float y1 = (float) points[i].y;
            float x2 = (float) points[i + 1].x;
            float y2 = (float) points[i + 1].y;
            //  float x2 = (float) points[i + 1].x;
            //  float y2 = (float) points[i + 1].y;
            PGraphics graphics = ((ProcessingTarget) target).getGraphics();
            if (((SubdividedEdgeWithGradientItem) item).intervalIntensity.length <= i) {
                System.err.println("should be " + points.length + " but " + ((SubdividedEdgeWithGradientItem) item).intervalIntensity.length + " damn");
                continue;
            }
            //   if (colors[i] != Color.WHITE)
            {
                // System.err.println("paint " + x1 + " " + y1 + " " + x2 + " " + y2);
                graphics.stroke(colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue(), colors[i].getAlpha());
                graphics.vertex(x1, -y1);
                graphics.vertex(x2, -y2);
            }
        }
        endStraightEdge(target);
    }

    private void startStraightEdge(RenderTarget renderTarget) {
        Color color = new Color(0f, 0f, 0.5f, alpha);
        if (renderTarget instanceof ProcessingTarget) {

            PGraphics graphics = ((ProcessingTarget) renderTarget).getGraphics();

            graphics.noFill();
            graphics.strokeWeight(thickness);
            graphics.strokeCap(PGraphics.ROUND);
            graphics.stroke(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());

            graphics.beginShape(PGraphics.LINES);
        }
    }

    private void endStraightEdge(RenderTarget renderTarget) {
        if (renderTarget instanceof ProcessingTarget) {
            PGraphics graphics = ((ProcessingTarget) renderTarget).getGraphics();
            graphics.endShape();
        }
    }

    @Override
    public PreviewProperty[] getProperties() {
        return new PreviewProperty[0];
    }

    @Override
    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        return (item instanceof SubdividedEdgeWithGradientItem);
    }

    @Override
    public boolean needsItemBuilder(ItemBuilder itemBuilder, PreviewProperties properties) {
        return (itemBuilder instanceof SubdividedEdgeWithGradientItemBuilder);
    }
}
