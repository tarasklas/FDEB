/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.fdeb.utils;

import java.util.Arrays;
import java.util.Random;
import org.gephi.fdeb.FDEBLayoutData;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.*;
import org.gephi.graph.dhns.edge.ProperEdgeImpl;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.project.api.*;
import org.gephi.project.api.Workspace;
import org.junit.*;
import static org.junit.Assert.*;
import org.openide.util.Lookup;

/**
 *
 * @author megaterik
 */
public class FDEBUtilitiesTest {
    
    
    Graph graph;
    GraphFactory factory;
    private static double eps = 0.001;
    
    public FDEBUtilitiesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        graph = graphModel.getGraph();
        factory = graphModel.factory();
        graph.clear();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of calculateSprintConstant method, of class FDEBUtilities.
     */
    @Test
    public void testCalculateSprintConstant() {/*
        System.out.println("calculateSprintConstant");
        Graph graph = null;
        double expResult = 0.0;
        double result = FDEBUtilities.calculateSprintConstant(graph);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype."); */
    }

    /**
     * Test of divideEdge method, of class FDEBUtilities.
     */
    @Test
    public void testDivideEdge() {
        Node n0 = factory.newNode();
        Node n1 = factory.newNode();
        Edge e0 = factory.newEdge(n0, n1);
        graph.addNode(n0);
        graph.addNode(n1);
        graph.addEdge(e0);
        FDEBLayoutData data = new FDEBLayoutData(n0.getNodeData().x(), n0.getNodeData().y(), n1.getNodeData().x(), n1.getNodeData().y());
        e0.getEdgeData().setLayoutData(data);
        FDEBUtilities.divideEdge(e0, 2);
        
        float x0 = n0.getNodeData().x();
        float y0 = n0.getNodeData().y();
        float x1 = n1.getNodeData().x();
        float y1 = n1.getNodeData().y();
        assertEquals(data.subdivisionPoints.length, 4);
        
        assertEquals(data.subdivisionPoints[0].x, x0, eps);
        assertEquals(data.subdivisionPoints[0].y, y0, eps);
        
        assertEquals(data.subdivisionPoints[1].x, x0 + (x1 - x0) / 3, eps);
        assertEquals(data.subdivisionPoints[1].y, y0 + (y1 - y0) / 3, eps);
        
        assertEquals(data.subdivisionPoints[2].x, x0 + (x1 - x0) * 2 / 3, eps);
        assertEquals(data.subdivisionPoints[2].y, y0 + (y1 - y0) * 2 / 3, eps);
        
        assertEquals(data.subdivisionPoints[3].x, x1, eps);
        assertEquals(data.subdivisionPoints[3].y, y1, eps);
        
        
        
        Random random = new Random();
        n0.getNodeData().setX(0);
        n0.getNodeData().setY(0);
        n1.getNodeData().setX(1000);
        n1.getNodeData().setY(1000);
        data = new FDEBLayoutData(n0.getNodeData().x(), n0.getNodeData().y(), n1.getNodeData().x(), n1.getNodeData().y());
        e0.getEdgeData().setLayoutData(data);
        data.subdivisionPoints[1].setLocation(0, 1000);
        FDEBUtilities.divideEdge(e0, 2);
        assertEquals(data.subdivisionPoints[1].x, 0, eps);
        assertEquals(data.subdivisionPoints[1].y, 2000f/3, eps);
        
        assertEquals(data.subdivisionPoints[2].x, 1000 - 2000f/3, eps);
        assertEquals(data.subdivisionPoints[2].y, 1000, eps);
        
        data = new FDEBLayoutData(n0.getNodeData().x(), n0.getNodeData().y(), n1.getNodeData().x(), n1.getNodeData().y());
        e0.getEdgeData().setLayoutData(data);
        data.subdivisionPoints[1].setLocation(0, 1000);
        
    }

    /**
     * Test of createCompatibilityRecords method, of class FDEBUtilities.
     */
    @Test
    public void testCreateCompatibilityRecords() {
        /*
        System.out.println("createCompatibilityRecords");
        Edge edge = null;
        double compatibilityThreshold = 0.0;
        Graph graph = null;
        FDEBUtilities.createCompatibilityRecords(edge, compatibilityThreshold, graph);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype."); */
    }

    /**
     * Test of updateNewSubdivisionPoints method, of class FDEBUtilities.
     */
    @Test
    public void testUpdateNewSubdivisionPoints() {
        /*
        System.out.println("updateNewSubdivisionPoints");
        Edge edge = null;
        double sprintConstant = 0.0;
        double stepSize = 0.0;
        FDEBUtilities.updateNewSubdivisionPoints(edge, sprintConstant, stepSize);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype."); */
    }
}
