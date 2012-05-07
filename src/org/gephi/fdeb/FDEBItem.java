/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.fdeb;

import org.gephi.graph.api.Edge;
import org.gephi.preview.api.Item;
import org.gephi.preview.plugin.items.AbstractItem;

/**
 *
 * @author megaterik
 */
public class FDEBItem extends AbstractItem implements Item{
    
    FDEBItem(Object source, String type, Edge edge)
    {
        super(source, type);
        data.put("edge", edge);
    }
    
}
