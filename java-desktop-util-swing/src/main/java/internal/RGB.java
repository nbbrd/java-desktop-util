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

import lombok.NonNull;

import java.util.Locale;


/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class RGB {

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
        return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF));
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
        float fromRatio = (float) ratio;
        float toRatio = 1 - fromRatio;

        float r = ((from >> 16) & 0xFF) * fromRatio + ((to >> 16) & 0xFF) * toRatio;
        float g = ((from >> 8) & 0xFF) * fromRatio + ((to >> 8) & 0xFF) * toRatio;
        float b = (from & 0xFF) * fromRatio + (to & 0xFF) * toRatio;

        return rgb((int) r, (int) g, (int) b);
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
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    /**
     * Return the "distance" between two colors.
     *
     * @param color1 First color [r,g,b].
     * @param color2 Second color [r,g,b].
     * @return Distance between colors.
     */
    public static double colorDistance(double[] color1, double[] color2) {
        return colorDistance(color1[0], color1[1], color1[2],
                color2[0], color2[1], color2[2]);
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
        double a = r2 - r1;
        double b = g2 - g1;
        double c = b2 - b1;

        return Math.sqrt(a * a + b * b + c * c);
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
        // Measure distance to white and black respectively
        double dWhite = colorDistance(r, g, b, 1.0, 1.0, 1.0);
        double dBlack = colorDistance(r, g, b, 0.0, 0.0, 0.0);

        return dBlack < dWhite;
    }

    @NonNull
    public static String toHex(int rgb) {
        return toHex((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    @NonNull
    public static String toHex(int r, int g, int b) {
        return String.format(Locale.ROOT, "#%02x%02x%02x", r, g, b);
    }

    /**
     * @see http://www.java2s.com/Code/Java/2D-Graphics-GUI/RGBGrayFilter.htm
     * @param rgb
     * @return
     */
    public static int toGray(int rgb) {
        // Find the average of red, green, and blue.
        float avg = (((rgb >> 16) & 0xff) / 255f
                + ((rgb >> 8) & 0xff) / 255f
                + (rgb & 0xff) / 255f) / 3;
        // Pull out the alpha channel.
        float alpha = (((rgb >> 24) & 0xff) / 255f);

        // Calculate the average.
        // Sun's formula: Math.min(1.0f, (1f - avg) / (100.0f / 35.0f) + avg);
        // The following formula uses less operations and hence is faster.
        avg = Math.min(1.0f, 0.35f + 0.65f * avg);
        // Convert back into RGB.
        return (int) (alpha * 255f) << 24
                | (int) (avg * 255f) << 16
                | (int) (avg * 255f) << 8
                | (int) (avg * 255f);
    }
}
