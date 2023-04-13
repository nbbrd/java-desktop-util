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

import internal.ColorIcon;
import ec.util.list.swing.JLists;
import ec.util.various.swing.BasicSwingLauncher;
import ec.util.various.swing.ModernUI;
import ec.util.various.swing.StandardSwingColor;
import ec.util.various.swing.UIItem;
import internal.Colors;
import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

/**
 *
 * @author Philippe Charles
 */
public final class StandardSwingColorDemo {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .content(StandardSwingColorDemo::create)
                .title("Standard Swing Colors")
                .size(300, 200)
                .launch();
    }

    public static Component create() {
        StandardSwingColor[] values = StandardSwingColor.values();
        Arrays.sort(values, Comparator.comparing(UIItem::key));

        JList<StandardSwingColor> list = new JList<>(values);
        list.setCellRenderer(JLists.cellRendererOf(StandardSwingColorDemo::applyColor));

        return ModernUI.withEmptyBorders(new JScrollPane(list));
    }

    private static void applyColor(JLabel label, StandardSwingColor value) {
        Color color = value.value();
        if (color != null) {
            label.setText(value.key() + " (" + Colors.toHex(color).toUpperCase(Locale.ROOT) + ")");
            label.setIcon(ColorIcon.of(color, label.getFont().getSize()));
        } else {
            label.setText(value.key() + " (null)");
            label.setIcon(null);
        }
    }
}
