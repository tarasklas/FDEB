/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.builder;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.bundler.FDEBBundler;
import org.gephi.edgelayout.spi.EdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayoutBuilder;
import org.gephi.edgelayout.spi.EdgeLayoutUI;
import org.gephi.ui.FDEBAbstractUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author megaterik
 */
@ServiceProvider(service = EdgeLayoutBuilder.class)
public class FDEBBuilder implements EdgeLayoutBuilder {

    private FDEBUI ui = new FDEBUI();

    private static class FDEBUI implements EdgeLayoutUI {
        private FDEBAbstractUI gui = new FDEBAbstractUI();

        @Override
        public String getDescription() {
            return "Force Directed Edge Bundling";
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public JPanel getSimplePanel(EdgeLayout layout) {
            return null;
        }

        @Override
        public int getQualityRank() {
            return 5;
        }

        @Override
        public int getSpeedRank() {
            return 1;
        }
    }

    @Override
    public String getName() {
        return "Simple FDEB";
    }

    @Override
    public EdgeLayoutUI getUI() {
        return ui;
    }

    @Override
    public EdgeLayout buildLayout() {
        return new FDEBBundler(this);
    }
}
