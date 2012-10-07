/*
 Copyright 2008-2012 Gephi
 Authors : Taras Klaskovsky <megaterik@gmail.com>
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
package org.gephi.edgelayout.spi;

import java.awt.Color;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperty;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Taras Klaskovsky <megaterik@gmail.com>
 */
@ServiceProvider(service = ColorChooserController.class)
public class ColorChooserControllerImpl implements ColorChooserController {

    private float[] thumbPositions;
    private Color[] colors;

    @Override
    public Color getColor(float percentage) {
        return (Color) getValue(percentage);
    }

    public Object getValue(float pos) {
        for (int a = 0; a < thumbPositions.length - 1; a++) {
            if (thumbPositions[a] == pos) {
                return colors[a];
            }
            if (thumbPositions[a] < pos && pos < thumbPositions[a + 1]) {
                float v = (pos - thumbPositions[a]) / (thumbPositions[a + 1] - thumbPositions[a]);
                return tween((Color) colors[a], (Color) colors[a + 1], v);
            }
        }
        if (pos < thumbPositions[0]) {
            return (Color) colors[0];
        }
        if (pos > thumbPositions[thumbPositions.length - 1]) {
            return (Color) colors[colors.length - 1];
        }
        return null;
    }

    private static Color tween(Color c1, Color c2, float p) {
        return new Color(
                (int) (c1.getRed() * (1 - p) + c2.getRed() * (p)),
                (int) (c1.getGreen() * (1 - p) + c2.getGreen() * (p)),
                (int) (c1.getBlue() * (1 - p) + c2.getBlue() * (p)),
                (int) (c1.getAlpha() * (1 - p) + c2.getAlpha() * (p)));
    }

    @Override
    public float[] getThumbPositions() {
        return thumbPositions;
    }

    @Override
    public void setThubmPositions(float[] thumbPositions) {
        this.thumbPositions = thumbPositions;
    }

    @Override
    public Color[] getColors() {
        return colors;
    }

    @Override
    public void setColors(Color[] colors) {
        this.colors = colors;
    }
}
