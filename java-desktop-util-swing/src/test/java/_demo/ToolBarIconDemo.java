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
package _demo;

import ec.util.list.swing.JLists;
import ec.util.various.swing.BasicSwingLauncher;
import ec.util.various.swing.ModernUI;
import ec.util.various.swing.UIItem;
import internal.ToolBarIcon;
import java.awt.Component;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

/**
 *
 * @author Philippe Charles
 */
public final class ToolBarIconDemo {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .content(ToolBarIconDemo::create)
                .size(300, 200)
                .launch();
    }

    private static Component create() {
        ToolBarIcon[] values = ToolBarIcon.values();
        Arrays.sort(values, Comparator.comparing(UIItem::key));

        JList<ToolBarIcon> list = new JList<>(values);
        list.setCellRenderer(JLists.cellRendererOf(ToolBarIconDemo::render));

        return ModernUI.withEmptyBorders(new JScrollPane(list));
    }

    private static void render(JLabel label, ToolBarIcon value) {
        label.setText("<html><b>" + value.name() + "</b><br>" + value.key());
        label.setIcon(value.value());
    }
}
