/*
 * Copyright 2026 National Bank of Belgium
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
import ec.util.various.swing.BasicSwingLauncher;
import ec.util.various.swing.JCommand;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import lombok.NonNull;
import nbbrd.desktop.swing.JMasterDetail;

/**
 * Demo for {@link JMasterDetail}.
 *
 * @author Philippe Charles
 */
public final class JMasterDetailDemo extends JPanel {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .content(() -> JDemoPane.of(new JMasterDetailDemo()))
                .title("MasterDetail Demo")
                .size(640, 420)
                .launch();
    }

    private final JMasterDetail masterDetail;

    public JMasterDetailDemo() {
        this.masterDetail = new JMasterDetail();

        // Master: list of country names
        JList<String> masterList = new JList<>(new String[] {
            "Albania", "Belgium", "Croatia", "Denmark", "Estonia",
            "Finland", "Germany", "Hungary", "Iceland", "Japan",
            "Kenya", "Luxembourg", "Morocco", "Norway", "Oman"
        });
        masterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        masterList.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        // Detail: text area reacting to list selection
        JTextArea detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setMargin(new Insets(10, 10, 10, 10));
        detailArea.setText("Select an item from the list to see its details.");

        masterList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = masterList.getSelectedValue();
                if (selected != null) {
                    detailArea.setText("Name:   " + selected + "\n\n"
                            + "This is the detail view for \"" + selected + "\".\n\n"
                            + "Place any component here to show richer information\n"
                            + "about the item selected in the master list.\n\n"
                            + "Use the context menu (right-click) to explore all\n"
                            + "JMasterDetail properties: detail side, visibility\n"
                            + "and divider position.");
                } else {
                    detailArea.setText("Select an item from the list to see its details.");
                }
            }
        });

        JScrollPane masterPane = new JScrollPane(masterList);
        JScrollPane detailPane = new JScrollPane(detailArea);

        masterDetail.setMasterNode(masterPane);
        masterDetail.setDetailNode(detailPane);

        JPopupMenu popup = createPopupMenu();
        masterDetail.setComponentPopupMenu(popup);
        masterPane.setComponentPopupMenu(popup);
        masterList.setComponentPopupMenu(popup);
        detailPane.setComponentPopupMenu(popup);
        detailArea.setComponentPopupMenu(popup);
        addPopupToDividers(masterDetail, popup);

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, masterDetail);
    }

    private static void addPopupToDividers(Container root, JPopupMenu popup) {
        for (Component c : root.getComponents()) {
            if (c instanceof BasicSplitPaneDivider) {
                c.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        showPopup(e);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        showPopup(e);
                    }

                    private void showPopup(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                });
            } else if (c instanceof Container) {
                addPopupToDividers((Container) c, popup);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Popup menu">
    private JPopupMenu createPopupMenu() {
        JPopupMenu result = new JPopupMenu();

        result.add(new JCheckBoxMenuItem(toggle(masterDetail, "detailVisible"))).setText("Show Detail");

        result.addSeparator();
        JMenu sideMenu = new JMenu("Detail Side");
        for (JMasterDetail.DetailSide side : JMasterDetail.DetailSide.values()) {
            sideMenu.add(new JCheckBoxMenuItem(apply(masterDetail, "detailSide", side)))
                    .setText(side.name());
        }
        result.add(sideMenu);

        result.addSeparator();
        JMenu dividerMenu = new JMenu("Divider Position");
        for (double pos : new double[] {0.2, 0.3, 0.5, 0.7, 0.8}) {
            dividerMenu
                    .add(new JCheckBoxMenuItem(apply(masterDetail, "dividerPosition", pos)))
                    .setText((int) (pos * 100) + "%");
        }
        result.add(dividerMenu);

        return result;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Commands">
    private static Action apply(JMasterDetail c, String propertyName, Object value) {
        return new ApplyProperty<>(JMasterDetail.class, propertyName, value).toAction(c);
    }

    private static Action toggle(JMasterDetail c, String propertyName) {
        return new ToggleProperty<>(JMasterDetail.class, propertyName).toAction(c);
    }

    private static PropertyDescriptor lookupProperty(Class<?> clazz, String propertyName) {
        try {
            for (PropertyDescriptor o : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                if (o.getName().equals(propertyName)) {
                    return o;
                }
            }
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }
        throw new IllegalArgumentException(propertyName);
    }

    private static final class ApplyProperty<X extends Component> extends JCommand<X> {

        private final PropertyDescriptor property;
        private final Object value;

        public ApplyProperty(Class<X> clazz, String propertyName, Object value) {
            this.property = lookupProperty(clazz, propertyName);
            this.value = value;
        }

        @Override
        public void execute(@NonNull X component) throws Exception {
            property.getWriteMethod().invoke(component, value);
        }

        @Override
        public boolean isSelected(@NonNull X component) {
            try {
                return Objects.equals(property.getReadMethod().invoke(component), value);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public JCommand.@NonNull ActionAdapter toAction(@NonNull X component) {
            return super.toAction(component).withWeakPropertyChangeListener(component, property.getName());
        }
    }

    private static final class ToggleProperty<X extends Component> extends JCommand<X> {

        private final PropertyDescriptor property;

        public ToggleProperty(Class<X> clazz, String propertyName) {
            this.property = lookupProperty(clazz, propertyName);
            if (!property.getPropertyType().equals(boolean.class)) {
                throw new IllegalArgumentException("Invalid property type: " + property.getPropertyType());
            }
        }

        @Override
        public void execute(@NonNull X component) throws Exception {
            Boolean value = (Boolean) property.getReadMethod().invoke(component);
            property.getWriteMethod().invoke(component, !value);
        }

        @Override
        public boolean isSelected(@NonNull X component) {
            try {
                return (Boolean) property.getReadMethod().invoke(component);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(property.getName(), ex);
            }
        }

        @Override
        public JCommand.@NonNull ActionAdapter toAction(@NonNull X component) {
            return super.toAction(component).withWeakPropertyChangeListener(component, property.getName());
        }
    }
    // </editor-fold>
}
