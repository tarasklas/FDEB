/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.example;

import org.gephi.renderer.FDEBRenderer;
import org.gephi.bundler.FDEBBundlerMultithreading;
import java.io.File;
import java.io.IOException;
import org.gephi.data.attributes.api.*;
import org.gephi.fdeb.*;
import org.gephi.filters.api.FilterController;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.*;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.preview.api.*;
import org.gephi.preview.plugin.renderers.*;
import org.gephi.preview.plugin.renderers.NodeRenderer;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.RankingController;
import org.gephi.renderer.FDEBSimpleBitmapExport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author megaterik
 */
public class FolderRun {

    public static void main(String[] args) {
        new FolderRun().run();
    }

    public void run() {
        System.err.println("Process all elements from input folder with multithreaded fdeb");

        //Get models and controllers for this new workspace - will be useful later

        File[] files = new File("input/").listFiles();
        for (File file : files) {
            System.err.println("Process " + file.getAbsolutePath());


            ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
            pc.newProject();
            Workspace workspace = pc.getCurrentWorkspace();
            AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
            GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
            PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
            ImportController importController = Lookup.getDefault().lookup(ImportController.class);
            FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
            RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);

            Container container;
            try {
                container = importController.importFile(file);
                container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED);   //Force DIRECTED
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            //Append imported data to GraphAPI
            importController.process(container, new DefaultProcessor(), workspace);

            //See if graph is well imported
            DirectedGraph graph = graphModel.getDirectedGraph();
            for (Node node : graph.getNodes()) {
                node.getNodeData().setSize(0.1f);
            }
            System.out.println("Nodes: " + graph.getNodeCount());
            System.out.println("Edges: " + graph.getEdgeCount());
            if (graph.getEdgeCount() > 2500) {
                System.out.println("Graph is too large, skip");
                continue;
            }

            FDEBBundlerMultithreading layout = new FDEBBundlerMultithreading(null, new FDEBBundlerParameters());
            layout.setGraphModel(graphModel);
            layout.initAlgo();
            layout.resetPropertiesValues();
            // layout.endAlgo();

            PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
            PreviewModel previewModel = previewController.getModel();
            ManagedRenderer[] managedRenderers = {new ManagedRenderer(new FDEBRenderer(), true),
                new ManagedRenderer(new NodeRenderer(), true),
                new ManagedRenderer(new EdgeRenderer(), false)};

            previewModel.setManagedRenderers(managedRenderers);

            int i = 0;
            long startMeasure = System.currentTimeMillis();
            ExportController ec = Lookup.getDefault().lookup(ExportController.class);
            try {
                ec.exportFile(new File("output/" + file.getName() + "result0.pdf"));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            while (!layout.isConverged()) {
                i++;
                layout.goAlgo();
                try {
                    ec.exportFile(new File("output/" + file.getName() + "result" + i + ".pdf"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return;
                }
            }
            layout.endAlgo();
            try {
                new FDEBSimpleBitmapExport().export(graphModel.getGraph(), "output/" + file.getName() + "exported" + i + ".pdf");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            System.err.println("Time spent " + (System.currentTimeMillis() - startMeasure) + " ms.");
        }

    }
};
