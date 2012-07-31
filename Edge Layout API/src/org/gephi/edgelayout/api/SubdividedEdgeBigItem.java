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
import org.gephi.preview.plugin.items.AbstractItem;

/**
 *
 * @author megaterik
 */
public class SubdividedEdgeBigItem extends AbstractItem implements Item {

    ArrayList<SortedEdgeWrapper> edges;

    SubdividedEdgeBigItem(Object source, String type) {
        super(source, type);
        edges = new ArrayList<SortedEdgeWrapper>();
        for (Edge edge : ((Graph) source).getEdges()) {
            if (edge.getEdgeData().getLayoutData() instanceof EdgeLayoutData) {
                edges.add(new SortedEdgeWrapper(edge, ((EdgeLayoutData) edge.getEdgeData().getLayoutData()).getEdgeSortOrder()));
            }
        }
        Collections.sort(edges);
    }
}

class SortedEdgeWrapper implements Comparable<SortedEdgeWrapper> {

    Edge edge;
    double sort;

    public SortedEdgeWrapper(Edge edge, double sort) {
        this.edge = edge;
        this.sort = sort;
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
