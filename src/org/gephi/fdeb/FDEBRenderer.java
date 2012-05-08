/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.fdeb;

import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import java.awt.Color;
import java.util.Locale;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.MetaEdge;
import org.gephi.preview.api.*;
import org.gephi.preview.plugin.builders.EdgeBuilder;
import org.gephi.preview.plugin.items.EdgeItem;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.plugin.renderers.EdgeRenderer;
import org.gephi.preview.plugin.renderers.NodeRenderer;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 *
 * @author megaterik
 */
@ServiceProvider(service = Renderer.class)
public class FDEBRenderer implements Renderer {

    @Override
    public String getDisplayName() {
        return "FDEB renderer";
    }

    @Override
    public void preProcess(PreviewModel previewModel) {
    }

    @Override
    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        //   FDEBLayoutData data = (FDEBLayoutData) item.getSource();
        //  float x1 = (float) (data.subdivisionPoints[0].x);
        //   float y1 = (float) (data.subdivisionPoints[0].y);

        //   float x2 = (float) (data.subdivisionPoints[data.subdivisionPoints.length - 1].x);
        //  float y2 = (float) (data.subdivisionPoints[data.subdivisionPoints.length - 1].y);


        //  Edge edge = (Edge) item.getData("edge");
        FDEBLayoutData data = (FDEBLayoutData) item.getSource();
        System.err.println("edge has " + data.subdivisionPoints.length + " subpoints");
        for (int i = 0; i < data.subdivisionPoints.length - 1; i++) {
            float x1 = (float) data.subdivisionPoints[i].x;
            float y1 = (float) data.subdivisionPoints[i].y;
            float x2 = (float) data.subdivisionPoints[i + 1].x;
            float y2 = (float) data.subdivisionPoints[i + 1].y;
          //  System.err.println(i + " " + x1 + " " + y1);
            // System.err.println("edge source " + edge.getSource().getNodeData().x() + " " + edge.getSource().getNodeData().y());
            // System.err.println("edge target " + edge.getTarget().getNodeData().x() + " " + edge.getTarget().getNodeData().y());
            // System.err.println();
            renderStraightEdge(x1, y1, x2, y2, target);
        }
        double x1 = data.subdivisionPoints[data.subdivisionPoints.length - 1].x;
        double y1 = data.subdivisionPoints[data.subdivisionPoints.length - 1].y;
        //renderStraightEdge((float)data.subdivisionPoints[0].x, (float)data.subdivisionPoints[0].y, (float)x1, (float)y1, target);
        //System.err.println(data.subdivisionPoints.length - 1 + " " + x1 + " " + y1);
        //System.err.println();
    }

    /*
     * variables replaced by constants, method from EdgeRenderer
     */
    public void renderStraightEdge(float x1, float y1, float x2, float y2, RenderTarget renderTarget) {

        int thickness = 2;
        Color color = new Color((float)Math.random(), (float)Math.random(), (float)Math.random());
        //Target radius - to start at the base of the arrow
        Float targetRadius = 2f;
        //Avoid edge from passing the node's center:
        if (targetRadius != null && targetRadius < 0) {
            PVector direction = new PVector(x2, y2);
            direction.sub(new PVector(x1, y1));
            direction.normalize();
            direction = new PVector(direction.x, direction.y);
            direction.mult(targetRadius);
            direction.add(new PVector(x2, y2));
            x2 = direction.x;
            y2 = direction.y;
        }
        //Source radius
        Float sourceRadius = 2f;
        //Avoid edge from passing the node's center:
        if (sourceRadius != null && sourceRadius < 0) {
            PVector direction = new PVector(x1, y1);
            direction.sub(new PVector(x2, y2));
            direction.normalize();
            direction = new PVector(direction.x, direction.y);
            direction.mult(sourceRadius);
            direction.add(new PVector(x1, y1));
            x1 = direction.x;
            y1 = direction.y;
        }


        PDFTarget pdfTarget = (PDFTarget) renderTarget;
        PdfContentByte cb = pdfTarget.getContentByte();
        cb.moveTo(x1, y1);
        cb.lineTo(x2, y2);
        cb.setRGBColorStroke(color.getRed(), color.getGreen(), color.getBlue());
        cb.setLineWidth(thickness);
        if (color.getAlpha() < 255) {
            cb.saveState();
            float alpha = color.getAlpha() / 255f;
            PdfGState gState = new PdfGState();
            gState.setStrokeOpacity(alpha);
            cb.setGState(gState);
        }
        cb.stroke();
        if (color.getAlpha() < 255) {
            cb.restoreState();

        }
    }

    @Override
    public PreviewProperty[] getProperties() {
        return new PreviewProperty[0];
    }

    @Override
    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        return (item instanceof FDEBItem);
    }

    @Override
    public boolean needsItemBuilder(ItemBuilder itemBuilder, PreviewProperties properties) {
        return (itemBuilder instanceof FDEBItemBuilder);
        //return (itemBuilder instanceof EdgeBuilder);
    }
}
