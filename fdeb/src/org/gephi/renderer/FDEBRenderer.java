/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.renderer;

import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import java.awt.Color;
import org.gephi.fdeb.FDEBItem;
import org.gephi.fdeb.FDEBItemBuilder;
import org.gephi.fdeb.FDEBLayoutData;
import org.gephi.preview.api.*;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author megaterik
 */
@ServiceProvider(service = Renderer.class)
public class FDEBRenderer implements Renderer {
    
    public static final float thickness = 0.2f;

    @Override
    public String getDisplayName() {
        return "FDEB renderer";
    }

    @Override
    public void preProcess(PreviewModel previewModel) {
    }

    @Override
    public void render(Item item, RenderTarget target, PreviewProperties properties) {

        FDEBLayoutData data = (FDEBLayoutData) item.getSource();
        for (int i = 0; i < data.subdivisionPoints.length - 1; i++) {
            float x1 = (float) data.subdivisionPoints[i].x;
            float y1 = (float) data.subdivisionPoints[i].y;
            float x2 = (float) data.subdivisionPoints[i + 1].x;
            float y2 = (float) data.subdivisionPoints[i + 1].y;
            renderStraightEdge(x1, y1, x2, y2, target);
        }
        double x1 = data.subdivisionPoints[data.subdivisionPoints.length - 1].x;
        double y1 = data.subdivisionPoints[data.subdivisionPoints.length - 1].y;
    }

    /*
     * variables replaced by constants, method from EdgeRenderer
     */
    public void renderStraightEdge(float x1, float y1, float x2, float y2, RenderTarget renderTarget) {
        Color color = new Color((float)Math.random(), (float)Math.random(), (float)Math.random());

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
