/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.edgelayout.api;

import java.util.ArrayList;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.preview.api.Item;
import org.gephi.preview.spi.ItemBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author megaterik
 */
@ServiceProvider(service = ItemBuilder.class)
public class SubdividedEdgeItemBuilder implements ItemBuilder {

    @Override
    public Item[] getItems(Graph graph, AttributeModel attributeModel) {
        ArrayList<SubdividedEdgeItem> items = new ArrayList<SubdividedEdgeItem>();
        for (Edge edge : graph.getEdges()) {
            if (!edge.isSelfLoop()) {
                items.add(new SubdividedEdgeItem(edge.getEdgeData().getLayoutData(), "Divided edge", edge));
            }
        }
        return items.toArray(new Item[0]);
    }

    @Override
    public String getType() {
        return "FDEB curve";
    }
}
