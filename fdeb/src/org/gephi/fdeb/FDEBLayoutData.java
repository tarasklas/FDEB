/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.fdeb;

import java.awt.Point;
import org.gephi.graph.spi.LayoutData;


public class FDEBLayoutData implements LayoutData{
    public Point.Double[] newSubdivisionPoints;//to store changes before merging them
    public Point.Double[] subdivisionPoints;
    public double length;
    public static final double eps = 1e-7;
    public FDEBCompatibilityRecord[] similarEdges;

    public FDEBLayoutData(double startPointX, double startPointY, double endPointX, double endPointY) {
        length = Point.Double.distance(startPointX, startPointY, endPointX, endPointY);
        if (length < eps) 
            length = 0;
        subdivisionPoints = new Point.Double[3];
        subdivisionPoints[0] = new Point.Double(startPointX, startPointY);
        subdivisionPoints[1] = new Point.Double((startPointX + endPointX) / 2, (startPointY + endPointY) / 2);
        subdivisionPoints[2] = new Point.Double(endPointX, endPointY);
    }
}
