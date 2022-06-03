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

import ec.util.various.swing.UIItem;
import java.awt.Font;
import java.util.function.Supplier;
import javax.swing.Icon;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public enum ToolBarIcon implements UIItem<Icon> {

    MOVE_UP("ToolBar.moveUpIcon"),
    MOVE_DOWN("ToolBar.moveDownIcon"),
    MOVE_LEFT("ToolBar.moveLeftIcon"),
    MOVE_RIGHT("ToolBar.moveRightIcon"),
    MOVE_ALL_UP("ToolBar.moveAllUpIcon"),
    MOVE_ALL_DOWN("ToolBar.moveAllDownIcon"),
    MOVE_ALL_LEFT("ToolBar.moveAllLeftIcon"),
    MOVE_ALL_RIGHT("ToolBar.moveAllRightIcon"),
    MOVE_HORIZONTALLY("ToolBar.moveHorizontallyIcon"),
    MOVE_VERTICALLY("ToolBar.moveVerticallyIcon");

    private final String key;

    @Override
    public @NonNull String key() {
        return key;
    }

    @Override
    public Icon value() {
        return UIManager.getIcon(key);
    }

    public void put(@Nullable Icon icon) {
        UIManager.put(key, icon);
    }

    static {
        registerDefaultValues();
    }

    private static void registerDefaultValues() {
        Supplier<Font> font = InternalUtil.getLazyResource(() -> new JToolBar().getFont());
        putIfAbsent("ToolBar.moveUpIcon", '\u2b61', font);
        putIfAbsent("ToolBar.moveDownIcon", '\u2b63', font);
        putIfAbsent("ToolBar.moveLeftIcon", '\u2b60', font);
        putIfAbsent("ToolBar.moveRightIcon", '\u2b62', font);
        putIfAbsent("ToolBar.moveAllUpIcon", '\u21d1', font);
        putIfAbsent("ToolBar.moveAllDownIcon", '\u21d3', font);
        putIfAbsent("ToolBar.moveAllLeftIcon", '\u21d0', font);
        putIfAbsent("ToolBar.moveAllRightIcon", '\u21d2', font);
        putIfAbsent("ToolBar.moveHorizontallyIcon", '\u21c4', font);
        putIfAbsent("ToolBar.moveVerticallyIcon", '\u21c5', font);
    }

    private static void putIfAbsent(String key, char fallback, Supplier<Font> font) {
        if (UIManager.get(key) == null) {
            UIManager.put(key, FontIcon.of(fallback, InternalUtil.resizeByFactor(font.get(), 2), null, 0));
        }
    }
}
