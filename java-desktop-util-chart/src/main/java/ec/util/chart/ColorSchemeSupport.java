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
package ec.util.chart;

import ec.util.chart.ColorScheme.KnownColor;
import internal.RGB;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * An helper class that simplifies the use of a color scheme in a specific
 * toolkit such as Swing, SWT or JavaFx.
 *
 * @author Philippe Charles
 * @param <T> the type of the color object in a toolkit
 */
public abstract class ColorSchemeSupport<T> {

    //<editor-fold defaultstate="collapsed" desc="RGB tools">
    /**
     * Creates an RGB value from the specified red, green and blue values in the
     * range (0 - 255).
     *
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @return the RGB value of a color
     */
    public static int rgb(int r, int g, int b) {
        return RGB.rgb(r, g, b);
    }

    /**
     * Blends two RGB values.
     *
     * @param from the first RGB value
     * @param to the second RGB value
     * @param ratio the ratio used to blend
     * @return the RBG value of he blended color
     */
    public static int blend(int from, int to, double ratio) {
        return RGB.blend(from, to, ratio);
    }

    /**
     * Calculate the relative luminance of a given RGB Color
     *
     * @see http://en.wikipedia.org/wiki/Luminance_(relative)
     * @param r Red value
     * @param g Green value
     * @param b Blue value
     * @return Relative luminance value
     */
    public static double getLuminance(int r, int g, int b) {
        return RGB.getLuminance(r, g, b);
    }

    /**
     * Return the "distance" between two colors.
     *
     * @param color1 First color [r,g,b].
     * @param color2 Second color [r,g,b].
     * @return Distance between colors.
     */
    public static double colorDistance(double[] color1, double[] color2) {
        return RGB.colorDistance(color1, color2);
    }

    /**
     * Return the "distance" between two colors. The rgb entries are taken to be
     * coordinates in a 3D space [0.0-1.0], and this method returns the distance
     * between the coordinates for the first and second color.
     *
     * @param r1 First color red value
     * @param g1 First color green value
     * @param b1 First color blue value
     * @param r2 Second color red value
     * @param g2 Second color green value
     * @param b2 Second color blue value
     * @return Distance between colors.
     */
    public static double colorDistance(double r1, double g1, double b1,
            double r2, double g2, double b2) {
        return RGB.colorDistance(r1, g1, b1, r2, g2, b2);
    }

    /**
     * Check if a color is more dark than light. Useful if an entity of this
     * color is to be labeled: Use white label on a "dark" color and black label
     * on a "light" color.
     *
     * @param r Red value
     * @param g Green value
     * @param b Blue value
     * @return True if this is a "dark" color, false otherwise.
     */
    public static boolean isDark(double r, double g, double b) {
        return RGB.isDark(r, g, b);
    }

    @Nonnull
    public static String toHex(int rgb) {
        return RGB.toHex(rgb);
    }

    @Nonnull
    public static String toHex(int r, int g, int b) {
        return RGB.toHex(r, g, b);
    }
    //</editor-fold>

    /**
     * Converts an RGB value into a color object.
     *
     * @param rgb a RGB value
     * @return a non-null color object
     */
    @Nonnull
    abstract public T toColor(int rgb);

    /**
     * Returns the current color scheme.
     *
     * @return a non-null color scheme
     */
    @Nonnull
    abstract public ColorScheme getColorScheme();

    @Nonnull
    public T getAreaColor(int series) {
        List<T> tmp = getColors().areaColors;
        return tmp.get(series % tmp.size());
    }

    @Nonnull
    public T getLineColor(int series) {
        List<T> tmp = getColors().lineColors;
        return tmp.get(series % tmp.size());
    }

    @Nonnull
    public T getAreaColor(@Nonnull KnownColor color) {
        return getColors().areaKnownColors.get(color);
    }

    @Nonnull
    public T getLineColor(@Nonnull KnownColor color) {
        return getColors().lineKnownColors.get(color);
    }

    @Nonnull
    public T getBackColor() {
        return getColors().backColor;
    }

    @Nonnull
    public T getPlotColor() {
        return getColors().plotColor;
    }

    @Nonnull
    public T getGridColor() {
        return getColors().gridColor;
    }

    @Nonnull
    public T getTextColor() {
        return getColors().textColor;
    }

    @Nonnull
    public T getAxisColor() {
        return getColors().axisColor;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation">
    private Colors<T> cache;

    private Colors<T> getColors() {
        ColorScheme colorScheme = getColorScheme();
        if (cache == null || !cache.name.equals(colorScheme.getName())) {
            cache = createColors(colorScheme);
        }
        return cache;
    }

    private Colors<T> createColors(ColorScheme colorScheme) {
        List<T> areaColors = colorScheme.getAreaColors().stream().map(this::toColor).collect(Collectors.toList());
        List<T> lineColors = colorScheme.getLineColors().stream().map(this::toColor).collect(Collectors.toList());
        Map<KnownColor, T> areaKnownColors = new EnumMap<>(KnownColor.class);
        colorScheme.getAreaKnownColors().forEach((k, v) -> areaKnownColors.put(k, toColor(v)));
        Map<KnownColor, T> lineKnownColors = new EnumMap<>(KnownColor.class);
        colorScheme.getLineKnownColors().forEach((k, v) -> lineKnownColors.put(k, toColor(v)));
        T backColor = toColor(colorScheme.getBackColor());
        T plotColor = toColor(colorScheme.getPlotColor());
        T gridColor = toColor(colorScheme.getGridColor());
        T textColor = toColor(colorScheme.getTextColor());
        T axisColor = toColor(colorScheme.getAxisColor());
        return new Colors(colorScheme.getName(), areaColors, lineColors, areaKnownColors, lineKnownColors, backColor, plotColor, gridColor, textColor, axisColor);
    }

    private static final class Colors<T> {

        final String name;
        final List<T> areaColors;
        final List<T> lineColors;
        final Map<KnownColor, T> areaKnownColors;
        final Map<KnownColor, T> lineKnownColors;
        final T backColor;
        final T plotColor;
        final T gridColor;
        final T textColor;
        final T axisColor;

        public Colors(String name, List<T> areaColors, List<T> lineColors, Map<KnownColor, T> areaKnownColors, Map<KnownColor, T> lineKnownColors, T backColor, T plotColor, T gridColor, T textColor, T axisColor) {
            this.name = name;
            this.areaColors = areaColors;
            this.lineColors = lineColors;
            this.areaKnownColors = areaKnownColors;
            this.lineKnownColors = lineKnownColors;
            this.backColor = backColor;
            this.plotColor = plotColor;
            this.gridColor = gridColor;
            this.textColor = textColor;
            this.axisColor = axisColor;
        }
    }
    //</editor-fold>
}
