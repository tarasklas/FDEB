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

    public Point2D.Double[] getSubdivisonPoints();

    public double getEdgeSortOrder();
    public Color getEdgeColor();

    public double[] getSubdivisionEdgeSortOrder();
    public Color[] getSubdivisionEdgeColor();
}
