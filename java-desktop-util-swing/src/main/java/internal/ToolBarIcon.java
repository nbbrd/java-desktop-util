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
import java.util.EnumMap;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.JToolBar;
import javax.swing.UIManager;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public enum ToolBarIcon {

    MOVE_UP("ToolBar.moveUpIcon", '\u2b61'),
    MOVE_DOWN("ToolBar.moveDownIcon", '\u2b63'),
    MOVE_LEFT("ToolBar.moveLeftIcon", '\u2b60'),
    MOVE_RIGHT("ToolBar.moveRightIcon", '\u2b62'),
    MOVE_ALL_UP("ToolBar.moveAllUpIcon", '\u21d1'),
    MOVE_ALL_DOWN("ToolBar.moveAllDownIcon", '\u21d3'),
    MOVE_ALL_LEFT("ToolBar.moveAllLeftIcon", '\u21d0'),
    MOVE_ALL_RIGHT("ToolBar.moveAllRightIcon", '\u21d2'),
    MOVE_HORIZONTALLY("ToolBar.moveHorizontallyIcon", '\u21c4'),
    MOVE_VERTICALLY("ToolBar.moveVerticallyIcon", '\u21c5');

    @lombok.Getter
    private final String key;

    private final char fallback;

    @Nonnull
    public Icon get() {
        Icon result = UIManager.getIcon(key);
        return result != null ? result : FALLBACKS.computeIfAbsent(this, o -> o.getFallback());
    }

    public void put(@Nullable Icon icon) {
        UIManager.put(key, icon);
    }

    private Icon getFallback() {
        Font font = UIManager.getFont("ToolBar.font");
        if (font == null) {
            font = TOOLBAR.get().getFont();
        }
        return FontIcon.of(fallback, InternalUtil.resizeByFactor(font, 2), null, 0);
    }

    private static final Supplier<JToolBar> TOOLBAR = InternalUtil.getLazyResource(JToolBar::new);
    private static final EnumMap<ToolBarIcon, Icon> FALLBACKS = new EnumMap<>(ToolBarIcon.class);
}
