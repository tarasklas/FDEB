/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.builder;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.bundler.FDEBBundlerMultithreading;
import org.gephi.edgelayout.spi.EdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayoutBuilder;
import org.gephi.fdeb.FDEBBundlerParameters;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author megaterik
 */
@ServiceProvider(service = EdgeLayoutBuilder.class)
public class FDEBMultithreadingBuilder implements EdgeLayoutBuilder{

    private static class FDEBMultithreadingUI implements LayoutUI{
        
        @Override
        public String getDescription() {
            return "Force Directed Edge Bundling With Multithreading";
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public JPanel getSimplePanel(Layout layout) {
            return null;
        }

        @Override
        public int getQualityRank() {
            return 5;
        }

        @Override
        public int getSpeedRank() {
            return 3;
        }
    }

    private FDEBMultithreadingUI ui =  new FDEBMultithreadingUI();
    @Override
    public String getName() {
        return "2 Multithreaded FDEB";
    }

    @Override
    public LayoutUI getUI() {
        return ui;
    }

    @Override
    public EdgeLayout buildLayout() {
       return new FDEBBundlerMultithreading(this);
    }
    
}
