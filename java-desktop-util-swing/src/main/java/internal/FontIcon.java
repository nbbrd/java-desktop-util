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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javax.swing.Icon;
import javax.swing.JLabel;
import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.Wither
@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FontIcon implements Icon {

    @NonNull
    public static FontIcon of(char code, @NonNull Font font, int width, int height, @Nullable Color color, double angle) {
        return new FontIcon(code, font, width, height, color, angle);
    }

    @NonNull
    public static FontIcon of(char code, @NonNull Font font, @Nullable Color color, double angle) {
        Dimension iconSize = getIconSize(code, font);
        return new FontIcon(code, font, iconSize.width, iconSize.height, color, angle);
    }

    private final char code;
    private final Font font;
    private final int width;
    private final int height;
    @Nullable
    private final Color color;
    private final double angle;

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create(x, y, width, height);

        g2d.setFont(font);
        g2d.setColor(getColor(c));

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        Point2D.Float position = getCharPosition(g2d);

        if (angle != 0) {
            AffineTransform trans = new AffineTransform();
            trans.rotate(Math.toRadians(angle), width / 2f, height / 2f);
            g2d.transform(trans);
        }

        g2d.drawString(String.valueOf(code), position.x, position.y);

        g2d.dispose();
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }

    private Color getColor(Component c) {
        Color foreground = color != null ? color : c.getForeground();
        return c.isEnabled() ? foreground : Colors.toGray(foreground);
    }

    private Point2D.Float getCharPosition(Graphics2D g2d) {
        // Fix FontMetrics & rotate bug https://bugs.openjdk.java.net/browse/JDK-8205046
        AffineTransform savedTransform = g2d.getTransform();
        g2d.setTransform(new AffineTransform());

        FontMetrics fm = g2d.getFontMetrics();
        float x = (width - fm.charWidth(code)) / 2f;
        float y = (fm.getAscent() + (fm.getHeight() - (fm.getAscent() + fm.getDescent())) / 2f);

        g2d.setTransform(savedTransform);

        return new Point2D.Float(x, y);
    }

    private static Dimension getIconSize(char code, Font font) {
        JLabel label = new JLabel(String.valueOf(code));
        label.setFont(font);
        return label.getPreferredSize();
    }
}
