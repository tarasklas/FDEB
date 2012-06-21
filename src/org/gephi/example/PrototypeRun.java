/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.example;

import org.gephi.bundler.FDEBBundler;
import java.io.File;
import java.io.IOException;
import org.gephi.data.attributes.api.*;
import org.gephi.fdeb.FDEBBundlerParameters;
import org.gephi.renderer.FDEBRenderer;
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
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author megaterik
 */
public class PrototypeRun {
    
    public String filename = "example.gml";

    public static void main(String[] args) {
        new PrototypeRun().run();
    }

    public void run() {
        System.err.println("Example of simple bundling:");
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get models and controllers for this new workspace - will be useful later
        AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);

        //Import file       
        Container container;
        try {
            File file = new File(filename);
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
        for (Node node : graph.getNodes())
        {
            node.getNodeData().setSize(0.1f);
        }
        System.out.println("Nodes: " + graph.getNodeCount());
        System.out.println("Edges: " + graph.getEdgeCount());

        FDEBBundler layout = new FDEBBundler(null, new FDEBBundlerParameters());
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
            ec.exportFile(new File("result0.pdf"));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        while (!layout.isConverged()) {
            i++;
            layout.goAlgo();
            try {
                ec.exportFile(new File("result" + i + ".pdf"));
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
        }
        layout.endAlgo();
        System.err.println("Time spent " + (System.currentTimeMillis() - startMeasure) + " ms.");
        
        /* PrintWriter debug = null;
        try {
            debug = new PrintWriter("debugSimple.txt");
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        for (Edge edge : graph.getEdges())
        {
            FDEBLayoutData data = edge.getEdgeData().getLayoutData();
            for (int j = 0; j < data.subdivisionPoints.length; j++)
            {
                debug.println(data.subdivisionPoints[j].x + " " + data.subdivisionPoints[j].y);
            }
        }
        debug.close(); */
    }
};
