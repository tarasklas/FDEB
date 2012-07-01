/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.fdeb.utils;

import java.awt.geom.Point2D.Float;
import org.gephi.graph.api.Edge;
import org.junit.*;
import static org.junit.Assert.*;
import processing.core.PVector;

/**
 *
 * @author megaterik
 */
public class FDEBCompatibilityComputatorTest {

    private static double eps = 0.001;

    public FDEBCompatibilityComputatorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of calculateCompatibility method, of class
     * FDEBCompatibilityComputator.
     */
    @Test
    public void testCalculateCompatibility() {/*
        System.out.println("calculateCompatibility");
        Edge aEdge = null;
        Edge bEdge = null;
        double expResult = 0.0;
        double result = FDEBCompatibilityComputator.calculateCompatibility(aEdge, bEdge);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype."); */
    }

    /**
     * Test of angleCompatibility method, of class FDEBCompatibilityComputator.
     */
    @Test
    public void testAngleCompatibility() {
        System.out.println("angleCompatibility");
        assertEquals(1.0, FDEBCompatibilityComputator.angleCompatibility(new PVector(1024, 1024), new PVector(-1024, -1024)), eps);
        assertEquals(0.0, FDEBCompatibilityComputator.angleCompatibility(new PVector(1024, 1024), new PVector(-1, 1)), eps);
        assertEquals(0.707106781, FDEBCompatibilityComputator.angleCompatibility(new PVector(1024, 0), new PVector(512, 512)), eps);
        assertTrue(FDEBCompatibilityComputator.angleCompatibility(new PVector(1, 0.25f), new PVector(1, 1)) 
                <  FDEBCompatibilityComputator.angleCompatibility(new PVector(1, 0.25f), new PVector(1, 0)));
    }

    /**
     * Test of scaleCompatibility method, of class FDEBCompatibilityComputator.
     */
    @Test
    public void testScaleCompatibility() {
        System.out.println("scaleCompatibility");
        assertEquals(0.0, FDEBCompatibilityComputator.scaleCompatibility(new PVector(12000, -30000), new PVector(0.1f, 0.5f)), eps);
        assertEquals(1.0, FDEBCompatibilityComputator.scaleCompatibility(new PVector(100, 100), new PVector(141, 12)), eps);
        assertTrue(FDEBCompatibilityComputator.scaleCompatibility(new PVector(125, 125), new PVector(50, 50))
                < FDEBCompatibilityComputator.scaleCompatibility(new PVector(125, 125), new PVector(51, 50)));
    }

    /**
     * Test of positionCompatibility method, of class
     * FDEBCompatibilityComputator.
     */
    @Test
    public void testPositionCompatibility() {
        /* System.out.println("positionCompatibility");
        PVector a = null;
        PVector b = null;
        Edge ae = null;
        Edge be = null;
        double expResult = 0.0;
        double result = FDEBCompatibilityComputator.positionCompatibility(a, b, ae, be);
        assertEquals(expResult, result, 0.0); */
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of visibilityCompatibility method, of class
     * FDEBCompatibilityComputator.
     */
    @Test
    public void testVisibilityCompatibility_Edge_Edge() {
       /* System.out.println("visibilityCompatibility");
        Edge aEdge = null;
        Edge bEdge = null;
        double expResult = 0.0;
        double result = FDEBCompatibilityComputator.visibilityCompatibility(aEdge, bEdge);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype."); */
    }

    /**
     * Test of visibilityCompatibility method, of class
     * FDEBCompatibilityComputator.
     */
    @Test
    public void testVisibilityCompatibility_4args() {
        /* System.out.println("visibilityCompatibility");
        Float as = null;
        Float af = null;
        Float bs = null;
        Float bf = null;
        double expResult = 0.0;
        double result = FDEBCompatibilityComputator.visibilityCompatibility(as, af, bs, bf);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype."); */
    }

    /**
     * Test of projectPointToLine method, of class FDEBCompatibilityComputator.
     */
    @Test
    public void testProjectPointToLine() {
        /* System.out.println("projectPointToLine");
        double x1 = 0.0;
        double y1 = 0.0;
        double x2 = 0.0;
        double y2 = 0.0;
        double x = 0.0;
        double y = 0.0;
        Float expResult = null;
        Float result = FDEBCompatibilityComputator.projectPointToLine(x1, y1, x2, y2, x, y);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype."); */
    }
}
