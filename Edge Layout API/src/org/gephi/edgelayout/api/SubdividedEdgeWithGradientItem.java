/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.edgelayout.api;

import java.awt.Color;
import org.gephi.graph.api.Edge;
import org.gephi.preview.api.Item;
import org.gephi.preview.plugin.items.AbstractItem;

/**
 *
 * @author megaterik
 */
public class SubdividedEdgeWithGradientItem extends AbstractItem implements Item {
    
    int[] intervalIntensity;
    Color[] colors;
    SubdividedEdgeWithGradientItem(Object source, String type, Edge edge) {
        super(source, type);
        data.put("edge", edge);
    }
}
