/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.edgelayout.spi;

import org.gephi.graph.api.GraphModel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;


public interface EdgeLayout extends Layout{
    /*
     * Called when it's possible to change something without full recalculation
     */
    public void modifyAlgo();
    
    
    public void removeLayoutData();
}
