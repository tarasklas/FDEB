/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.edgelayout.api;

import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Graph;
import org.gephi.preview.api.Item;
import org.gephi.preview.spi.ItemBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author megaterik
 */
@ServiceProvider(service = ItemBuilder.class)
public class SubdividedEdgeBigItemBuilder implements ItemBuilder {

    @Override
    public Item[] getItems(Graph graph, AttributeModel attributeModel) {
        Item[] item = new Item[1];
        item[0] = new SubdividedEdgeBigItem(graph, "FDEB gradient curve");
        return item;
    }

    @Override
    public String getType() {
        return "FDEB gradient curve";
    }
}
