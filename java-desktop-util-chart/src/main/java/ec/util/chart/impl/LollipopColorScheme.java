/*
 * Copyright 2013 National Bank of Belgium
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
package ec.util.chart.impl;

import ec.util.chart.ColorScheme;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.NonNull;
import nbbrd.service.ServiceProvider;

/**
 * Android 5 color scheme
 *
 * @author Jeremy Demortier
 */
@ServiceProvider(ColorScheme.class)
public class LollipopColorScheme extends AbstractColorScheme {

    private static final int BLUE = 0x2196f3;
    private static final int RED = 0xf44336;
    private static final int PURPLE = 0x9c27b0;
    private static final int GREEN = 0x4caf50;
    private static final int AMBER = 0xffc107;
    private static final int LIME = 0xcddc39;
    private static final int BROWN = 0x795548;
    private static final int BLUE_GRAY = 0x607d8b;
    private static final int DEEP_ORANGE = 0xff5722;
    private static final int INDIGO = 0x3f51b5;
    private static final int LIGHT_GREEN = 0x8bc34a;
    private static final int PINK = 0xe91e63;
    private static final int TEAL = 0x009688;
    private static final int LIGHT_BLUE = 0x03a9f4;
    private static final int CYAN = 0x00bcd4;
    private static final int DEEP_PURPLE = 0x673ab7;
    private static final int YELLOW = 0xffeb3b;
    private static final int ORANGE = 0xff9800;
    private static final int GRAY = 0x9e9e9e;

    @Override
    public @NonNull String getName() {
        return "Lollipop";
    }

    @Override
    public @NonNull List<Integer> getAreaColors() {
        return Arrays.asList(BLUE,
                RED,
                PURPLE,
                GREEN,
                AMBER,
                LIME,
                BROWN,
                BLUE_GRAY,
                DEEP_ORANGE,
                INDIGO,
                LIGHT_GREEN,
                PINK,
                TEAL,
                LIGHT_BLUE,
                CYAN,
                DEEP_PURPLE,
                YELLOW,
                ORANGE,
                GRAY
        );
    }

    @Override
    public @NonNull Map<KnownColor, Integer> getAreaKnownColors() {
        return knownColors(BLUE, BROWN, GRAY, GREEN, ORANGE, RED, YELLOW);
    }

    @Override
    public int getBackColor() {
        return BasicColor.WHITE;
    }

    @Override
    public int getPlotColor() {
        return BasicColor.WHITE;
    }

    @Override
    public int getGridColor() {
        return BasicColor.LIGHT_GRAY;
    }
}
