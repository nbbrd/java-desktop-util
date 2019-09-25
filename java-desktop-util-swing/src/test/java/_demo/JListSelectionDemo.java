/*
 * Copyright 2016 National Bank of Belgium
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

import ec.util.list.swing.JListSelection;
import static ec.util.list.swing.JListSelection.APPLY_HORIZONTAL_ACTION;
import ec.util.list.swing.JLists;
import static ec.util.list.swing.JListSelection.SOURCE_HEADER_PROPERTY;
import ec.util.various.swing.BasicSwingLauncher;
import ec.util.various.swing.JCommand;
import java.awt.Component;
import java.util.stream.Stream;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

/**
 *
 * @author Philippe Charles
 */
public final class JListSelectionDemo {

    public static void main(String[] arg) {
        new BasicSwingLauncher()
                .content(JListSelectionDemo::create)
                .launch();
    }

    private static Component create() {
        JListSelection<MaterialDesign> result = new JListSelection<>();
        result.setCellRenderer(JLists.cellRendererOf(JListSelectionDemo::applyIcon));
        result.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        result.setComponentPopupMenu(createPopupMenu(result));
        report(ToggleHeadersCommand.INSTANCE.executeSafely(result));
        report(EmptyCommand.INSTANCE.executeSafely(result));
        return result;
    }

    private static void report(Exception ex) {
    }

    private static JPopupMenu createPopupMenu(JListSelection<MaterialDesign> list) {
        JPopupMenu result = list.createPopupMenu();
        result.addSeparator();
        result.add(new JCheckBoxMenuItem(list.getActionMap().get(APPLY_HORIZONTAL_ACTION))).setText("Horizontal");
        result.add(new JCheckBoxMenuItem(ToggleHeadersCommand.INSTANCE.toAction(list))).setText("Headers");
        result.addSeparator();
        result.add(new JCheckBoxMenuItem(EmptyCommand.INSTANCE.toAction(list))).setText("Empty");
        return result;
    }

    private static final class ToggleHeadersCommand extends JCommand<JListSelection<MaterialDesign>> {

        public static final ToggleHeadersCommand INSTANCE = new ToggleHeadersCommand();

        @Override
        public void execute(JListSelection<MaterialDesign> c) throws Exception {
            if (c.getSourceHeader() == null) {
                c.setSourceHeader(newLabel("Source header:", SwingConstants.LEADING));
                c.setSourceFooter(newLabel("Source footer", SwingConstants.CENTER));
                c.setTargetHeader(newLabel("Target header:", SwingConstants.LEADING));
                c.setTargetFooter(newLabel("Target footer", SwingConstants.CENTER));
            } else {
                c.setSourceHeader(null);
                c.setSourceFooter(null);
                c.setTargetHeader(null);
                c.setTargetFooter(null);
            }
        }

        @Override
        public boolean isSelected(JListSelection<MaterialDesign> c) {
            return c.getSourceHeader() != null;
        }

        @Override
        public ActionAdapter toAction(JListSelection<MaterialDesign> component) {
            return super.toAction(component)
                    .withWeakPropertyChangeListener(component, SOURCE_HEADER_PROPERTY);
        }
    }

    private static final class EmptyCommand extends JCommand<JListSelection<MaterialDesign>> {

        public static final EmptyCommand INSTANCE = new EmptyCommand();

        @Override
        public void execute(JListSelection<MaterialDesign> c) throws Exception {
            if (isSelected(c)) {
                Stream.of(MaterialDesign.values()).limit(10).forEach(c.getSourceModel()::addElement);
            } else {
                c.getSourceModel().clear();
                c.getTargetModel().clear();
            }
        }

        @Override
        public boolean isSelected(JListSelection<MaterialDesign> c) {
            return c.getSourceModel().isEmpty() && c.getTargetModel().isEmpty();
        }
    }

    private static JLabel newLabel(String text, int alignment) {
        JLabel result = new JLabel(text);
        result.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        result.setHorizontalAlignment(alignment);
        return result;
    }

    private static void applyIcon(JLabel c, MaterialDesign icon) {
        c.setText(icon.getDescription());
        c.setIcon(Ikons.of(icon, c.getFont().getSize()));
    }
}
