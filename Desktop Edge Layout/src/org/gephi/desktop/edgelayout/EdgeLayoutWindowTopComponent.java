/*
 * Api package isn't the best place to host gui, probably.
 */
package org.gephi.desktop.edgelayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.desktop.edgelayout.GradientPresetPersistence.GradientPreset;
import org.gephi.desktop.edgelayout.LayoutPresetPersistence.Preset;
import org.gephi.desktop.preview.api.PreviewUIController;
import org.gephi.edgelayout.api.EdgeLayoutController;
import org.gephi.edgelayout.api.EdgeLayoutModel;
import org.gephi.edgelayout.api.SubdividedEdgeRenderer;
import org.gephi.edgelayout.spi.EdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayoutBuilder;
import org.gephi.edgelayout.spi.EdgeLayoutProperty;
import org.gephi.preview.api.ManagedRenderer;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.plugin.renderers.ArrowRenderer;
import org.gephi.preview.plugin.renderers.EdgeRenderer;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.ui.components.gradientslider.GradientSlider;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.gephi.desktop.edgelayout//EdgeLayoutWindow//EN",
autostore = false)
@TopComponent.Description(preferredID = "EdgeLayoutWindowTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "rankingmode", openAtStartup = true, roles = {"preview"})
@ActionID(category = "Window", id = "org.gephi.desktop.edgelayout.EdgeLayoutWindowTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_EdgeLayoutWindowAction",
preferredID = "EdgeLayoutWindowTopComponent")
@Messages({
    "CTL_EdgeLayoutWindowAction=EdgeLayoutWindow",
    "CTL_EdgeLayoutWindowTopComponent=EdgeLayoutWindow Window",
    "HINT_EdgeLayoutWindowTopComponent=This is a EdgeLayoutWindow window"
})
public final class EdgeLayoutWindowTopComponent extends TopComponent implements PropertyChangeListener {

    private EdgeLayoutController controller;
    private EdgeLayoutModel model;
    private LayoutPresetPersistence layoutPresetPersistence;
    private GradientSlider gradientSlider;
    private GradientPresetPersistence gradientPresetPersistence;

    private void refreshPreview() {
        System.err.println("refresh preview " + System.currentTimeMillis());
        if (controller.getModel() != null && controller.getModel().getSelectedLayout() != null && !controller.getModel().isRunning()) {
            controller.getModel().getSelectedLayout().modifyAlgo();
        }
        Lookup.getDefault().lookup(PreviewUIController.class).refreshPreview();
    }

    public EdgeLayoutWindowTopComponent() {
        initComponents();
        setName(Bundle.CTL_EdgeLayoutWindowTopComponent());
        setToolTipText(Bundle.HINT_EdgeLayoutWindowTopComponent());
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.TRUE);
        controller = Lookup.getDefault().lookup(EdgeLayoutController.class);
        Lookup.getDefault().lookup(ProjectController.class).addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
            }

            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(EdgeLayoutModel.class);
                if (model.getSelectedBuilder() != null) {
                    for (int i = 0; i < layoutComboBox.getItemCount(); i++) {
                        if (layoutComboBox.getItemAt(i) instanceof EdgeLayoutWrapper
                                && ((EdgeLayoutWrapper) layoutComboBox.getItemAt(i)).getBuilder().equals(model.getSelectedBuilder())) {
                            layoutComboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                } else {
                    layoutComboBox.setSelectedIndex(0);
                }
            }

            public void unselect(Workspace workspace) {
            }

            public void close(Workspace workspace) {
                layoutComboBox.setSelectedIndex(0);
            }

            public void disable() {
                model = null;
            }
        });
        Lookup.getDefault().lookup(EdgeLayoutController.class).addRefreshListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                System.err.println(e.toString() + " event");
                if (e.getSource().equals("end") || e.getSource().equals("stop")) {
                    regenerateRunButtonWhenLayoutStops();
                }
                refreshPreview();
            }
        });
        regenerateRunButton(true);
        regenerateSettings();
        regenerateCombobox();

        gradientSliderPanel.setLayout(new BorderLayout());
        gradientSlider = new GradientSlider(GradientSlider.HORIZONTAL, new float[]{0f, 0.2f, 0.4f, 0.6f, 1f},
                new Color[]{Color.WHITE, Color.BLUE, Color.PINK, Color.RED, Color.YELLOW});
        gradientSliderPanel.add(gradientSlider, BorderLayout.CENTER);

        layoutPresetPersistence = new LayoutPresetPersistence();
        gradientPresetPersistence = new GradientPresetPersistence();

        gradientPresetPersistence.savePreset("White-Blue", new GradientSlider(GradientSlider.HORIZONTAL, new float[]{0f, 1f}, new Color[]{Color.WHITE, Color.BLUE}));
        gradientPresetPersistence.savePreset("White-Blue-Pink-Red-Yellow", new GradientSlider(GradientSlider.HORIZONTAL, new float[]{0f, 0.2f, 0.4f, 0.6f, 1f},
                new Color[]{Color.WHITE, Color.BLUE, Color.PINK, Color.RED, Color.YELLOW}));
    }

    private void regenerateSettings() {
        if (controller.getModel() != null && controller.getModel().getSelectedLayout() != null) {
            JPanel layoutPanel = null;
            try {
                layoutPanel = controller.getModel().getSelectedBuilder().getUI().getSimplePanel(controller.getModel().getSelectedLayout());
            } catch (Exception ex) {
                // No simple panel, switch to PropertySheet instead
            }
            if (layoutPanel != null) {
                customSettingsPanel.removeAll();
                customSettingsPanel.add(layoutPanel);
                settingsPanel.setVisible(false);
                customSettingsPanel.setVisible(true);
                customSettingsPanel.setEnabled(true);
            } else {
                LayoutNode layoutNode;
                layoutNode = new LayoutNode(controller.getModel().getSelectedLayout());
                layoutNode.getPropertySets();
                ((PropertySheet) settingsPanel).setNodes(new Node[]{layoutNode});
                customSettingsPanel.setVisible(false);
                settingsPanel.setVisible(true);
            }
        } else {
            ((PropertySheet) settingsPanel).setNodes(new Node[0]);
            settingsPanel.setVisible(true);
            customSettingsPanel.setVisible(false);
        }
    }

    private void regenerateCombobox() {
        ArrayList<EdgeLayoutBuilder> list = new ArrayList(Lookup.getDefault().lookupAll(EdgeLayoutBuilder.class));

        ArrayList sortedlist = new ArrayList(list.size());
        for (int i = 0; i < list.size(); i++) {
            sortedlist.add(new EdgeLayoutWrapper(list.get(i)));
        }
        Collections.sort(sortedlist);
        sortedlist.add(0, "--Choose a layout");
        DefaultComboBoxModel model = new DefaultComboBoxModel(sortedlist.toArray());
        layoutComboBox.setModel(model);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        customSettingsPanel = new javax.swing.JPanel();
        runButton = new javax.swing.JButton();
        gradientSliderPanel = new javax.swing.JPanel();
        layoutComboBox = new javax.swing.JComboBox();
        refreshButton = new javax.swing.JButton();
        layoutToolbar = new javax.swing.JToolBar();
        presetsButton = new javax.swing.JButton();
        gradientsButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        settingsPanel = new PropertySheet();

        customSettingsPanel.setLayout(new java.awt.CardLayout());

        org.openide.awt.Mnemonics.setLocalizedText(runButton, org.openide.util.NbBundle.getMessage(EdgeLayoutWindowTopComponent.class, "EdgeLayoutWindowTopComponent.runButton.text")); // NOI18N
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout gradientSliderPanelLayout = new javax.swing.GroupLayout(gradientSliderPanel);
        gradientSliderPanel.setLayout(gradientSliderPanelLayout);
        gradientSliderPanelLayout.setHorizontalGroup(
            gradientSliderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        gradientSliderPanelLayout.setVerticalGroup(
            gradientSliderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 84, Short.MAX_VALUE)
        );

        layoutComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        layoutComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                layoutComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(EdgeLayoutWindowTopComponent.class, "EdgeLayoutWindowTopComponent.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        layoutToolbar.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(presetsButton, org.openide.util.NbBundle.getMessage(EdgeLayoutWindowTopComponent.class, "EdgeLayoutWindowTopComponent.presetsButton.text")); // NOI18N
        presetsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                presetsButtonActionPerformed(evt);
            }
        });
        layoutToolbar.add(presetsButton);

        org.openide.awt.Mnemonics.setLocalizedText(gradientsButton, org.openide.util.NbBundle.getMessage(EdgeLayoutWindowTopComponent.class, "EdgeLayoutWindowTopComponent.gradientsButton.text")); // NOI18N
        gradientsButton.setFocusable(false);
        gradientsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        gradientsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        gradientsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gradientsButtonActionPerformed(evt);
            }
        });
        layoutToolbar.add(gradientsButton);

        org.openide.awt.Mnemonics.setLocalizedText(deleteButton, org.openide.util.NbBundle.getMessage(EdgeLayoutWindowTopComponent.class, "EdgeLayoutWindowTopComponent.deleteButton.text")); // NOI18N
        deleteButton.setToolTipText(org.openide.util.NbBundle.getMessage(EdgeLayoutWindowTopComponent.class, "EdgeLayoutWindowTopComponent.deleteButton.toolTipText")); // NOI18N
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(refreshButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(runButton, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(gradientSliderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(layoutToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 625, Short.MAX_VALUE)
                        .addComponent(deleteButton))
                    .addComponent(layoutComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(layoutComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(refreshButton)
                    .addComponent(runButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(gradientSliderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(layoutToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void switchToSubdividedRenderer() {
        ManagedRenderer[] renderers = Lookup.getDefault().lookup(PreviewController.class).getModel().getManagedRenderers();
        for (int i = 0; i < renderers.length; i++) {
            if (renderers[i].getRenderer() instanceof EdgeRenderer || renderers[i].getRenderer() instanceof ArrowRenderer) {
                renderers[i].setEnabled(false);
            }
        }

        for (int i = 0; i < renderers.length; i++) {
            if (renderers[i].getRenderer() instanceof SubdividedEdgeRenderer) {
                renderers[i].setEnabled(true);
            }
        }
    }

    private void switchToEdgeRenderer() {
        ManagedRenderer[] renderers = Lookup.getDefault().lookup(PreviewController.class).getModel().getManagedRenderers();
        for (int i = 0; i < renderers.length; i++) {
            if (renderers[i].getRenderer() instanceof EdgeRenderer || renderers[i].getRenderer() instanceof ArrowRenderer) {
                renderers[i].setEnabled(true);
            } else if (renderers[i].getRenderer() instanceof SubdividedEdgeRenderer) {
                renderers[i].setEnabled(false);
            }
        }
    }

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        EdgeLayout layout = (EdgeLayout) controller.getModel().getSelectedLayout();
        layout.removeLayoutData();
        switchToEdgeRenderer();
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void run() {
        if (controller.canExecute()) {
            switchToSubdividedRenderer();
            controller.executeLayout();
        }
    }

    private void stop() {
        if (controller.canStop()) {
            controller.stopLayout();
        }
    }

    private boolean layoutIsRunning() {
        return (controller.getModel() != null && controller.getModel().isRunning());
    }

    private void regenerateRunButtonWhenLayoutStops() {
        if (layoutIsRunning()) {
            System.err.println("still running!");
        }
        runButton.setText("Run");
        layoutComboBox.setEnabled(true);
    }

    private void regenerateRunButton(boolean shouldRefresh) {
        boolean refresh = false;
        if (layoutIsRunning() != !layoutComboBox.isEnabled() && shouldRefresh) //i.e., it just changed and we should update preview
        {
            refresh = true;
        }
        if (layoutIsRunning()) {
            runButton.setText("Stop");
            layoutComboBox.setEnabled(false);
        } else {
            runButton.setText("Run");
            layoutComboBox.setEnabled(true);
        }
        if (refresh) {
            refreshPreview();
        }
    }

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        Lookup.getDefault().lookup(PreviewController.class).getModel().getProperties().putValue(PreviewProperty.EDGE_LAYOUT_GRADIENT_SLIDER_LOCATION, gradientSlider);
        if (layoutIsRunning()) {
            stop();
        } else {
            run();
        }
        regenerateRunButton(true);
    }//GEN-LAST:event_runButtonActionPerformed

    private void layoutComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_layoutComboBoxItemStateChanged
        if (layoutComboBox.getSelectedItem() instanceof EdgeLayoutWrapper) {
            EdgeLayoutWrapper wrapper = (EdgeLayoutWrapper) layoutComboBox.getSelectedItem();
            controller.setLayout(wrapper.getBuilder().buildLayout());
            runButton.setEnabled(true);
        } else {
            runButton.setEnabled(false);
        }
        regenerateSettings();
    }//GEN-LAST:event_layoutComboBoxItemStateChanged

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        refreshPreview();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void presetsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_presetsButtonActionPerformed
        JPopupMenu menu = new JPopupMenu();
        List<Preset> presets = null;
        if (model == null) {
            model = controller.getModel();
        }
        if (model.getSelectedLayout() != null) {
            presets = layoutPresetPersistence.getPresets(model.getSelectedLayout());
        }
        if (presets != null && !presets.isEmpty()) {
            for (final Preset p : presets) {
                JMenuItem item = new JMenuItem(p.toString());
                item.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        layoutPresetPersistence.loadPreset(p, model.getSelectedLayout());
                        repaint();
                        //StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.status.loadPreset", model.getSelectedBuilder().getName(), p.toString()));
                    }
                });
                menu.add(item);
            }
        } else {
            //menu.add("<html><i>" + NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.presetsButton.nopreset") + "</i></html>");
            menu.add("<html><i>No presets yet </i></html>");
        }

        //JMenuItem saveItem = new JMenuItem(NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.presetsButton.savePreset"));
        JMenuItem saveItem = new JMenuItem("Save?");
        saveItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String lastPresetName = NbPreferences.forModule(EdgeLayoutWindowTopComponent.class).get("EdgeLayoutWindowPanel.lastPresetName", "");
                // NotifyDescriptor.InputLine question = new NotifyDescriptor.InputLine(
                //        NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.presetsButton.savePreset.input"),
                //        NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.presetsButton.savePreset.input.name"));
                NotifyDescriptor.InputLine question = new NotifyDescriptor.InputLine("text", "title");
                question.setInputText(lastPresetName);
                if (DialogDisplayer.getDefault().notify(question) == NotifyDescriptor.OK_OPTION) {
                    String input = question.getInputText();
                    if (input != null && !input.isEmpty()) {
                        layoutPresetPersistence.savePreset(input, model.getSelectedLayout());
                        //StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.status.savePreset", model.getSelectedBuilder().getName(), input));
                        NbPreferences.forModule(EdgeLayoutWindowTopComponent.class).put("EdgeLayoutWindowPanel.lastPresetName", input);
                    }
                }
            }
        });
        menu.add(new JSeparator());
        menu.add(saveItem);
        menu.show(layoutToolbar, 0, -menu.getPreferredSize().height);
    }//GEN-LAST:event_presetsButtonActionPerformed

    private void gradientsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gradientsButtonActionPerformed
        JPopupMenu menu = new JPopupMenu();
        ArrayList<GradientPreset> presets = null;
        if (model == null) {
            model = controller.getModel();
        }
        if (model.getSelectedLayout() != null) {
            presets = gradientPresetPersistence.getPresets();
        }
        if (presets != null && !presets.isEmpty()) {
            for (final GradientPreset p : presets) {
                JMenuItem item = new JMenuItem(p.toString());
                item.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        gradientPresetPersistence.loadPreset(p, gradientSlider);
                        //regenerateSettings();
                        //StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.status.loadPreset", model.getSelectedBuilder().getName(), p.toString()));
                    }
                });
                menu.add(item);
            }
        } else {
            //menu.add("<html><i>" + NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.presetsButton.nopreset") + "</i></html>");
            menu.add("<html><i>No presets yet :(</i></html>");
        }

        //JMenuItem saveItem = new JMenuItem(NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.presetsButton.savePreset"));
        JMenuItem saveItem = new JMenuItem("Save?");
        saveItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String lastPresetName = NbPreferences.forModule(EdgeLayoutWindowTopComponent.class).get("EdgeLayoutWindowTopComponent.lastGradientPesetName", "");
                // NotifyDescriptor.InputLine question = new NotifyDescriptor.InputLine(
                //        NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.presetsButton.savePreset.input"),
                //        NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.presetsButton.savePreset.input.name"));
                // question.setInputText(lastPresetName);
                NotifyDescriptor.InputLine question = new NotifyDescriptor.InputLine("text", "title");
                question.setInputText(lastPresetName);
                if (DialogDisplayer.getDefault().notify(question) == NotifyDescriptor.OK_OPTION) {
                    String input = question.getInputText();
                    if (input != null && !input.isEmpty()) {
                        gradientPresetPersistence.savePreset(input, gradientSlider);
                        //StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.status.savePreset", model.getSelectedBuilder().getName(), input));
                        NbPreferences.forModule(EdgeLayoutWindowTopComponent.class).put("EdgeLayoutWindowTopComponent.lastGradientPesetName", input);
                    }
                }
            }
        });
        menu.add(new JSeparator());
        menu.add(saveItem);
        menu.show(layoutToolbar, 0, -menu.getPreferredSize().height);
    }//GEN-LAST:event_gradientsButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel customSettingsPanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JPanel gradientSliderPanel;
    private javax.swing.JButton gradientsButton;
    private javax.swing.JComboBox layoutComboBox;
    private javax.swing.JToolBar layoutToolbar;
    private javax.swing.JButton presetsButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton runButton;
    private javax.swing.JPanel settingsPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        //regenerateRunButton(true);
        //regenerateSettings();
    }

    private class EdgeLayoutWrapper implements Comparable<EdgeLayoutWrapper> {

        EdgeLayoutBuilder builder;

        public EdgeLayoutWrapper(EdgeLayoutBuilder builder) {
            this.builder = builder;
        }

        public EdgeLayoutBuilder getBuilder() {
            return builder;
        }

        @Override
        public String toString() {
            return builder.getName().toString();
        }

        @Override
        public int compareTo(EdgeLayoutWrapper o) {
            return builder.getName().compareTo(o.getBuilder().getName());
        }
    }

    /**
     *
     * @author Mathieu Bastian
     */
    private class LayoutNode extends AbstractNode {

        private EdgeLayout layout;
        private PropertySet[] propertySets;

        public LayoutNode(EdgeLayout layout) {
            super(Children.LEAF);
            this.layout = layout;
            setName(layout.getBuilder().getName());
        }

        @Override
        public PropertySet[] getPropertySets() {
            if (propertySets == null) {
                try {
                    Map<String, Sheet.Set> sheetMap = new HashMap<String, Sheet.Set>();
                    System.err.println("layout has " + layout.getProperties().length + " properties");
                    for (EdgeLayoutProperty layoutProperty : layout.getProperties()) {
                        Sheet.Set set = sheetMap.get(layoutProperty.getCategory());
                        if (set == null) {
                            set = Sheet.createPropertiesSet();
                            set.setDisplayName(layoutProperty.getCategory());
                            sheetMap.put(layoutProperty.getCategory(), set);
                        }
                        set.put(layoutProperty.getProperty());
                    }
                    propertySets = sheetMap.values().toArray(new PropertySet[0]);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            }
            return propertySets;
        }

        public EdgeLayout getLayout() {
            return layout;
        }
    }
}
