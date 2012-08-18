/*
 Copyright 2008-2012 Gephi
 Authors : Taras Klaskovsky <megaterik@gmail.com>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.bundler;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author Taras Klaskovsky <megaterik@gmail.com>
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
