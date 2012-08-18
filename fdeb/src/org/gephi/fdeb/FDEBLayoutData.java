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
package org.gephi.fdeb;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import org.gephi.edgelayout.spi.EdgeLayoutData;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.ui.components.gradientslider.GradientSlider;
import org.openide.util.Lookup;

/**
 *
 * @author Taras Klaskovsky <megaterik@gmail.com>
 */
public class FDEBLayoutData implements EdgeLayoutData {

    public Point.Double[] newSubdivisionPoints;//to store changes before merging them
    public Point.Double[] subdivisionPoints;
    public double length;
    public static final double eps = 1e-7;
    public FDEBCompatibilityRecord[] similarEdges;
    public double intensity;//for edge
    public double[] intensities; //for each subdivision point
    public Color color; //for edge
    public Color[] colors; //for each subdivision point

    public FDEBLayoutData(double startPointX, double startPointY, double endPointX, double endPointY) {
        length = Point.Double.distance(startPointX, startPointY, endPointX, endPointY);
        if (length < eps) {
            length = 0;
        }
        subdivisionPoints = new Point.Double[3];
        subdivisionPoints[0] = new Point.Double(startPointX, startPointY);
        subdivisionPoints[1] = new Point.Double((startPointX + endPointX) / 2, (startPointY + endPointY) / 2);
        subdivisionPoints[2] = new Point.Double(endPointX, endPointY);
        color = null;
    }


    private Color pickGradientFromLookup(double f) {
        /*
         * That is likely woudn't work with toolkit, since gradientslider is set
         * in EdgeLayoutWindowTopComponent.
         */
        GradientSlider gradientSlider = Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getValue(PreviewProperty.EDGE_LAYOUT_GRADIENT_SLIDER_LOCATION);
        assert (gradientSlider != null);
        return (Color) gradientSlider.getValue((float) f);
    }

    public void updateColor(ArrayList<Double> find) {
        double f;
        if (Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getBooleanValue(PreviewProperty.EDGE_LAYOUT_USE_PERCENTAGE_INSTEAD_OF_LINEAR_SCALE)) {
            f = (double) Math.abs(Collections.binarySearch(find, intensity + 1)) / find.size();
        } else {
            f = intensity / find.get(find.size() - 1);
        }
        if (f > 1) {
            f = 1;
        }
        color = pickGradientFromLookup(f);
    }

    public void updateColors(ArrayList<Double> find) {
        if (colors == null || colors.length != subdivisionPoints.length) {
            colors = new Color[subdivisionPoints.length];
        }
        for (int i = 0; i < colors.length; i++) {
            double f;
            if (Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getBooleanValue(PreviewProperty.EDGE_LAYOUT_USE_PERCENTAGE_INSTEAD_OF_LINEAR_SCALE)) {
                f = (double) Math.abs(Collections.binarySearch(find, intensities[i])) / find.size();
            } else {
                f = (intensities[i]) / find.get(find.size() - 1);
            }
            if (f > 1) {
                f = 1;
            }
            colors[i] = pickGradientFromLookup(f);
        }
    }

    @Override
    public Point.Double[] getSubdivisonPoints() {
        return subdivisionPoints;
    }

    @Override
    public double getEdgeSortOrder() {
        return intensity;
    }

    @Override
    public Color getEdgeColor() {
        return color;
    }

    @Override
    public Color[] getSubdivisionEdgeColor() {
        return colors;
    }

    @Override
    public double[] getSubdivisionEdgeSortOrder() {
        return intensities;
    }
}
