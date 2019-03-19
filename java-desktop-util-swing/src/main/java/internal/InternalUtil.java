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

import java.awt.Font;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalUtil {

    @Nonnull
    public Font resizeByFactor(@Nonnull Font font, float factor) {
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

    public final char LEFTWARDS_ARROW_OVER_RIGHTWARDS_ARROW = '\u21c4';
    public final char UPWARDS_ARROW_LEFTWARDS_OF_DOWNWARDS_ARROW = '\u21c5';
}
