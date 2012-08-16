/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.edgelayout.api;

import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.beans.PropertyEditorManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.gephi.edgelayout.spi.EdgeLayoutData;
import org.gephi.graph.api.Edge;
import org.gephi.preview.api.*;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;
import processing.core.PGraphics;

/**
 *
 * @author megaterik Will be rewritten soon.
 */
@ServiceProvider(service = Renderer.class)
public class SubdividedEdgeRenderer implements Renderer {

    private float thickness;
    private float alpha;
    private float intAlpha;
    private boolean forceAlpha;
    private Color originalColor;
    private boolean useSimpleRendererBecauseOtherAreEmpty;

    @Override
    public String getDisplayName() {
        return "FDEB renderer";
    }

    @Override
    public void preProcess(PreviewModel previewModel) {
        alpha = (float) previewModel.getProperties().getDoubleValue(PreviewProperty.EDGE_LAYOUT_EDGE_TRANSPARENCY);
        intAlpha = (int) (255 * alpha);
        forceAlpha = (boolean) previewModel.getProperties().getBooleanValue(PreviewProperty.EDGE_LAYOUT_EDGE_TRANSPARENCY_FORCE);
        thickness = (float) previewModel.getProperties().getDoubleValue(PreviewProperty.EDGE_LAYOUT_EDGE_THICKNESS);
        originalColor = previewModel.getProperties().getColorValue(PreviewProperty.EDGE_LAYOUT_SIMPLE_RENDERER_COLOR);
        useSimpleRendererBecauseOtherAreEmpty = false;
        if (previewModel.getItems("FDEB gradient curve") == null || previewModel.getItems("FDEB gradient curve").length == 0
                || !((SubdividedEdgeBigItem) previewModel.getItems("FDEB gradient curve")[0]).isReady()) {
            useSimpleRendererBecauseOtherAreEmpty = true;
        }
    }

    private void renderSimpleItem(SubdividedEdgeItem item, RenderTarget target, PreviewProperties properties) {
        if (item.getSource() == null) {
            return;
        }
        EdgeLayoutData data = (EdgeLayoutData) item.getSource();
        Point2D.Double[] points = data.getSubdivisonPoints();
        if (target instanceof ProcessingTarget) {
            renderSimpleProcessingItem(item, target, properties, points);
        } else if (target instanceof PDFTarget) {
            renderSimplePDFItem(item, target, properties, points);
        } else if (target instanceof SVGTarget) {
            renderSimpleSVGItem(item, target, properties, points, ((Edge) item.getData("edge")).toString());
        }
    }

    private void renderSimpleSVGItem(SubdividedEdgeItem item, RenderTarget target, PreviewProperties properties, Point2D.Double[] points, String classEdge) {
        Color color = new Color(originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue(), (int) (255 * alpha));
        SVGTarget svgTarget = (SVGTarget) target;
        Element edgeElem = svgTarget.createElement("polyline");
        edgeElem.setAttribute("stroke", svgTarget.toHexString(color));
        edgeElem.setAttribute("stroke-width", Float.toString(thickness * svgTarget.getScaleRatio()));
        edgeElem.setAttribute("stroke-opacity", (color.getAlpha() / 255f) + "");
        edgeElem.setAttribute("fill", "none");
        edgeElem.setAttribute("class", classEdge);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < points.length; i++) {
            if (i != 0) {
                builder.append(",");
            }
            builder.append(points[i].x).append(" ").append(-points[i].y);
        }
        edgeElem.setAttribute("points", builder.toString());
        svgTarget.getTopElement(SVGTarget.TOP_EDGES).appendChild(edgeElem);

    }

    private void renderSimplePDFItem(SubdividedEdgeItem item, RenderTarget target, PreviewProperties properties, Point2D.Double[] points) {
        Color color = new Color(originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue(), (int) (255 * alpha));
        PDFTarget pdfTarget = (PDFTarget) target;
        PdfContentByte cb = pdfTarget.getContentByte();
        for (int i = 0; i < points.length - 1; i++) {
            cb.moveTo((float) points[i].x, (float) points[i].y);
            cb.lineTo((float) points[i + 1].x, (float) points[i + 1].y);
        }
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

    private void renderSimpleProcessingItem(SubdividedEdgeItem item, RenderTarget target, PreviewProperties properties, Point2D.Double[] points) {
        assert (originalColor != null);
        Color color = new Color(originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue(), (int) (255 * alpha));
        PGraphics graphics = ((ProcessingTarget) target).getGraphics();
        graphics.noFill();
        graphics.strokeWeight(thickness);
        graphics.strokeCap(PGraphics.ROUND);
        graphics.stroke(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        graphics.beginShape();

        for (int i = 0; i < points.length; i++) {
            float x1 = (float) points[i].x;
            float y1 = (float) points[i].y;
            graphics.vertex(x1, -y1);
        }
        graphics.endShape();
    }

    @Override
    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        if (item instanceof SubdividedEdgeItem && (properties.getValue(PreviewProperty.EDGE_LAYOUT_USE_RENDERER).equals(PreviewProperty.RendererModes.SimpleRenderer) || useSimpleRendererBecauseOtherAreEmpty)) {
            assert (properties.getValue(PreviewProperty.EDGE_LAYOUT_USE_RENDERER).equals(PreviewProperty.RendererModes.SimpleRenderer)
                    || useSimpleRendererBecauseOtherAreEmpty);
            renderSimpleItem((SubdividedEdgeItem) item, target, properties);
        } else if (!useSimpleRendererBecauseOtherAreEmpty
                && item instanceof SubdividedEdgeBigItem && properties.getValue(PreviewProperty.EDGE_LAYOUT_USE_RENDERER).equals(PreviewProperty.RendererModes.GradientRenderer)) {
            renderBigItem((SubdividedEdgeBigItem) item, target, properties);
        } else if (!useSimpleRendererBecauseOtherAreEmpty
                && item instanceof SubdividedEdgeBigItem && properties.getValue(PreviewProperty.EDGE_LAYOUT_USE_RENDERER).equals(PreviewProperty.RendererModes.GradientComplexRenderer)) {
            renderBigAndComplexItem((SubdividedEdgeBigItem) item, target, properties);
        }
    }

    private void renderBigItem(SubdividedEdgeBigItem item, RenderTarget target, PreviewProperties properties) {
        System.err.println("render big item! " + System.currentTimeMillis());
        if (target instanceof ProcessingTarget) {
            renderBigProcessingItem(item, target, properties);
        } else if (target instanceof PDFTarget) {
            renderBigPDFItem(item, target, properties);
        } else if (target instanceof SVGTarget) {
            renderBigSVGItem(item, target, properties);
        }
    }

    private void renderBigAndComplexItem(SubdividedEdgeBigItem item, RenderTarget target, PreviewProperties properties) {
        if (target instanceof ProcessingTarget) {
            renderBigAndComplexProcessingItem(item, target, properties);
        } else if (target instanceof PDFTarget) {
            renderBigAndComplexPDFItem(item, target, properties);
        } else if (target instanceof SVGTarget) {
            renderBigAndComplexSVGItem(item, target, properties);
        }
    }

    private void renderBigPDFItem(SubdividedEdgeBigItem item, RenderTarget target, PreviewProperties properties) {
        for (SortedEdgeWrapper edgeWrapper : item.edges) {
            Edge edge = edgeWrapper.edge;
            EdgeLayoutData data = (EdgeLayoutData) edge.getEdgeData().getLayoutData();
            Point2D.Double[] points = data.getSubdivisonPoints();

            Color color = new Color(data.getEdgeColor().getRed(), data.getEdgeColor().getGreen(), data.getEdgeColor().getBlue(), (int) (255 * alpha));
            PDFTarget pdfTarget = (PDFTarget) target;
            PdfContentByte cb = pdfTarget.getContentByte();
            for (int i = 0; i < points.length - 1; i++) {
                cb.moveTo((float) points[i].x, (float) points[i].y);
                cb.lineTo((float) points[i + 1].x, (float) points[i + 1].y);
            }
            cb.setRGBColorStroke(color.getRed(), color.getGreen(), color.getBlue());
            cb.setLineWidth(thickness);
            
            float usedAlpha = (forceAlpha ? intAlpha : color.getAlpha());
            if (usedAlpha < 255) {
                cb.saveState();
                float alpha = usedAlpha / 255f;
                PdfGState gState = new PdfGState();
                gState.setStrokeOpacity(alpha);
                cb.setGState(gState);
            }
            cb.stroke();
            if (usedAlpha < 255) {
                cb.restoreState();
            }
        }
    }

    private void renderBigSVGItem(SubdividedEdgeBigItem item, RenderTarget target, PreviewProperties properties) {
        for (SortedEdgeWrapper edgeWrapper : item.edges) {
            Edge edge = edgeWrapper.edge;
            EdgeLayoutData data = (EdgeLayoutData) edge.getEdgeData().getLayoutData();
            Point2D.Double[] points = data.getSubdivisonPoints();

            Color color = new Color(data.getEdgeColor().getRed(), data.getEdgeColor().getGreen(), data.getEdgeColor().getBlue());
            SVGTarget svgTarget = (SVGTarget) target;
            Element edgeElem = svgTarget.createElement("polyline");
            edgeElem.setAttribute("stroke", svgTarget.toHexString(color));
            edgeElem.setAttribute("stroke-width", Float.toString(thickness * svgTarget.getScaleRatio()));
            edgeElem.setAttribute("stroke-opacity", ((forceAlpha ? intAlpha : color.getAlpha()) / 255f) + "");
            edgeElem.setAttribute("fill", "none");
            edgeElem.setAttribute("class", edge.toString());
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < points.length; i++) {
                if (i != 0) {
                    builder.append(",");
                }
                builder.append(points[i].x).append(" ").append(-points[i].y);
            }
            edgeElem.setAttribute("points", builder.toString());
            svgTarget.getTopElement(SVGTarget.TOP_EDGES).appendChild(edgeElem);
        }
    }

    private void renderBigProcessingItem(SubdividedEdgeBigItem item, RenderTarget target, PreviewProperties properties) {
        for (SortedEdgeWrapper edgeWrapper : item.edges) {
            Edge edge = edgeWrapper.edge;
            EdgeLayoutData data = (EdgeLayoutData) edge.getEdgeData().getLayoutData();
            Point2D.Double[] points = data.getSubdivisonPoints();

            PGraphics graphics = ((ProcessingTarget) target).getGraphics();

            graphics.noFill();
            graphics.strokeWeight(thickness);
            graphics.strokeCap(PGraphics.ROUND);
            graphics.stroke(data.getEdgeColor().getRed(), data.getEdgeColor().getGreen(), data.getEdgeColor().getBlue(), forceAlpha ? intAlpha : data.getEdgeColor().getAlpha());
            graphics.beginShape();
            for (int i = 0; i < points.length; i++) {
                float x1 = (float) points[i].x;
                float y1 = (float) points[i].y;
                graphics.vertex(x1, -y1);
            }
            graphics.endShape();
        }
    }

    private void renderBigAndComplexPDFItem(SubdividedEdgeBigItem item, RenderTarget target, PreviewProperties properties) {
        for (SortedEdgeWrapper edgeWrapper : item.edges) {
            Edge edge = edgeWrapper.edge;
            EdgeLayoutData data = (EdgeLayoutData) edge.getEdgeData().getLayoutData();
            Point2D.Double[] points = data.getSubdivisonPoints();

            Color color = data.getSubdivisionEdgeColor()[edgeWrapper.id];
            PDFTarget pdfTarget = (PDFTarget) target;
            PdfContentByte cb = pdfTarget.getContentByte();
            int i = edgeWrapper.id;
            if (i == points.length - 1) {
                continue;
            }
            cb.moveTo((float) points[i].x, (float) points[i].y);
            cb.lineTo((float) points[i + 1].x, (float) points[i + 1].y);
            cb.setRGBColorStroke(color.getRed(), color.getGreen(), color.getBlue());
            cb.setLineWidth(thickness);
            
            float usedAlpha = (forceAlpha ? intAlpha : color.getAlpha());
            if (usedAlpha < 255) {
                cb.saveState();
                float alpha = usedAlpha / 255f;
                PdfGState gState = new PdfGState();
                gState.setStrokeOpacity(alpha);
                cb.setGState(gState);
            }
            cb.stroke();
            if (usedAlpha < 255) {
                cb.restoreState();
            }
        }
    }

    private void renderBigAndComplexSVGItem(SubdividedEdgeBigItem item, RenderTarget target, PreviewProperties properties) {
        for (SortedEdgeWrapper edgeWrapper : item.edges) {
            Edge edge = edgeWrapper.edge;
            EdgeLayoutData data = (EdgeLayoutData) edge.getEdgeData().getLayoutData();
            Point2D.Double[] points = data.getSubdivisonPoints();
            if (edgeWrapper.id == points.length - 1) {
                continue;
            }

            Color color = data.getSubdivisionEdgeColor()[edgeWrapper.id];
            SVGTarget svgTarget = (SVGTarget) target;
            Element edgeElem = svgTarget.createElement("path");
            edgeElem.setAttribute("class", edge.getSource().getNodeData().getId() + " " + edge.getTarget().getNodeData().getId());
            edgeElem.setAttribute("d", String.format(Locale.ENGLISH, "M %f,%f L %f,%f",
                    points[edgeWrapper.id].x, points[edgeWrapper.id].y, points[edgeWrapper.id + 1].x, points[edgeWrapper.id + 1].y));
            edgeElem.setAttribute("stroke", svgTarget.toHexString(color));
            edgeElem.setAttribute("stroke-width", Float.toString(thickness * svgTarget.getScaleRatio()));
            edgeElem.setAttribute("stroke-opacity", ((forceAlpha ? intAlpha : color.getAlpha()) / 255f) + "");
            edgeElem.setAttribute("fill", "none");
            svgTarget.getTopElement(SVGTarget.TOP_EDGES).appendChild(edgeElem);
        }
    }

    private void renderBigAndComplexProcessingItem(SubdividedEdgeBigItem item, RenderTarget target, PreviewProperties properties) {
        for (SortedEdgeWrapper edgeWrapper : item.edges) {
            Edge edge = edgeWrapper.edge;
            EdgeLayoutData data = (EdgeLayoutData) edge.getEdgeData().getLayoutData();
            Point2D.Double[] points = data.getSubdivisonPoints();
            if (data.getSubdivisonPoints() == null || data.getSubdivisionEdgeColor() == null
                    || edgeWrapper.id == data.getSubdivisonPoints().length - 1) {
                continue;
            }
            PGraphics graphics = ((ProcessingTarget) target).getGraphics();

            graphics.noFill();
            graphics.strokeWeight(thickness);
            graphics.strokeCap(PGraphics.ROUND);
            Color color = data.getSubdivisionEdgeColor()[edgeWrapper.id];
            if (color == null) {
                continue;
            }
            graphics.stroke(color.getRed(), color.getGreen(), color.getBlue(), (forceAlpha ? intAlpha : color.getAlpha()));
            graphics.line((float) points[edgeWrapper.id].x, -(float) points[edgeWrapper.id].y, (float) points[edgeWrapper.id + 1].x, -(float) points[edgeWrapper.id + 1].y);
        }
    }

    @Override
    public PreviewProperty[] getProperties() {
        List<PreviewProperty> properties = new ArrayList<PreviewProperty>();
        /*
         * In pure PropertySheet it's possible to easy make nice combobox, looks
         * like this isn't possible with PreviewProperty, going to make a jpanel
         * for that soon
         */
        properties.add(PreviewProperty.createProperty(this,
                PreviewProperty.EDGE_LAYOUT_USE_RENDERER,
                PreviewProperty.RendererModes.class,
                "Renderer mode",
                "standart one-color renderer;gradient renderer; slow gradient renderer that requires precalculation",
                PreviewProperty.CATEGORY_EDGE_LAYOUT).setValue(PreviewProperty.RendererModes.SimpleRenderer));

        properties.add(PreviewProperty.createProperty(this,
                PreviewProperty.EDGE_LAYOUT_REFRESH_RATE,
                Integer.class,
                "Refresh rate",
                "Refresh every n-th iteration of edge layout",
                PreviewProperty.CATEGORY_EDGE_LAYOUT).setValue(1));

        properties.add(PreviewProperty.createProperty(this,
                PreviewProperty.EDGE_LAYOUT_EDGE_THICKNESS,
                Double.class,
                "Edge thickness",
                "Thickness of edge",
                PreviewProperty.CATEGORY_EDGE_LAYOUT).setValue(0.5));

        /*
         * I think it's more comfortably to control it in that way, rather than
         * with gradient slider, though probably option woud be nice
         */
        properties.add(PreviewProperty.createProperty(this,
                PreviewProperty.EDGE_LAYOUT_EDGE_TRANSPARENCY,
                Double.class,
                "Edge transparency",
                "Transparency for edge",
                PreviewProperty.CATEGORY_EDGE_LAYOUT).setValue(0.1));
        
        properties.add(PreviewProperty.createProperty(this,
                PreviewProperty.EDGE_LAYOUT_EDGE_TRANSPARENCY_FORCE,
                Boolean.class,
                "Force edge transparency",
                "Forces user edge transparency for non simple rendering modes",
                PreviewProperty.CATEGORY_EDGE_LAYOUT).setValue(false));

        PropertyEditorManager.registerEditor(ColorWrapper.class, DependantColorPropertyEditor.class);
        properties.add(PreviewProperty.createProperty(this,
                PreviewProperty.EDGE_LAYOUT_SIMPLE_RENDERER_COLOR,
                ColorWrapper.class,
                "Simple Renderer Color",
                "Renderer color for 0th rendererer mode",
                PreviewProperty.CATEGORY_EDGE_LAYOUT).setValue(new Color(0f, 0f, 0.5f)));

        properties.add(PreviewProperty.createProperty(this,
                PreviewProperty.EDGE_LAYOUT_USE_PERCENTAGE_INSTEAD_OF_LINEAR_SCALE,
                Boolean.class,
                "Use relative value",
                "Use relative value rather then absolute, i.e. edge gets colors in the middle of slider if it is a median",
                PreviewProperty.CATEGORY_EDGE_LAYOUT).setValue(true));

        properties.add(PreviewProperty.createProperty(this,
                PreviewProperty.EDGE_LAYOUT_PRECALCULATE_POINTS,
                Boolean.class,
                "Precalculate points",
                "Slowly precalculate points for 2nd renderer, needs to be re-runned",
                PreviewProperty.CATEGORY_EDGE_LAYOUT).setValue(false));

        return properties.toArray(new PreviewProperty[0]);
    }

    @Override
    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        return (item instanceof SubdividedEdgeItem
                || item instanceof SubdividedEdgeBigItem && !properties.getValue(PreviewProperty.EDGE_LAYOUT_USE_RENDERER).equals(PreviewProperty.RendererModes.SimpleRenderer));
    }

    @Override
    public boolean needsItemBuilder(ItemBuilder itemBuilder, PreviewProperties properties) {
        return (itemBuilder instanceof SubdividedEdgeItemBuilder || itemBuilder instanceof SubdividedEdgeBigItemBuilder);
    }
}

class ColorWrapper extends Color {

    ColorWrapper(int r, int g, int b) {
        super(r, g, b);
    }
}