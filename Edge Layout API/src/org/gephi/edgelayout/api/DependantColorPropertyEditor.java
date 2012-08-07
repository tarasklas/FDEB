/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.edgelayout.api;

import com.bric.swing.ColorPicker;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JColorChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.edgelayout.api.BasicColorPropertyEditor;

/**
 *
 * @author megaterik
 */
public class DependantColorPropertyEditor extends BasicColorPropertyEditor {
//JColorButton

    ColorPicker colorPicker;
    @Override
    public Component getCustomEditor() {
        colorPicker =  new ColorPicker();
        colorPicker.setColor((Color)getValue());
        colorPicker.getColorPanel().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                setValue(colorPicker.getColor());
            }
        });
        return colorPicker;
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }
}
