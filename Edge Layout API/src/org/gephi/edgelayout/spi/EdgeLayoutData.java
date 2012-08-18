/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.edgelayout.spi;

import java.awt.Color;
import java.awt.geom.Point2D;
import org.gephi.graph.spi.LayoutData;

/**
 *
 * @author megaterik
 */
public interface EdgeLayoutData extends LayoutData {

    /**
     * Returns result of edge layout algorithm, polyline that are being used in all renderer modes. 
     * 
     */
    public Point2D.Double[] getSubdivisonPoints();

    /**
     * Edges with lower value will be rendered earlier with <code>getEdgeColor()</code> as color.
     * @return order for gradient renderer.
     */
    public double getEdgeSortOrder();
    
    /**
     * Color for gradient renderer. See <code>getEdgeSortOrder()</code>.
     * @return color for gradient renderer. 
     */
    public Color getEdgeColor();

    /**
     * Edges with lower value will be rendered earlier with <code>getSubdivisionEdgeColor()</code> as color.
     * @return order for complex gradient renderer. 
     */
    public double[] getSubdivisionEdgeSortOrder();
    
    /**
     * @return color for complex gradient renderer. See <code>getSubdivisionEdgeSortOrder()</code>.
     */
    public Color[] getSubdivisionEdgeColor();
}
