/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package ec.util.chart.swing;

import ec.util.chart.ColorScheme;
import ec.util.chart.ColorSchemeSupport;
import internal.Colors;
import java.awt.Color;
import lombok.NonNull;

/**
 * An helper class that simplifies the use of a color scheme in the Swing
 * toolkit.
 *
 * @author Philippe Charles
 */
public abstract class SwingColorSchemeSupport extends ColorSchemeSupport<Color> {

    //<editor-fold defaultstate="collapsed" desc="Color tools">
    /**
     * Derives a color by changing its alpha.
     *
     * @param c the original color
     * @param alpha the alpha component
     * @return a non-null color
     */
    @NonNull
    public static Color withAlpha(@NonNull Color c, int alpha) {
        return Colors.withAlpha(c, alpha);
    }

    /**
     * Blends two colors.
     *
     * @param from the non-null first color
     * @param to the non-null second color
     * @param ratio the ratio used to blend
     * @return the non-null blended color
     */
    @NonNull
    public static Color blend(@NonNull Color from, @NonNull Color to, double ratio) {
        return Colors.blend(from, to, ratio);
    }

    /**
     * Calculate the relative luminance of a given Color
     *
     * @see http://en.wikipedia.org/wiki/Luminance_(relative)
     * @param c Color
     * @return Relative luminance value
     */
    public static double getLuminance(Color c) {
        return Colors.getLuminance(c);
    }

    /**
     * Check if a color is more dark than light. Useful if an entity of this
     * color is to be labeled: Use white label on a "dark" color and black label
     * on a "light" color.
     *
     * @param color Color to check.
     * @return True if this is a "dark" color, false otherwise.
     */
    public static boolean isDark(Color color) {
        return Colors.isDark(color);
    }

    /**
     * Converts a RGB value into a Color object.
     *
     * @param value the original RGB value
     * @return a non-null corresponding color
     */
    @NonNull
    public static Color rgbToColor(int value) {
        return Colors.rgbToColor(value);
    }

    /**
     * Converts a color object into a RGB value.
     *
     * @param color the original color
     * @return the corresponding RGB value
     */
    public static int colorToRgb(@NonNull Color color) {
        return Colors.colorToRgb(color);
    }

    @NonNull
    public static String toHex(@NonNull Color color) {
        return Colors.toHex(color);
    }
    //</editor-fold>

    @Override
    public @NonNull Color toColor(int value) {
        return rgbToColor(value);
    }

    // STATIC FACTORY METHODS >
    public static SwingColorSchemeSupport from(final ColorScheme colorScheme) {
        return new SwingColorSchemeSupport() {
            @Override
            public @NonNull ColorScheme getColorScheme() {
                return colorScheme;
            }

            @Override
            public int hashCode() {
                return colorScheme.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return this == obj || (obj instanceof SwingColorSchemeSupport && equals((SwingColorSchemeSupport) obj));
            }

            private boolean equals(SwingColorSchemeSupport other) {
                return colorScheme.equals(other.getColorScheme());
            }

            @Override
            public String toString() {
                return colorScheme.getName();
            }
        };
    }
    // < STATIC FACTORY METHODS
}
