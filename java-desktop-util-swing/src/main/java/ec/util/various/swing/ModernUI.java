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
package ec.util.various.swing;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.Border;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
public final class ModernUI {

    private ModernUI() {
        // static class
    }

    @NonNull
    public static <X extends JScrollPane> X withEmptyBorders(@NonNull X scrollPane) {
        Border empty = BorderFactory.createEmptyBorder();
        //On GTK L&F, the viewport border must be set to empty (not null!) or we still get border buildup
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBorder(empty);
        return scrollPane;
    }

    @NonNull
    public static <X extends JSplitPane> X withEmptyBorders(@NonNull X splitPane) {
        Border empty = BorderFactory.createEmptyBorder();
        splitPane.setBorder(empty);
        return splitPane;
    }

    @NonNull
    public static Border createDropBorder(@NonNull Color color) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createDashedBorder(color, 3.0f, 5.0f, 3.0f, true));
    }
}
