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

import java.util.Arrays;
import org.gephi.edgelayout.spi.EdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayoutBuilder;
import org.gephi.fdeb.FDEBLayoutData;
import org.gephi.fdeb.utils.FDEBUtilities;
import org.gephi.graph.api.Edge;
import org.gephi.utils.longtask.spi.LongTask;

/**
 *
 * @author Taras Klaskovsky <megaterik@gmail.com>
 */
public class FDEBBundler extends FDEBAbstractBundler implements EdgeLayout, LongTask {

    public FDEBBundler(EdgeLayoutBuilder layoutBuilder) {
        super(layoutBuilder);
    }
    long startTime;
    long endTime;
    /*
     * Get parameters and init structures
     */

    @Override
    public void initAlgo() {
        startTime = System.currentTimeMillis();
        for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
            edge.getEdgeData().setLayoutData(
                    new FDEBLayoutData(edge.getSource().getNodeData().x(), edge.getSource().getNodeData().y(),
                    edge.getTarget().getNodeData().x(), edge.getTarget().getNodeData().y()));
        }
        cycle = 1;
        setConverged(false);
        subdivisionPointsPerEdge = 1;//start and end doesnt count
        stepSize = stepSizeAtTheBeginning;
        iterationsPerCycle = iterationsPerCycleAtTheBeginning;
        System.out.println("K " + springConstant);

        createCompatibilityLists();
    }

    @Override
    public void goAlgo() {
        if (cancel) {
            return;
        }

        System.err.println("Next iteration");
        for (int step = 0; step < iterationsPerCycle; step++) {
            for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
                if (cancel) {
                    return;
                }
                ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).newSubdivisionPoints = Arrays.copyOf(((FDEBLayoutData) edge.getEdgeData().getLayoutData()).subdivisionPoints,
                        ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).subdivisionPoints.length);
            }

            for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
                if (cancel) {
                    return;
                }
                if (!useLowMemoryMode) {
                    FDEBUtilities.updateNewSubdivisionPoints(edge, springConstant, stepSize, useInverseQuadraticModel);
                } else {
                    FDEBUtilities.updateNewSubdivisionPointsInLowMemoryMode(edge, springConstant, stepSize, useInverseQuadraticModel,
                            graphModel.getGraph(), computator, compatibilityThreshold);
                }
            }

            for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
                ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).subdivisionPoints = ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).newSubdivisionPoints;
                ((FDEBLayoutData) edge.getEdgeData().getLayoutData()).newSubdivisionPoints = null;
            }

            if (cancel) {
                return;
            }
        }

        if (cycle == numCycles) {
            setConverged(true);
            endTime = System.currentTimeMillis();
            System.err.println(endTime - startTime + " execution time");
        } else {
            prepareForTheNextStep();
        }
    }

    void prepareForTheNextStep() {
        cycle++;
        stepSize *= (1.0 - stepDampingFactor);
        iterationsPerCycle = (iterationsPerCycle * iterationIncreaseRate);
        divideEdges();
    }

    void divideEdges() {
        subdivisionPointsPerEdge *= subdivisionPointIncreaseRate;
        for (Edge edge : graphModel.getGraph().getEdges().toArray()) {
            FDEBUtilities.divideEdge(edge, subdivisionPointsPerEdge);
        }
    }
}
