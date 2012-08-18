/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Taras Klaskovsky <megaterik@gmail.com>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.desktop.edgelayout;

import java.awt.Color;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.gephi.edgelayout.spi.EdgeLayout;
import org.gephi.edgelayout.spi.EdgeLayoutProperty;
import org.gephi.ui.components.gradientslider.GradientSlider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GradientPresetPersistence {

    private ArrayList<GradientPresetPersistence.GradientPreset> presets = new ArrayList<GradientPresetPersistence.GradientPreset>();

    public GradientPresetPersistence() {
        loadPresets();
    }

    public void savePreset(String name, GradientSlider gradientSlider) {
        GradientPresetPersistence.GradientPreset preset = addPreset(new GradientPresetPersistence.GradientPreset(name, gradientSlider));

        try {
            //Create file if dont exist
            FileObject folder = FileUtil.getConfigFile("GradientPresets");
            if (folder == null) {
                folder = FileUtil.getConfigRoot().createFolder("GradientPresets");
            }
            FileObject presetFile = folder.getFileObject(name, "txt");
            if (presetFile == null) {
                presetFile = folder.createData(name, "txt");
            }
            PrintWriter printWriter = new PrintWriter(presetFile.getOutputStream());

            //Write doc
            preset.write(printWriter);
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadPreset(GradientPresetPersistence.GradientPreset preset, GradientSlider gradientSlider) {
        gradientSlider.setValues(preset.thumbPositions, preset.values);
    }

    public ArrayList<GradientPresetPersistence.GradientPreset> getPresets() {
        return presets;
    }

    private void loadPresets() {
        FileObject folder = FileUtil.getConfigFile("GradientPresets");
        if (folder != null) {
            for (FileObject child : folder.getChildren()) {
                if (child.isValid() && child.hasExt("txt")) {
                    try {
                        InputStream stream = child.getInputStream();
                        Scanner scanner = new Scanner(stream);
                        System.err.println("scannerhasnext " + scanner.hasNext());
                        if (scanner.hasNext()) {
                            GradientPresetPersistence.GradientPreset preset = new GradientPresetPersistence.GradientPreset(scanner);
                            addPreset(preset);
                        }
                        scanner.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private GradientPresetPersistence.GradientPreset addPreset(GradientPresetPersistence.GradientPreset preset) {
        List<GradientPresetPersistence.GradientPreset> GradientPresets = presets;
        if (GradientPresets == null) {
            GradientPresets = new ArrayList<GradientPresetPersistence.GradientPreset>();
        }
        for (GradientPresetPersistence.GradientPreset p : GradientPresets) {
            if (p.equals(preset)) {
                return p;
            }
        }
        GradientPresets.add(preset);
        return preset;
    }

    protected static class GradientPreset {

        protected Color[] values;
        protected float[] thumbPositions;
        private String name;

        private GradientPreset(String name, GradientSlider gradientSlider) {
            this.name = name;
            Object[] obj = gradientSlider.getValues();
            values = new Color[obj.length];
            for (int i = 0; i < obj.length; i++) {
                values[i] = (Color) obj[i];
            }
            thumbPositions = gradientSlider.getThumbPositions();
        }

        private GradientPreset(Scanner scanner) {
            read(scanner);
        }

        public void read(Scanner scanner) {
            int n = scanner.nextInt();
            values = new Color[n];
            thumbPositions = new float[n];
            for (int i = 0; i < n; i++) {
                thumbPositions[i] = scanner.nextFloat();
                values[i] = new Color(scanner.nextInt(), scanner.nextInt(), scanner.nextInt(), scanner.nextInt());
            }
        }

        public void write(PrintWriter printWriter) {
            printWriter.println(thumbPositions.length);
            for (int i = 0; i < thumbPositions.length; i++) {
                printWriter.println(thumbPositions[i] + " " + values[i].getRed() + " " + values[i].getGreen() + " "
                        + values[i].getBlue() + " " + values[i].getAlpha());
            }
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final GradientPresetPersistence.GradientPreset other = (GradientPresetPersistence.GradientPreset) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }
    }
}
