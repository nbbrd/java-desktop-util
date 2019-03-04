/*
 * Copyright 2019 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package internal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.annotation.Nonnull;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalUtil {

    /**
     * Derives a color by changing its alpha.
     *
     * @param c the original color
     * @param alpha the alpha component
     * @return a non-null color
     */
    @Nonnull
    public Color withAlpha(@Nonnull Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    public Font resizeByFactor(Font font, float factor) {
        return font.deriveFont(font.getSize2D() * factor);
    }

    public final char RIGHTWARDS_TRIANGLE_HEADED_ARROW = '\u2b62';
    public final char DOWNWARDS_TRIANGLE_HEADED_ARROW = '\u2b63';
    public final char LEFTWARDS_TRIANGLE_HEADED_ARROW = '\u2b60';
    public final char UPWARDS_TRIANGLE_HEADED_ARROW = '\u2b61';

    public final char RIGHTWARDS_DOUBLE_ARROW = '\u21d2';
    public final char DOWNWARDS_DOUBLE_ARROW = '\u21d3';
    public final char LEFTWARDS_DOUBLE_ARROW = '\u21d0';
    public final char UPWARDS_DOUBLE_ARROW = '\u21d1';

    public Icon getIcon(char icon, Font font, Color color) {
        return new ImageIcon(getImage(icon, font, color));
    }

    public Image getImage(char icon, Font font, Color color) {
        return getImage(getRenderer(icon, font, color));
    }

    private Image getImage(JComponent renderer) {
        BufferedImage result = new BufferedImage(renderer.getWidth(), renderer.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        renderer.print(g2d);
        g2d.dispose();
        return result;
    }

    private JComponent getRenderer(char icon, Font font, Color color) {
        JLabel result = new JLabel(String.valueOf(icon));
        result.setForeground(color);
        result.setFont(font);
        result.setHorizontalAlignment(JLabel.CENTER);
        result.setVerticalAlignment(JLabel.CENTER);
        Dimension preferredSize = result.getPreferredSize();
        int size = Math.max(preferredSize.width, preferredSize.height);
        result.setSize(size, size);
        return result;
    }
}
