/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.barnes_hut;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;

/**
 *
 * @author megaterik
 */
public class QuadNode {

    public QuadNode lu, ld, ru, rd;
    public float xl, xr, yl, yr;
    public boolean isLeaf;
    public Object storedElement;
    public Point2D.Float storedElementAttachmentPoint;
    public int size;
    
    public Point2D.Float center;
    public Point2D.Float sum;
    
    public int height;

    public QuadNode(float xl, float yl, float xr, float yr) {
        this.xl = xl;
        this.yl = yl;
        this.xr = xr;
        this.yr = yr;
        isLeaf = true;
        center = new Point2D.Float();
        sum = new Point2D.Float();
        size = height = 1;
    }

    public void expand() {
        lu = new QuadNode(xl, yl, (xl + xr) / 2, (yl + yr) / 2);
        ld = new QuadNode(xl, (yl + yr) / 2, (xl + xr) / 2, yr);
        ru = new QuadNode((xl + xr) / 2, yl, xr, (yl + yr) / 2);
        rd = new QuadNode((xl + xr) / 2, (yl + yr) / 2, xr, yr);
        isLeaf = false;
    }

    public void push(Point2D.Float atttachmentPoint, Object obj) {
        if (isLeaf && storedElement == null) {
            storedElement = obj;
            storedElementAttachmentPoint = atttachmentPoint;
        } else {
            if (isLeaf) {
                expand();
                if (storedElement != null) {
                    push(storedElementAttachmentPoint, storedElement);
                }
                storedElement = null;
                storedElementAttachmentPoint = null;
            }
            chooseDirection(atttachmentPoint).push(atttachmentPoint, obj);
        }
        
        if (!isLeaf) {
            height = Math.max(height, ru.height + 1);
            height = Math.max(height, rd.height + 1);
            height = Math.max(height, lu.height + 1);
            height = Math.max(height, ld.height + 1);
            size = ru.size + rd.size + lu.size + ld.size + 1;
            
            sum.x += atttachmentPoint.x;
            sum.y += atttachmentPoint.y;
            center.setLocation(sum.x / size, sum.y / size);
        }
        else
        {
            size = height = 1;
        }
    }

    private QuadNode chooseDirection(Point2D.Float atttachmentPoint) {
        if (atttachmentPoint.x < (xl + xr) / 2) {
            if (atttachmentPoint.y <= (yl + yr) / 2) {
                return lu;
            } else {
                return ld;
            }
        } else if (atttachmentPoint.y <= (yl + yr) / 2) {
            return ru;
        } else {
            return rd;
        }
    }
}
