/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.edgelayout.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.gephi.edgelayout.spi.EdgeLayoutData;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.plugin.items.AbstractItem;
import org.openide.util.Lookup;

/**
 *
 * @author megaterik
 */
public class SubdividedEdgeBigItem extends AbstractItem implements Item {

    ArrayList<SortedEdgeWrapper> edges;
    boolean ready = false;

    SubdividedEdgeBigItem(Object source, String type) {
        super(source, type);
        edges = new ArrayList<SortedEdgeWrapper>();
        if (Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getValue(PreviewProperty.EDGE_LAYOUT_USE_RENDERER) == PreviewProperty.RendererModes.GradientRenderer) {
            for (Edge edge : ((Graph) source).getEdges()) {
                if (edge.getEdgeData().getLayoutData() instanceof EdgeLayoutData) {
                    edges.add(new SortedEdgeWrapper(edge, ((EdgeLayoutData) edge.getEdgeData().getLayoutData()).getEdgeSortOrder()));
                }
            }
        } else {
            for (Edge edge : ((Graph) source).getEdges()) {
                if (edge.getEdgeData().getLayoutData() instanceof EdgeLayoutData) {
                    EdgeLayoutData data = edge.getEdgeData().getLayoutData();
                    double[] sort = data.getSubdivisionEdgeSortOrder();
                    if (sort != null) {
                        for (int i = 0; i < sort.length; i++) {
                            try {
                                edges.add(new SortedEdgeWrapper(edge, sort[i], i));
                            } catch (NullPointerException ex) {
                                ex.printStackTrace();;
                            }
                        }
                    }
                }
            }
        }
        Collections.sort(edges);
    }

    public boolean isReady() {
        if (edges == null || edges.size() == 0) {
            return false;
        }
        if ((Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getValue(PreviewProperty.EDGE_LAYOUT_USE_RENDERER)
                == PreviewProperty.RendererModes.GradientRenderer)) {
            for (SortedEdgeWrapper wrapper : edges) {
                if (wrapper.edge.getEdgeData().getLayoutData() != null
                        && ((EdgeLayoutData) wrapper.edge.getEdgeData().getLayoutData()).getEdgeColor() == null) {
                    return false;
                }
            }
        }
        if ((Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getValue(PreviewProperty.EDGE_LAYOUT_USE_RENDERER)
                == PreviewProperty.RendererModes.GradientComplexRenderer)) {
            for (SortedEdgeWrapper wrapper : edges) {
                if (wrapper.edge.getEdgeData().getLayoutData() != null
                        && ((EdgeLayoutData) wrapper.edge.getEdgeData().getLayoutData()).getSubdivisionEdgeColor() == null) {
                    return false;
                }
            }
        }
        return true;
    }
}

class SortedEdgeWrapper implements Comparable<SortedEdgeWrapper> {

    Edge edge;
    double sort;
    int id;

    public SortedEdgeWrapper(Edge edge, double sort) {
        this.edge = edge;
        this.sort = sort;
    }

    public SortedEdgeWrapper(Edge edge, double sort, int id) {
        this.edge = edge;
        this.sort = sort;
        this.id = id;
    }

    @Override
    public int compareTo(SortedEdgeWrapper o) {
        if (this.sort > o.sort) {
            return 1;
        } else if (this.sort < o.sort) {
            return -1;
        } else {
            return 0;
        }
    }
}