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

import ec.util.list.swing.JListOrdering;
import ec.util.list.swing.JLists;
import static ec.util.list.swing.JListOrdering.MOVE_DOWN_ACTION;
import static ec.util.list.swing.JListOrdering.MOVE_UP_ACTION;
import ec.util.various.swing.BasicSwingLauncher;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

/**
 *
 * @author Philippe Charles
 */
final class JListOrderingDemo {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .content(JListOrderingDemo::create)
                .size(400, 300)
                .logLevel(Level.FINE)
                .launch();
    }

    private static Component create() throws Exception {
        JListOrdering<MaterialDesign> list = new JListOrdering<>();
        Stream.of(MaterialDesign.values()).limit(10).forEach(list.getModel()::addElement);
        list.setCellRenderer(JLists.cellRendererOf(JListOrderingDemo::applyIcon));

        JToolBar toolBar = new JToolBar();
        toolBar.setOrientation(JToolBar.VERTICAL);
        toolBar.setFloatable(false);

        ActionMap am = list.getActionMap();

        applyIcon(toolBar.add(am.get(MOVE_UP_ACTION)), MaterialDesign.MDI_ARROW_UP_DROP_CIRCLE_OUTLINE);
        applyIcon(toolBar.add(am.get(MOVE_DOWN_ACTION)), MaterialDesign.MDI_ARROW_DOWN_DROP_CIRCLE_OUTLINE);

        JPanel result = new JPanel();
        result.setLayout(new BorderLayout());
        result.add(list, BorderLayout.CENTER);
        result.add(toolBar, BorderLayout.EAST);
        return result;
    }

    private static void applyIcon(JLabel c, MaterialDesign icon) {
        c.setText(icon.getDescription());
        c.setIcon(Ikons.of(icon, c.getFont().getSize()));
    }

    private static void applyIcon(JButton c, MaterialDesign icon) {
        c.setIcon(Ikons.of(icon, c.getFont().getSize() * 2));
    }
}
