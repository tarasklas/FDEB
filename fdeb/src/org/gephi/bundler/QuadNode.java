/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.bundler;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author megaterik
 */
public class QuadNode {

    public QuadNode lu, ld, ru, rd;
    public double xl, xr, yl, yr;
    public Point2D.Double point;

    public QuadNode(Point2D.Double[] points) {
        this.xl = points[0].x;
        this.yl = points[0].y;
        this.xr = points[0].x;
        this.yr = points[0].y;
        for (Point2D.Double point : points) {
            xl = Math.min(xl, point.x);
            xr = Math.max(xr, point.x);

            yl = Math.min(yl, point.y);
            yr = Math.max(yr, point.y);
        }
        tryToExpand(points);
    }

    private void tryToExpand(Point2D.Double[] points) {
        if (points.length == 1) {
            point = points[0];
        } else {
            Point2D.Double[] luElements, ldElements, ruElements, rdElements;
            Arrays.sort(points, new CompareByX());
            Point2D.Double[] leftPart = Arrays.copyOfRange(points, 0, points.length / 2);
            Point2D.Double[] rightPart = Arrays.copyOfRange(points, points.length / 2, points.length);
            Arrays.sort(leftPart, new CompareByY());
            luElements = Arrays.copyOfRange(leftPart, 0, leftPart.length / 2);
            ldElements = Arrays.copyOfRange(leftPart, leftPart.length / 2, leftPart.length);
            Arrays.sort(rightPart, new CompareByY());
            ruElements = Arrays.copyOfRange(rightPart, 0, rightPart.length / 2);
            rdElements = Arrays.copyOfRange(rightPart, rightPart.length / 2, rightPart.length);
            if (luElements.length > 0) {
                lu = new QuadNode(luElements);
            }
            if (ldElements.length > 0) {
                ld = new QuadNode(ldElements);
            }
            if (ruElements.length > 0) {
                ru = new QuadNode(ruElements);
            }
            if (rdElements.length > 0) {
                rd = new QuadNode(rdElements);
            }
        }
    }

    double getNumberOfPointsInRange(Point2D.Double point, double radius) {
        if (this.point != null) {
            if (Point2D.distance(point.x, point.y, this.point.x, this.point.y) <= radius) {
                return 1;
            } else {
                return 0;
            }
        }

        double distance = Integer.MAX_VALUE;
        if (point.x >= xl && point.x <= xr) {
            if (point.y >= yl && point.y <= yr) {
                distance = 0;
            }
        }

        if (point.x >= xl && point.x <= xr) {
            if (point.y <= yl) {
                distance = Math.min(distance, yl - point.y);
            } else {
                distance = Math.min(distance, point.y - yr);
            }
        }

        if (point.y >= yl && point.y <= yr) {
            if (point.x <= xl) {
                distance = Math.min(distance, xl - point.x);
            } else {
                distance = Math.min(distance, point.x - xr);
            }
        }

        distance = Math.min(distance, Point2D.distance(point.x, point.y, xl, yl));
        distance = Math.min(distance, Point2D.distance(point.x, point.y, xr, yl));
        distance = Math.min(distance, Point2D.distance(point.x, point.y, xl, yr));
        distance = Math.min(distance, Point2D.distance(point.x, point.y, xr, yr));

        if (distance > radius) {
            return 0;
        } else {
            int res = 0;
            if (ld != null) {
                res += ld.getNumberOfPointsInRange(point, radius);
            }
            if (lu != null) {
                res += lu.getNumberOfPointsInRange(point, radius);
            }
            if (rd != null) {
                res += rd.getNumberOfPointsInRange(point, radius);
            }
            if (ru != null) {
                res += ru.getNumberOfPointsInRange(point, radius);
            }
            return res;
        }
    }
}

class CompareByX implements Comparator<Point2D.Double> {

    @Override
    public int compare(Point2D.Double o1, Point2D.Double o2) {
        if (o1.x < o2.x) {
            return -1;
        }
        if (o1.x > o2.x) {
            return 1;
        }
        return 0;
    }
}

class CompareByY implements Comparator<Point2D.Double> {

    @Override
    public int compare(Point2D.Double o1, Point2D.Double o2) {
        if (o1.y < o2.y) {
            return -1;
        }
        if (o1.y > o2.y) {
            return 1;
        }
        return 0;
    }
}
