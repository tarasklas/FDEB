/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.edgelayout.api;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyEditorSupport;
import javax.swing.JComboBox;
import org.gephi.preview.types.RendererModes;

/**
 *
 * @author Eduardo Ramos<eduramiba@gmail.com>
 */
public class RenderModePropertyEditor extends PropertyEditorSupport {

    @Override
    public String getAsText() {
        RendererModes value = (RendererModes) getValue();
        return value.toString();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        RendererModes value = (RendererModes.fromString(text));
        if (value != null) {
            setValue(value);
        }
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
        final JComboBox component = new JComboBox();
        component.removeAllItems();
        for (RendererModes mode : RendererModes.values()) {
            component.addItem(mode);
        }
        component.setSelectedItem(getValue());
        component.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setValue(component.getSelectedItem());
            }
        });
        return component;
    }
}
