/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.edgelayout.api;

import java.awt.Color;
import java.beans.PropertyEditorSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.preview.types.DependantOriginalColor;

public class BasicColorPropertyEditor extends PropertyEditorSupport {

    @Override
    public String getAsText() {
        Color color = (Color) getValue();
        return String.format(
                "[%d,%d,%d]",
                color.getRed(),
                color.getGreen(),
                color.getBlue());
    }

    @Override
    public void setAsText(String str) {
        String[] s = str.split(",");
        int r = Integer.parseInt(s[0].substring(1));
        int g = Integer.parseInt(s[1]);
        int b = Integer.parseInt(s[2].substring(0, s[2].length() - 1));
        setValue(new ColorWrapper(r, g, b));
    }

    @Override
    public boolean supportsCustomEditor() {
        return false;
    }
}
