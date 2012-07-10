package org.gephi.edgelayout.spi;

/**
 * A
 * <code>LayoutBuilder</code> provides a specific {@link Layout} instance. The
 * Builder pattern is more suitable for the Layout instantiation to allow
 * simpler reusability of Layout's code. <p> Only the LayoutBuilder of a given
 * layout algorithm is exposed, this way, one can devise different layout
 * algorithms (represented by their respective LayoutBuilder) that uses a same
 * underlying Layout implementation, but that differs only by an aggregation,
 * composition or a property that is set only during instantiation time. <p> See
 * <code>ClockwiseRotate</code> and
 * <code>CounterClockwiseRotate</code> for a simple example of this pattern.
 * Both are LayoutBuilders that instanciate Layouts with a different behaviour
 * (the direction of rotation), but both uses the RotateLayout class. The only
 * difference is the angle provided by the LayoutBuilder on the time of
 * instantiation of the RotateLayout object.
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public interface EdgeLayoutBuilder {

    /**
     * The name of the behaviour of the Layout's provided by this Builder.
     *
     * @return the display neame of the layout algorithm
     */
    public String getName();

    /**
     * User interface attributes (name, description, icon...) for all Layouts
     * built by this builder.
     *
     * @return a
     * <code>LayoutUI</code> instance
     */
    public EdgeLayoutUI getUI();

    /**
     * Builds an instance of the Layout.
     *
     * @return a new
     * <code>Layout</code> instance
     */
    public EdgeLayout buildLayout();
}
