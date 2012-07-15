/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.edgelayout.api;

import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import java.awt.Color;
import java.awt.geom.Point2D;
import org.gephi.edgelayout.spi.EdgeLayoutData;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.preview.api.*;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import processing.core.PGraphics;

/**
 *
 * @author megaterik Will be rewritten soon.
 */
@ServiceProvider(service = Renderer.class)
public class SubdividedEdgeRenderer implements Renderer {

    private float thickness = 1f;
    private float alpha = 0.5f;

    @Override
    public String getDisplayName() {
        return "FDEB renderer";
    }

    @Override
    public void preProcess(PreviewModel previewModel) {
        alpha = (float) previewModel.getProperties().getDoubleValue("subdividededge.alpha");
        thickness = (float) previewModel.getProperties().getDoubleValue("subdividededge.thickness");
    }

    @Override
    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        if (item.getSource() == null) {
            return;
        }
        EdgeLayoutData data = (EdgeLayoutData) item.getSource();
        Point2D.Double[] points = data.getSubdivisonPoints();
        for (int i = 0; i < points.length - 1; i++) {
            float x1 = (float) points[i].x;
            float y1 = (float) points[i].y;
            float x2 = (float) points[i + 1].x;
            float y2 = (float) points[i + 1].y;
            renderStraightEdge(x1, y1, x2, y2, target, ((Edge)item.getData("edge")).getWeight());
        }
    }

    /*
     * variables replaced by constants, method from EdgeRenderer
     */
    public void renderStraightEdge(float x1, float y1, float x2, float y2, RenderTarget renderTarget, float weight) {
        Color color = new Color(0f, 0f, 0.5f, alpha);
        if (renderTarget instanceof PDFTarget) {
            PDFTarget pdfTarget = (PDFTarget) renderTarget;
            PdfContentByte cb = pdfTarget.getContentByte();
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
            cb.moveTo(x1, y1);
            cb.lineTo(x2, y2);
        } else {
            PGraphics graphics = ((ProcessingTarget) renderTarget).getGraphics();
            graphics.strokeWeight(thickness);
            graphics.strokeCap(PGraphics.SQUARE);
            graphics.stroke(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            graphics.noFill();
            graphics.line(x1, -y1, x2, -y2);
        }
    }

    @Override
    public PreviewProperty[] getProperties() {
        return new PreviewProperty[0];
    }

    @Override
    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        return (item instanceof SubdividedEdgeItem);
    }

    @Override
    public boolean needsItemBuilder(ItemBuilder itemBuilder, PreviewProperties properties) {
        return (itemBuilder instanceof SubdividedEdgeItemBuilder);
    }
}
