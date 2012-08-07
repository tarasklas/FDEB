/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.edgelayout.spi;

import org.gephi.graph.api.GraphModel;

/**
 * A Layout algorithm should implement the
 * <code>Layout</code> interface to allow the
 * <code>LayoutController</code> to run it properly. <p> See the
 * <code>LayoutBuilder</code> documentation to know how layout should be
 * instanciated. <p> To have fully integrated properties that can be changed in
 * real-time by users, properly define the various
 * <code>LayoutProperty</code> returned by the
 * {@link #getProperties()} method and provide getter and setter for each.
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 * @see LayoutBuilder
 */
public interface EdgeLayout{

    /**
     * initAlgo() is called to initialize the algorithm (prepare to run).
     */
    public void initAlgo();

    /**
     * Injects the graph model for the graph this Layout should operate on. <p>
     * It's preferable to get <b>visible</b> graph to perform on visualization.
     *
     * @param graphModel the graph model that the layout is to be working on
     */
    public void setGraphModel(GraphModel graphModel);

    /**
     * Run a step in the algorithm, should be called only if canAlgo() returns
     * true.
     */
    public void goAlgo();

    /**
     * Tests if the algorithm can run, called before each pass.
     *
     * @return
     * <code>true</code> if the algorithm can run,
     * <code>
     *                      false</code> otherwise
     */
    public boolean canAlgo();

    /**
     * Called when the algorithm is finished (canAlgo() returns false).
     */
    public void endAlgo();

    /**
     * The properties for this layout.
     *
     * @return the layout properties
     * @throws NoSuchMethodException
     */
    public EdgeLayoutProperty[] getProperties();

    /**
     * Resets the properties values to the default values.
     */
    public void resetPropertiesValues();

    /**
     * The reference to the LayoutBuilder that instanciated this Layout.
     *
     * @return the reference to the builder that builts this instance
     */
    public EdgeLayoutBuilder getBuilder();

    /*
     * Called when it's possible to change something without full recalculation
     */
    public void modifyAlgo();

    public void removeLayoutData();
    
    /*
     * Called after each goAlgo() or initAlgo() call, so could be used, for example, to refresh after every fifth iteration
     */
    public boolean shouldRefreshPreview(int refreshRate);
}
