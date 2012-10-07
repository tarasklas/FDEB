/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Taras Klaskovsky <megaterik@gmail.com>
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.edgelayout.spi.EdgeLayout;
import org.gephi.graph.api.GraphController;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian, Taras Klaskovsky <megaterik@gmail.com>
 */
@ServiceProvider(service = EdgeLayoutController.class)
public class EdgeLayoutControllerImpl implements EdgeLayoutController {

    private EdgeLayoutModelImpl model;
    private EdgeLayoutControllerImpl.EdgeLayoutRun layoutRun;
    private ArrayList<ChangeListener> listeners;

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

        listeners = new ArrayList<ChangeListener>();

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
            layoutRun = new EdgeLayoutControllerImpl.EdgeLayoutRun(model.getSelectedLayout(), listeners);
            model.getExecutor().execute(layoutRun, layoutRun);
            model.setRunning(true);
        }
    }

    public void executeLayout(int numIterations) {
        if (model.getSelectedLayout() != null) {
            layoutRun = new EdgeLayoutControllerImpl.EdgeLayoutRun(model.getSelectedLayout(), numIterations, listeners);
            model.getExecutor().execute(layoutRun, layoutRun);
            model.setRunning(true);
        }
    }

    public void injectGraph() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (model.getSelectedLayout() != null && graphController.getModel() != null) {
            model.getSelectedLayout().setGraphModel(graphController.getModel());
            model.getSelectedLayout().resetPropertiesValues();
        }
    }

    public boolean canExecute() {
        return model.getSelectedLayout() != null && !model.isRunning();
    }

    public boolean canStop() {
        return model.isRunning();
    }

    public void stopLayout() {
        layoutRun.cancel();
    }

    public void killLayout() {
        layoutRun.kill();
    }

    @Override
    public void addRefreshListener(ChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public boolean removeRefreshListener(ChangeListener listener) {
        return listeners.remove(listener);
    }

    private static class EdgeLayoutRun implements LongTask, Runnable {

        private final EdgeLayout layout;
        private boolean cancel = false;
        private boolean kill = false;
        private ProgressTicket progressTicket;
        private final Integer iterations;
        private final ArrayList<ChangeListener> listeners;
        public static final String REFRESH = "refresh";
        public static final String END_ALGO = "end";
        public static final String STOP_ALGO = "stop";
        public static final String KILL_ALGO = "kill";

        public EdgeLayoutRun(EdgeLayout layout, ArrayList<ChangeListener> listeners) {
            this.layout = layout;
            this.iterations = null;
            this.listeners = listeners;
        }

        public EdgeLayoutRun(EdgeLayout layout, int numIterations, ArrayList<ChangeListener> listeners) {
            this.layout = layout;
            this.iterations = numIterations;
            this.listeners = listeners;
        }

        public void refreshPreview(String event) {
            System.err.println("refresh " + event + " " + System.currentTimeMillis() + " " + kill + " " + cancel + " " + iterations);
            System.err.flush();
            for (ChangeListener listener : listeners) {
                listener.stateChanged(new ChangeEvent(event));
            }
        }

        public void run() {
            cancel = kill = false;
            int refreshRate = Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().getIntValue(PreviewProperty.EDGE_LAYOUT_REFRESH_RATE);
            Progress.setDisplayName(progressTicket, layout.getBuilder().getName());
            Progress.start(progressTicket);
            layout.initAlgo();
            if (layout.shouldRefreshPreview(refreshRate)) {
                refreshPreview(REFRESH);
            }

            long i = 0;
            while (layout.canAlgo() && !cancel && !kill) {
                layout.goAlgo();
                i++;
                if (iterations != null && iterations.longValue() == i) {
                    break;
                }
                if (!cancel && !kill && layout.shouldRefreshPreview(refreshRate)) {
                    refreshPreview(REFRESH);
                }
                if (cancel) {
                    layout.cancel();
                    layout.endAlgo();
                    refreshPreview(STOP_ALGO);
                }
            }
            
            if (!cancel && !kill) //succesfull end
            {
                layout.endAlgo();
                refreshPreview(END_ALGO);
            }
            if (i > 1) {
                // Progress.finish(progressTicket, NbBundle.getMessage(EdgeLayoutControllerImpl.class, "LayoutRun.end", layout.getBuilder().getName(), i));
                Progress.finish(progressTicket);
            } else {
                Progress.finish(progressTicket);
            }
        }

        public boolean cancel() {
            if (kill) //if already killed ignore stop
            {
                return true;
            }
            cancel = true;
            //else layout will be stopped at run() function after its next layout.goAlgo() iteration so that no data will be lost
            return true;
        }

        public boolean kill() {
            kill = true;
            cancel = false;// layout will be stopped by the time it's checked
            layout.cancel();
            refreshPreview(KILL_ALGO);
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
