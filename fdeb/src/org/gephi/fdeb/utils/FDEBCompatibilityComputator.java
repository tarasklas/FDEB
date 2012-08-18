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
package org.gephi.fdeb.utils;

import java.awt.geom.Point2D;
import org.gephi.graph.api.Edge;
import processing.core.PVector;

/**
 *
 * @author Taras Klaskovsky <megaterik@gmail.com>
 */
public class FDEBCompatibilityComputator {

    private boolean angleCompatibility = true;
    private boolean scaleCompatibility = true;
    private boolean positionCompatibility = true;
    private boolean visibilityCompatibility = true;

    public double calculateCompatibility(Edge aEdge, Edge bEdge) {
        PVector a = new PVector(aEdge.getTarget().getNodeData().x() - aEdge.getSource().getNodeData().x(),
                aEdge.getTarget().getNodeData().y() - aEdge.getSource().getNodeData().y());
        PVector b = new PVector(bEdge.getTarget().getNodeData().x() - bEdge.getSource().getNodeData().x(),
                bEdge.getTarget().getNodeData().y() - bEdge.getSource().getNodeData().y());
        double compatibility = 1.0;
        if (angleCompatibility) {
            if (affectedByDirection) {
                compatibility *= angleCompatibilityAffectedByDirection(a, b);
            } else {
                compatibility *= angleCompatibility(a, b);
            }
        }
        if (scaleCompatibility) {
            compatibility *= scaleCompatibility(a, b);
        }
        if (positionCompatibility) {
            compatibility *= positionCompatibility(a, b, aEdge, bEdge);
        }
        if (visibilityCompatibility) {
            if (!visibilityApply || compatibility >= 0.9) {
                compatibility *= visibilityCompatibility(aEdge, bEdge);
            }
        }
        //System.err.println(compatibility);
        return compatibility;
    }

    private double angleCompatibility(PVector a, PVector b) {
        double compatiblity = Math.abs(a.dot(b) / (a.mag() * b.mag()));

        return compatiblity;
    }

    private double angleCompatibilityAffectedByDirection(PVector a, PVector b) {
        double compatibility = (a.dot(b) / (a.mag() * b.mag()) + 1.0) / 2.0;
//        assert (compatibility >= 0);
//        assert (compatibility <= 1);
        return compatibility;
    }

    private double scaleCompatibility(PVector a, PVector b) {
        double lavg = (a.mag() + b.mag()) / 2.0;
        double compatibility = 2.0 / (lavg / Math.min(a.mag(), b.mag()) + Math.max(a.mag(), b.mag()) / lavg);
//        assert (compatibility >= 0);
//        assert (compatibility <= 1);
        return compatibility;
    }

    private double positionCompatibility(PVector a, PVector b, Edge ae, Edge be) {
        PVector aMid = new PVector((ae.getSource().getNodeData().x() + ae.getTarget().getNodeData().x()) / 2.0f,
                (ae.getSource().getNodeData().y() + ae.getTarget().getNodeData().y()) / 2.0f);
        PVector bMid = new PVector((be.getSource().getNodeData().x() + be.getTarget().getNodeData().x()) / 2.0f,
                (be.getSource().getNodeData().y() + be.getTarget().getNodeData().y()) / 2.0f);
        double lavg = (a.mag() + b.mag()) / 2.0;
        double compatibility = lavg / (lavg + aMid.dist(bMid));
//        assert (compatibility >= 0);
//        assert (compatibility <= 1);
        return compatibility;
    }

    private double visibilityCompatibility(Edge aEdge, Edge bEdge) {
        Point2D.Float as = new Point2D.Float(aEdge.getSource().getNodeData().x(), aEdge.getSource().getNodeData().y());
        Point2D.Float af = new Point2D.Float(aEdge.getTarget().getNodeData().x(), aEdge.getTarget().getNodeData().y());

        Point2D.Float bs = new Point2D.Float(bEdge.getSource().getNodeData().x(), bEdge.getSource().getNodeData().y());
        Point2D.Float bf = new Point2D.Float(bEdge.getTarget().getNodeData().x(), bEdge.getTarget().getNodeData().y());
        double compatibility = Math.min(visibilityCompatibility(as, af, bs, bf), visibilityCompatibility(bs, bf, as, af));

        try {
//            assert (compatibility >= 0);
//            assert (compatibility <= 1);
        } catch (AssertionError ex) {
            return 0;
        }
        return compatibility;
    }

    private double visibilityCompatibility(Point2D.Float p0, Point2D.Float p1, Point2D.Float q0, Point2D.Float q1) {
        Point2D.Float i0 = projectPointToLine(p0.x, p0.y, p1.x, p1.y, q0.x, q0.y);
        Point2D.Float i1 = projectPointToLine(p0.x, p0.y, p1.x, p1.y, q1.x, q1.y);
        Point2D.Float im = new Point2D.Float((i0.x + i1.x) / 2.0f, (i0.y + i1.y) / 2.0f);
        Point2D.Float pm = new Point2D.Float((p0.x + p1.x) / 2.0f, (p0.y + p1.y) / 2.0f);
        return Math.max(0, 1 - 2 * pm.distance(im) / (i0.distance(i1)));
    }

    /**
     * See http://www.exaflop.org/docs/cgafaq/cga1.html
     */
    public static Point2D.Float projectPointToLine(double x1, double y1, double x2, double y2, double x, double y) {
        double L = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        double r = ((y1 - y) * (y1 - y2) - (x1 - x) * (x2 - x1)) / (L * L);
        return new Point2D.Float((float) (x1 + r * (x2 - x1)), (float) (y1 + r * (y2 - y1)));
    }

    public boolean isAngleCompatibility() {
        return angleCompatibility;
    }

    public void setAngleCompatibility(boolean angleCompatibility) {
        this.angleCompatibility = angleCompatibility;
    }

    public boolean isPositionCompatibility() {
        return positionCompatibility;
    }

    public void setPositionCompatibility(boolean positionCompatibility) {
        this.positionCompatibility = positionCompatibility;
    }

    public boolean isScaleCompatibility() {
        return scaleCompatibility;
    }

    public void setScaleCompatibility(boolean scaleCompatibility) {
        this.scaleCompatibility = scaleCompatibility;
    }

    public boolean isVisibilityCompatibility() {
        return visibilityCompatibility;
    }

    public void setVisibilityCompatibility(boolean visibilityCompatibility) {
        this.visibilityCompatibility = visibilityCompatibility;
    }
    private boolean visibilityApply = false;
    private boolean affectedByDirection = false;

    public boolean isVisibilityApply() {
        return visibilityApply;
    }

    public void setVisibilityApply(boolean visibilityApply) {
        this.visibilityApply = visibilityApply;
    }

    public boolean isAffectedByDirection() {
        return affectedByDirection;
    }

    public void setAffectedByDirection(boolean affectedByDirection) {
        this.affectedByDirection = affectedByDirection;
    }
}
