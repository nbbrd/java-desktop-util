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
package _demo;

import ec.util.grid.swing.AbstractGridModel;
import ec.util.grid.swing.GridModel;
import ec.util.grid.swing.GridModels;
import ec.util.grid.swing.JGrid;
import ec.util.various.swing.BasicSwingLauncher;
import ec.util.various.swing.JCommand;
import ec.util.various.swing.ModernUI;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

import static ec.util.grid.swing.JGrid.*;

/**
 * @author Philippe Charles
 */
public final class JGridDemo extends JPanel {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .content(JGridDemo.class)
                .title("Grid Demo")
                .size(750, 300)
                .launch();
    }

    private final JGrid grid;
    private final JTextArea chart;

    public JGridDemo() {
        this.grid = new JGrid();
        this.chart = new JTextArea();

        SampleData sampleData = new SampleData();

        grid.setModel(sampleData.asModel());
        grid.setPreferredSize(new Dimension(350, 10));
        grid.setRowSelectionAllowed(true);
        grid.setColumnSelectionAllowed(true);
        grid.setDefaultRenderer(Object.class, new CustomCellRenderer(grid));
        grid.setComponentPopupMenu(createGridMenu().getPopupMenu());

        chart.setPreferredSize(new Dimension(350, 10));

        enableSync();

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, ModernUI.withEmptyBorders(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, grid, chart)));
    }

    private JMenu createGridMenu() {
        JMenu result = new JMenu();

        result.add(apply(grid, MODEL_PROPERTY, GridModels.empty())).setText("Clear");
        result.add(apply(grid, MODEL_PROPERTY, grid.getModel())).setText("Fill");

        result.addSeparator();
        result.add(new JCheckBoxMenuItem(toggle(grid, DRAG_ENABLED_PROPERTY))).setText("Enable drag");
        result.add(new JCheckBoxMenuItem(toggle(grid, ROW_SELECTION_ALLOWED_PROPERTY))).setText("Row selection");
        result.add(new JCheckBoxMenuItem(toggle(grid, COLUMN_SELECTION_ALLOWED_PROPERTY))).setText("Column selection");

        result.addSeparator();
        JMenu menu = new JMenu("Zoom");
        for (int o : new int[]{200, 100, 75, 50, 25}) {
            Font font = getFont();
            font = font.deriveFont(font.getSize2D() * (o / 100f));
            menu.add(new JCheckBoxMenuItem(apply(grid, "font", font))).setText(o + "%");
        }
        result.add(menu);

        return result;
    }

    private void enableSync() {
        PropertyChangeListener listener = new PropertyChangeListener() {
            boolean updating = false;

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (updating) {
                    return;
                }
                updating = true;
                switch (evt.getPropertyName()) {
                    case JGrid.HOVERED_CELL_PROPERTY:
                    case JGrid.SELECTED_CELL_PROPERTY:
                        chart.setText("HOVERED_CELL_PROPERTY: " + (grid.getHoveredCell())
                                + "\nSELECTED_CELL_PROPERTY: " + (grid.getSelectedCell())
                        );
                        break;
                }
                updating = false;
            }
        };

        grid.addPropertyChangeListener(listener);
        chart.addPropertyChangeListener(listener);
    }

    //<editor-fold defaultstate="collapsed" desc="Models">
    private static final class SampleData {

        private final long startTimeMillis = new Date().getTime();
        private final double[][] values = getValues(3, 12 * 3, new Random(), startTimeMillis);

        private static double[][] getValues(int series, int obs, Random rng, long startTimeMillis) {
            double[][] result = new double[series][obs];
            for (int i = 0; i < series; i++) {
                for (int j = 0; j < obs; j++) {
                    result[i][j] = Math.abs((100 * (Math.cos(startTimeMillis * i))) + (100 * (Math.sin(startTimeMillis) - Math.cos(rng.nextDouble()) + Math.tan(rng.nextDouble())))) - 50;
                }
            }
            return result;
        }

        public GridModel asModel() {
            return new AbstractGridModel() {
                final Calendar cal = Calendar.getInstance();
                final DateFormat format = new SimpleDateFormat("yyyy-MM");

                @Override
                public String getRowName(int rowIndex) {
                    cal.setTimeInMillis(startTimeMillis);
                    cal.add(Calendar.MONTH, rowIndex);
                    return format.format(cal.getTimeInMillis());
                }

                @Override
                public String getColumnName(int column) {
                    return "Series " + column;
                }

                @Override
                public int getRowCount() {
                    return values[0].length;
                }

                @Override
                public int getColumnCount() {
                    return values.length;
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                    return values[columnIndex][rowIndex];
                }
            };
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Renderers">
    private static final class CustomCellRenderer implements TableCellRenderer {

        private final NumberFormat format;
        private final TableCellRenderer delegate;

        public CustomCellRenderer(JGrid grid) {
            this.format = new DecimalFormat("#.00");
            this.delegate = grid.getDefaultRenderer(Object.class);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String formattedValue = format.format(value);
            Component result = delegate.getTableCellRendererComponent(table, formattedValue, isSelected, hasFocus, row, column);
            if (result instanceof JLabel) {
                JLabel label = (JLabel) result;
                label.setToolTipText(formattedValue);
                label.setHorizontalAlignment(JLabel.TRAILING);
            }
            return result;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Commands">
    private static Action apply(JGrid grid, String propertyName, Object value) {
        return new ApplyProperty(JGrid.class, propertyName, value).toAction(grid);
    }

    private static Action toggle(JGrid grid, String propertyName) {
        return new ToggleProperty(JGrid.class, propertyName).toAction(grid);
    }

    private static PropertyDescriptor lookupProperty(Class<?> clazz, String propertyName) throws IllegalArgumentException {
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
    //</editor-fold>
}
