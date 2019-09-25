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
import java.awt.Color;
import java.util.Objects;
import java.util.function.Supplier;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import org.checkerframework.checker.nullness.qual.NonNull;

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
    public String key() {
        return key;
    }

    @Override
    public Color value() {
        Color result = null;
        switch (this) {
            case TABLE_HEADER_BACKGROUND:
                result = TABLE.get().getTableHeader().getBackground();
                break;
            case TABLE_HEADER_FOREGROUND:
                result = TABLE.get().getTableHeader().getForeground();
                break;
            case TABLE_BACKGROUND:
                result = TABLE.get().getBackground();
                break;
            case TABLE_FOREGROUND:
                result = TABLE.get().getForeground();
                break;
            case TABLE_SELECTION_BACKGROUND:
                result = TABLE.get().getSelectionBackground();
                break;
            case TABLE_SELECTION_FOREGROUND:
                result = TABLE.get().getSelectionForeground();
                break;
            case TEXT_FIELD_INACTIVE_BACKGROUND:
                break;
            case TEXT_FIELD_INACTIVE_FOREGROUND:
                result = TEXT_FIELD.get().getDisabledTextColor();
                break;
            case CONTROL:
                result = PANEL.get().getBackground();
                break;
        }
        return result != null ? result : UIManager.getColor(key);
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
