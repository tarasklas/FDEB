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
package org.gephi.example;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;
import org.gephi.edgelayout.spi.AbstractEdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayoutBuilder;
import org.gephi.edgelayout.spi.EdgeLayoutProperty;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;

/**
 *
 * @author Taras Klaskovsky <megaterik@gmail.com>
 */
public class PipesBundler extends AbstractEdgeLayout implements EdgeLayout {

    private int numberOfIterations;
    private double randomStep;
    private double probabilityOfNewBend;
    private int leftIterations;
    private Random random = new Random();

    PipesBundler(EdgeLayoutBuilder builder) {
        super(builder);
    }

    @Override
    public void initAlgo() {
        for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
            edge.getEdgeData().setLayoutData(new PipesLayoutData(
                    edge.getSource().getNodeData().x(), edge.getSource().getNodeData().y(),
                    edge.getTarget().getNodeData().x(), edge.getTarget().getNodeData().y()));
        }

        leftIterations = numberOfIterations;
        cancel = false;
        setConverged(false);
    }

    @Override
    public void goAlgo() {
        for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
            PipesLayoutData data = edge.getEdgeData().getLayoutData();
            ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
            Point2D.Double[] oldpoints = data.getSubdivisonPoints();
            points.add(oldpoints[0]);
            for (int i = 1; i < oldpoints.length; i++) {
                if (random.nextDouble() <= probabilityOfNewBend) {
                    double x1 = points.get(points.size() - 1).x;
                    double y1 = points.get(points.size() - 1).y;

                    double x2 = oldpoints[i].x;
                    double y2 = oldpoints[i].y;

                    // It's parallel to coordinate axes
                    if (Math.abs(y2 - y1) <= Math.abs(x2 - x1)) {
                        double moveTo = randomStep * random.nextDouble();
                        if (random.nextBoolean()) {
                            moveTo *= -1;
                        }

                        double xd1 = x1 + (x2 - x1) / 3;
                        double xd2 = x1 + (x2 - x1) * 2 / 3;
                        points.add(new Point2D.Double(xd1, y1));
                        points.add(new Point2D.Double(xd1, y1 + moveTo));
                        points.add(new Point2D.Double(xd2, y1 + moveTo));
                        points.add(new Point2D.Double(xd2, y1));
                    } else {
                        double moveTo = randomStep * random.nextDouble();
                        if (random.nextBoolean()) {
                            moveTo *= -1;
                        }

                        double yd1 = y1 + (y2 - y1) / 3;
                        double yd2 = y1 + (y2 - y1) * 2 / 3;
                        points.add(new Point2D.Double(x1, yd1));
                        points.add(new Point2D.Double(x1 + moveTo, yd1));
                        points.add(new Point2D.Double(x1 + moveTo, yd2));
                        points.add(new Point2D.Double(x1, yd2));
                    }
                }
                points.add(oldpoints[i]);
            }
            data.updatePoints(points.toArray(new Point2D.Double[0]));
        }

        leftIterations--;
        if (leftIterations == 0) {
            setConverged(true);
        }
    }

    @Override
    public void endAlgo() {
        modifyAlgo();
    }

    @Override
    public EdgeLayoutProperty[] getProperties() {
        ArrayList<EdgeLayoutProperty> properties = new ArrayList<EdgeLayoutProperty>();

        try {
            properties.add(EdgeLayoutProperty.createProperty(this,
                    Integer.class,
                    "Number of cycles",
                    "General options",
                    "Higher number means higher number of pipe bend",
                    "getNumberOfIterations", "setNumberOfIterations"));

            properties.add(EdgeLayoutProperty.createProperty(this,
                    Double.class,
                    "Size",
                    "General options",
                    "Maximum size of one random bend",
                    "getRandomStep", "setRandomStep"));

            properties.add(EdgeLayoutProperty.createProperty(this,
                    Double.class,
                    "Probability",
                    "General options",
                    "Probability of edge bend",
                    "getProbabilityOfNewBend", "setProbabilityOfNewBend"));
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }

        return properties.toArray(new EdgeLayoutProperty[0]);
    }

    @Override
    public void resetPropertiesValues() {
        numberOfIterations = 1;
        randomStep = 50.0;
        probabilityOfNewBend = 0.2;
    }

    @Override
    public void modifyAlgo() {
        /*
         * To calculate color we need to set maxLength
         */
        double maxLength = 0;
        for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
            PipesLayoutData data = edge.getEdgeData().getLayoutData();
            maxLength = Math.max(maxLength, data.getTotalLength());
        }
        for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
            PipesLayoutData data = edge.getEdgeData().getLayoutData();
            data.setMaxLength(maxLength);
        }
    }

    @Override
    public void removeLayoutData() {
        for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
            edge.getEdgeData().setLayoutData(null);
        }
    }

    @Override
    public boolean shouldRefreshPreview(int refreshRate) {
        //refresh after every iteration since number of edges is relatively low
        return true;
    }

    /*
     * Setters and getters for reflection for getProperties() -- note Integer
     * instead of int
     */
    public Integer getNumberOfIterations() {
        return numberOfIterations;
    }

    public void setNumberOfIterations(Integer numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }

    public Double getRandomStep() {
        return randomStep;
    }

    public void setRandomStep(Double randomStep) {
        this.randomStep = randomStep;
    }

    public Double getProbabilityOfNewBend() {
        return probabilityOfNewBend;
    }

    public void setProbabilityOfNewBend(Double probabilityOfNewBend) {
        this.probabilityOfNewBend = probabilityOfNewBend;
    }
}
