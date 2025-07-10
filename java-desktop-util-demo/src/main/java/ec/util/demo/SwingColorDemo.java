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
package ec.util.demo;

import ec.util.demo.ext.JDemoPane;
import ec.util.list.swing.JLists;
import ec.util.various.swing.BasicSwingLauncher;
import ec.util.various.swing.ModernUI;
import ec.util.various.swing.StandardSwingColor;
import ec.util.various.swing.UIItem;
import internal.ColorIcon;
import internal.Colors;
import internal.ExtSwingColor;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Philippe Charles
 */
public final class SwingColorDemo {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .content(() -> JDemoPane.of(create()))
                .title("Swing Colors")
                .size(400, 300)
                .launch();
    }

    public static JComponent create() {
        List<UIItem<Color>> values = new ArrayList<>();
        Collections.addAll(values, StandardSwingColor.values());
        Collections.addAll(values, ExtSwingColor.values());
        values.sort(Comparator.comparing(UIItem::key));

        JList<UIItem<Color>> list = new JList<>();
        list.setModel(JLists.modelOf(values));
        list.setCellRenderer(JLists.cellRendererOf(SwingColorDemo::renderColor));

        return ModernUI.withEmptyBorders(new JScrollPane(list));
    }

    private static void renderColor(JLabel label, UIItem<Color> value) {
        Color color = value.value();
        if (color != null) {
            label.setText(value.key() + " (" + Colors.toHex(color).toUpperCase(Locale.ROOT) + ")");
        } else {
            label.setText(value.key() + " (null)");
        }
        label.setIcon(ColorIcon.of(color, label.getFont().getSize()));
    }
}
