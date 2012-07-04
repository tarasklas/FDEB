package org.gephi.edgelayout.spi;

import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;

/*
 * Class, used to provide different from node layouts @ServiceProvider
 * 
 * Sadly, that doesn't seem to work. I am thinking about the best way to fix this.
 * (In worst case, I can just make wrapper for LayoutBuilder, which would create builder,
 * which would create bundler, but I looking for something nicer)
 */
public interface EdgeLayoutBuilder extends LayoutBuilder{
}
