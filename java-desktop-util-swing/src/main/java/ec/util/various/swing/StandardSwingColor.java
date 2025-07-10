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
package ec.util.various.swing;

import internal.InternalUtil;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * http://alvinalexander.com/java/java-uimanager-color-keys-list
 * http://nadeausoftware.com/articles/2008/11/all_ui_defaults_names_common_java_look_and_feels_windows_mac_os_x_and_linux
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public enum StandardSwingColor implements UIItem<Color> {

    TABLE_HEADER_BACKGROUND("TableHeader.background"),
    TABLE_HEADER_FOREGROUND("TableHeader.foreground"),
    TABLE_BACKGROUND("Table.background"),
    TABLE_FOREGROUND("Table.foreground"),
    TABLE_SELECTION_BACKGROUND("Table.selectionBackground"),
    TABLE_SELECTION_FOREGROUND("Table.selectionForeground"),
    TEXT_FIELD_INACTIVE_BACKGROUND("TextField.inactiveBackground"),
    TEXT_FIELD_INACTIVE_FOREGROUND("TextField.inactiveForeground"),
    CONTROL("control");

    private final String key;

    @Override
    public @NonNull String key() {
        return key;
    }

    @Override
    public Color value() {
        Color result = UIManager.getColor(key);
        if (result != null) return result;
        switch (this) {
            case TABLE_HEADER_BACKGROUND:
                return TABLE.get().getTableHeader().getBackground();
            case TABLE_HEADER_FOREGROUND:
                return TABLE.get().getTableHeader().getForeground();
            case TABLE_BACKGROUND:
                return TABLE.get().getBackground();
            case TABLE_FOREGROUND:
                return TABLE.get().getForeground();
            case TABLE_SELECTION_BACKGROUND:
                return TABLE.get().getSelectionBackground();
            case TABLE_SELECTION_FOREGROUND:
                return TABLE.get().getSelectionForeground();
            case TEXT_FIELD_INACTIVE_FOREGROUND:
                return TEXT_FIELD.get().getDisabledTextColor();
            case CONTROL:
                return PANEL.get().getBackground();
            default:
                return null;
        }
    }

    @Deprecated
    @NonNull
    public Color or(@NonNull Color fallback) {
        Objects.requireNonNull(fallback);
        Color result = value();
        return result != null ? result : fallback;
    }

    private static final Supplier<JTable> TABLE = InternalUtil.getLazyResource(JTable::new);
    private static final Supplier<JTextField> TEXT_FIELD = InternalUtil.getLazyResource(JTextField::new);
    private static final Supplier<JPanel> PANEL = InternalUtil.getLazyResource(JPanel::new);
}
