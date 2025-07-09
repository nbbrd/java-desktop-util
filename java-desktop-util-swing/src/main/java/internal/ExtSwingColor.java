/*
 * Copyright 2015 National Bank of Belgium
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

import ec.util.various.swing.UIItem;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * https://www.formdev.com/flatlaf/components/
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public enum ExtSwingColor implements UIItem<Color> {

    TABLE_HEADER_HOVER_BACKGROUND("TableHeader.hoverBackground"),
    TABLE_HEADER_HOVER_FOREGROUND("TableHeader.hoverForeground"),
    TABLE_SELECTION_INACTIVE_BACKGROUND("Table.selectionInactiveBackground"),
    TABLE_SELECTION_INACTIVE_FOREGROUND("Table.selectionInactiveForeground");

    private final String key;

    @Override
    public @NonNull String key() {
        return key;
    }

    @Override
    public Color value() {
        return UIManager.getColor(key);
    }

    @Deprecated
    @NonNull
    public Color or(@NonNull Color fallback) {
        Objects.requireNonNull(fallback);
        Color result = value();
        return result != null ? result : fallback;
    }
}
