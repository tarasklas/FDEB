/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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

import org.gephi.edgelayout.spi.EdgeLayout;
import org.gephi.graph.api.GraphController;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = EdgeLayoutController.class)
public class EdgeLayoutControllerImpl implements EdgeLayoutController {

    private EdgeLayoutModelImpl model;
    private EdgeLayoutControllerImpl.EdgeLayoutRun layoutRun;

    public EdgeLayoutControllerImpl() {
        Lookup.getDefault().lookup(ProjectController.class).addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                workspace.add(new EdgeLayoutModelImpl());
            }

            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(EdgeLayoutModelImpl.class);
                if (model == null) {
                    model = new EdgeLayoutModelImpl();
                }
                workspace.add(model);
            }

            public void unselect(Workspace workspace) {
                if (model != null && model.getSelectedLayout() != null) {
                    model.saveProperties(model.getSelectedLayout());
                }
            }

            public void close(Workspace workspace) {
                EdgeLayoutModelImpl layoutModel = workspace.getLookup().lookup(EdgeLayoutModelImpl.class);
                if (layoutModel != null) {
                    layoutModel.getExecutor().cancel();
                }
            }

            public void disable() {
                model = null;
            }
        });

        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        if (projectController.getCurrentWorkspace() != null) {
            model = projectController.getCurrentWorkspace().getLookup().lookup(EdgeLayoutModelImpl.class);
            if (model == null) {
                model = new EdgeLayoutModelImpl();
            }
            projectController.getCurrentWorkspace().add(model);
        }
    }

    public EdgeLayoutModel getModel() {
        return model;
    }

    public void setLayout(EdgeLayout layout) {
        model.setSelectedLayout(layout);
        injectGraph();
    }

    public void executeLayout() {
        if (model.getSelectedLayout() != null) {
            layoutRun = new EdgeLayoutControllerImpl.EdgeLayoutRun(model.getSelectedLayout());
            model.getExecutor().execute(layoutRun, layoutRun);
            model.setRunning(true);
        }
    }

    public void executeLayout(int numIterations) {
        if (model.getSelectedLayout() != null) {
            layoutRun = new EdgeLayoutControllerImpl.EdgeLayoutRun(model.getSelectedLayout(), numIterations);
            model.getExecutor().execute(layoutRun, layoutRun);
            model.setRunning(true);
        }
    }

    public void injectGraph() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (model.getSelectedLayout() != null && graphController.getModel() != null) {
            model.getSelectedLayout().setGraphModel(graphController.getModel());
        }
    }

    public boolean canExecute() {
        return model.getSelectedLayout() != null && !model.isRunning();
    }

    public boolean canStop() {
        return model.isRunning();
    }

    public void stopLayout() {
        model.getExecutor().cancel();
    }
    private static class EdgeLayoutRun implements LongTask, Runnable {

        private final EdgeLayout layout;
        private boolean stopRun = false;
        private ProgressTicket progressTicket;
        private final Integer iterations;

        public EdgeLayoutRun(EdgeLayout layout) {
            this.layout = layout;
            this.iterations = null;
        }

        public EdgeLayoutRun(EdgeLayout layout, int numIterations) {
            this.layout = layout;
            this.iterations = numIterations;
        }

        public void run() {
            Progress.setDisplayName(progressTicket, layout.getBuilder().getName());
            Progress.start(progressTicket);
            layout.initAlgo();
            long i = 0;
            while (layout.canAlgo() && !stopRun) {
                layout.goAlgo();
                i++;
                if (iterations != null && iterations.longValue() == i) {
                    break;
                }
            }
            layout.endAlgo();
            if (i > 1) {
               // Progress.finish(progressTicket, NbBundle.getMessage(EdgeLayoutControllerImpl.class, "LayoutRun.end", layout.getBuilder().getName(), i));
                Progress.finish(progressTicket);
            } else {
                Progress.finish(progressTicket);
            }
        }

        public boolean cancel() {
            stopRun = true;
            return true;
        }

        public void setProgressTicket(ProgressTicket progressTicket) {
            this.progressTicket = progressTicket;
            if (layout instanceof LongTask) {
                ((LongTask) layout).setProgressTicket(progressTicket);
            }
        }
    }
}
