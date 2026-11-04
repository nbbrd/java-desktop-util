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
package ec.util.demo;

import static ec.util.list.swing.JListSelection.ORIENTATION_PROPERTY;
import static ec.util.list.swing.JListSelection.SEARCH_VISIBLE_PROPERTY;
import static ec.util.list.swing.JListSelection.SOURCE_HEADER_PROPERTY;

import ec.util.demo.ext.Ikons;
import ec.util.demo.ext.JDemoPane;
import ec.util.list.swing.JListSelection;
import ec.util.list.swing.JLists;
import ec.util.various.swing.BasicSwingLauncher;
import ec.util.various.swing.JCommand;
import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;
import lombok.NonNull;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

/**
 * @author Philippe Charles
 */
public final class JListSelectionDemo {

    public static void main(String[] arg) {
        new BasicSwingLauncher()
                .content(() -> JDemoPane.of(JListSelectionDemo.create()))
                .launch();
    }

    public static JComponent create() {
        JListSelection<MaterialDesign> result = new JListSelection<>();
        result.setCellRenderer(JLists.cellRendererOf(JListSelectionDemo::applyIcon));
        result.setSearchTextProvider(MaterialDesign::getDescription);
        result.setSearchVisible(true);
        result.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        result.setComponentPopupMenu(createPopupMenu(result));
        ToggleHeadersCommand.INSTANCE.execute(result);
        resetItems(result);
        return result;
    }

    private static JPopupMenu createPopupMenu(JListSelection<MaterialDesign> list) {
        JPopupMenu result = list.createPopupMenu();
        result.addSeparator();
        JMenu search = new JMenu("Search");
        search.add(new JCheckBoxMenuItem(
                        new JCommand<JListSelection<MaterialDesign>>() {
                            @Override
                            public void execute(@NonNull JListSelection<MaterialDesign> c) {
                                c.setSearchVisible(!c.isSearchVisible());
                            }

                            @Override
                            public boolean isSelected(@NonNull JListSelection<MaterialDesign> c) {
                                return c.isSearchVisible();
                            }

                            @Override
                            public @NonNull ActionAdapter toAction(@NonNull JListSelection<MaterialDesign> c) {
                                return super.toAction(c).withWeakPropertyChangeListener(c, SEARCH_VISIBLE_PROPERTY);
                            }
                        }.toAction(list)))
                .setText("Show search bar");
        search.addSeparator();
        search.add(JCommand.<JListSelection<MaterialDesign>>of(c -> showSearch(c, "arrow"))
                        .toAction(list))
                .setText("Try \"arrow\" - then Add all matches");
        search.add(JCommand.<JListSelection<MaterialDesign>>of(c -> showSearch(c, "  ArRoW  "))
                        .toAction(list))
                .setText("Try mixed case and surrounding spaces");
        search.add(JCommand.<JListSelection<MaterialDesign>>of(c -> showSearch(c, "no-such-icon"))
                        .toAction(list))
                .setText("Try a search with no matches");
        search.addSeparator();
        search.add(JCommand.<JListSelection<MaterialDesign>>of(c -> c.setSearchText(""))
                        .toAction(list))
                .setText("Clear search (Escape)");
        result.add(search);

        JMenu layout = new JMenu("Layout");
        ButtonGroup orientations = new ButtonGroup();
        JRadioButtonMenuItem horizontal = orientationItem(list, SwingConstants.HORIZONTAL);
        horizontal.setText("Side by side");
        JRadioButtonMenuItem vertical = orientationItem(list, SwingConstants.VERTICAL);
        vertical.setText("Stacked");
        orientations.add(horizontal);
        orientations.add(vertical);
        layout.add(horizontal);
        layout.add(vertical);
        layout.addSeparator();
        layout.add(new JCheckBoxMenuItem(ToggleHeadersCommand.INSTANCE.toAction(list)))
                .setText("Show headers and usage hints");
        result.add(layout);

        JMenu data = new JMenu("Demo data");
        data.add(JCommand.<JListSelection<MaterialDesign>>of(JListSelectionDemo::resetItems)
                        .toAction(list))
                .setText("Reset all icons and selection");
        data.add(JCommand.<JListSelection<MaterialDesign>>of(c -> {
                            c.getSourceModel().clear();
                            c.getTargetModel().clear();
                        })
                        .toAction(list))
                .setText("Empty both lists");
        result.add(data);
        return result;
    }

    private static JRadioButtonMenuItem orientationItem(JListSelection<MaterialDesign> list, int orientation) {
        return new JRadioButtonMenuItem(
                new JCommand<JListSelection<MaterialDesign>>() {
                    @Override
                    public void execute(@NonNull JListSelection<MaterialDesign> c) {
                        c.setOrientation(orientation);
                    }

                    @Override
                    public boolean isSelected(@NonNull JListSelection<MaterialDesign> c) {
                        return c.getOrientation() == orientation;
                    }

                    @Override
                    public @NonNull ActionAdapter toAction(@NonNull JListSelection<MaterialDesign> c) {
                        return super.toAction(c).withWeakPropertyChangeListener(c, ORIENTATION_PROPERTY);
                    }
                }.toAction(list));
    }

    private static void showSearch(JListSelection<MaterialDesign> list, String query) {
        list.setSearchVisible(true);
        list.setSearchText(query);
        list.getActionMap().get("focusSearch").actionPerformed(null);
    }

    private static void resetItems(JListSelection<MaterialDesign> list) {
        list.setSearchText("");
        list.getSourceModel().clear();
        list.getTargetModel().clear();
        Stream.of(MaterialDesign.values()).forEach(list.getSourceModel()::addElement);
    }

    private static final class ToggleHeadersCommand extends JCommand<JListSelection<MaterialDesign>> {

        public static final ToggleHeadersCommand INSTANCE = new ToggleHeadersCommand();

        @Override
        public void execute(@NonNull JListSelection<MaterialDesign> c) {
            if (c.getSourceHeader() == null) {
                c.setSourceHeader(newLabel("Available icons (try searching for \"arrow\"):", SwingConstants.LEADING));
                c.setSourceFooter(newLabel("Ctrl+F: search | Enter or double-click: add", SwingConstants.CENTER));
                c.setTargetHeader(newLabel("Chosen icons:", SwingConstants.LEADING));
                c.setTargetFooter(newLabel("Drag to reorder | Enter or double-click: remove", SwingConstants.CENTER));
            } else {
                c.setSourceHeader(null);
                c.setSourceFooter(null);
                c.setTargetHeader(null);
                c.setTargetFooter(null);
            }
        }

        @Override
        public boolean isSelected(@NonNull JListSelection<MaterialDesign> c) {
            return c.getSourceHeader() != null;
        }

        @Override
        public @NonNull ActionAdapter toAction(@NonNull JListSelection<MaterialDesign> component) {
            return super.toAction(component).withWeakPropertyChangeListener(component, SOURCE_HEADER_PROPERTY);
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
        c.setToolTipText(icon.getDescription() + " (" + icon.name() + ")");
        c.setIcon(Ikons.of(icon, c.getFont().getSize()));
    }
}
