/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.builder;

import org.gephi.bundler.FDEBBundlerBarnesHut;
import org.gephi.edgelayout.spi.EdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayoutBuilder;
import org.gephi.edgelayout.spi.EdgeLayoutUI;
import org.gephi.fdeb.FDEBBundlerParameters;

/**
 *
 * @author megaterik
 */
//@ServiceProvider(service = EdgeLayoutBuilder.class) 
public class FDEBBarnesHutBuilder implements EdgeLayoutBuilder {

    @Override
    public String getName() {
        return "3 Barnes-Hut FDEB";
    }

    @Override
    public EdgeLayoutUI getUI() {
        return null;
    }

    @Override
    public EdgeLayout buildLayout() {
        return new FDEBBundlerBarnesHut(this, new FDEBBundlerParameters());
    }
}
