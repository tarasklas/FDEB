/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.fdeb;

import com.itextpdf.text.PageSize;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PDFExporter;
import org.gephi.io.generator.plugin.RandomGraph;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.preview.api.ManagedRenderer;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.plugin.renderers.EdgeRenderer;
import org.gephi.preview.plugin.renderers.NodeRenderer;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 *
 * @author megaterik
 */
public class PrototypeRun {

    public static void main(String[] args) {
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Generate a new random graph into a container
        Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();

        RandomGraph randomGraph = new RandomGraph();
        randomGraph.setNumberOfNodes(50);
        randomGraph.setWiringProbability(0.005);
        randomGraph.generate(container.getLoader());

        //Append container to graph structure
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        importController.process(container, new DefaultProcessor(), workspace);

        //See if graph is well imported
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        DirectedGraph graph = graphModel.getDirectedGraph();
        System.out.println("Nodes: " + graph.getNodeCount());
        System.out.println("Edges: " + graph.getEdgeCount());

        FDEBBundler layout = new FDEBBundler(null, new FDEBBundlerParameters());
        layout.setGraphModel(graphModel);
        layout.initAlgo();
        layout.resetPropertiesValues();
        while (!layout.isConverged()) {
            layout.goAlgo();
        }
        layout.endAlgo();

        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModel previewModel = previewController.getModel();
        ManagedRenderer[] managedRenderers = { new ManagedRenderer(new FDEBRenderer(), true),
            new ManagedRenderer(new NodeRenderer(), true),
            new ManagedRenderer(new EdgeRenderer(), true)};

        previewModel.setManagedRenderers(managedRenderers);
        

        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("simple.pdf"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        //PDF Exporter config and export to Byte array
        PDFExporter pdfExporter = (PDFExporter) ec.getExporter("pdf");
        pdfExporter.setPageSize(PageSize.A0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ec.exportStream(baos, pdfExporter);
        // byte[] pdf = baos.toByteArray(); 

    }
};
