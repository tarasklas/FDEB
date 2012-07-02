/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.fdeb;

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
public class FDEBItemBuilder implements ItemBuilder{

    @Override
    public Item[] getItems(Graph graph, AttributeModel attributeModel) {
        ArrayList<FDEBItem> items = new ArrayList<FDEBItem>();
        for (Edge edge : graph.getEdges())
            if (!edge.isSelfLoop())
                items.add(new FDEBItem(edge.getEdgeData().getLayoutData(), "FDEB curve", edge));
        return items.toArray(new Item[0]);
    }

    @Override
    public String getType() {
        return "FDEB curve";
    }
    
}
