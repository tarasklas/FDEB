/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.edgelayout.spi;

import java.awt.geom.Point2D;
import org.gephi.graph.spi.LayoutData;

/**
 *
 * @author megaterik
 */
public interface EdgeLayoutData extends LayoutData {

    public Point2D.Double[] getSubdivisonPoints();
    /*
     * Here will be also getter for the curved-straight edges options
     */
}
