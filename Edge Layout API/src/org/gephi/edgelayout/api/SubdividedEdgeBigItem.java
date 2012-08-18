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
package org.gephi.edgelayout.api;

import java.util.ArrayList;
import java.util.Collections;
import org.gephi.edgelayout.spi.EdgeLayoutData;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.plugin.items.AbstractItem;
import org.gephi.preview.types.RendererModes;
import org.openide.util.Lookup;

/**
 *
 * @author Taras Klaskovsky <megaterik@gmail.com>
 */
public class SubdividedEdgeBigItem extends AbstractItem implements Item {

    ArrayList<SortedEdgeWrapper> edges;

    SubdividedEdgeBigItem(Object source, String type) {
        super(source, type);
        edges = new ArrayList<SortedEdgeWrapper>();
        if (Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getValue(PreviewProperty.EDGE_LAYOUT_USE_RENDERER) == RendererModes.GRADIENT) {
            for (Edge edge : ((Graph) source).getEdges()) {
                if (edge.getEdgeData().getLayoutData() instanceof EdgeLayoutData) {
                    edges.add(new SortedEdgeWrapper(edge, ((EdgeLayoutData) edge.getEdgeData().getLayoutData()).getEdgeSortOrder()));
                }
            }
        } else {
            for (Edge edge : ((Graph) source).getEdges()) {
                if (edge.getEdgeData().getLayoutData() instanceof EdgeLayoutData) {
                    EdgeLayoutData data = edge.getEdgeData().getLayoutData();
                    double[] sort = data.getSubdivisionEdgeSortOrder();
                    if (sort != null) {
                        for (int i = 0; i < sort.length; i++) {
                            try {
                                edges.add(new SortedEdgeWrapper(edge, sort[i], i));
                            } catch (NullPointerException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        Collections.sort(edges);
    }

    public boolean isReady() {
        if (edges == null || edges.isEmpty()) {
            return false;
        }
        if ((Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getValue(PreviewProperty.EDGE_LAYOUT_USE_RENDERER)
                == RendererModes.GRADIENT)) {
            for (SortedEdgeWrapper wrapper : edges) {
                if (wrapper.edge.getEdgeData().getLayoutData() != null
                        && ((EdgeLayoutData) wrapper.edge.getEdgeData().getLayoutData()).getEdgeColor() == null) {
                    return false;
                }
            }
        }
        if ((Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getValue(PreviewProperty.EDGE_LAYOUT_USE_RENDERER)
                == RendererModes.GRADIENT_COMPLEX)) {
            for (SortedEdgeWrapper wrapper : edges) {
                if (wrapper.edge.getEdgeData().getLayoutData() != null
                        && ((EdgeLayoutData) wrapper.edge.getEdgeData().getLayoutData()).getSubdivisionEdgeColor() == null) {
                    return false;
                }
            }
        }
        return true;
    }
}

class SortedEdgeWrapper implements Comparable<SortedEdgeWrapper> {

    Edge edge;
    double sort;
    int id;

    public SortedEdgeWrapper(Edge edge, double sort) {
        this.edge = edge;
        this.sort = sort;
    }

    public SortedEdgeWrapper(Edge edge, double sort, int id) {
        this.edge = edge;
        this.sort = sort;
        this.id = id;
    }

    @Override
    public int compareTo(SortedEdgeWrapper o) {
        if (this.sort > o.sort) {
            return 1;
        } else if (this.sort < o.sort) {
            return -1;
        } else {
            return 0;
        }
    }
}