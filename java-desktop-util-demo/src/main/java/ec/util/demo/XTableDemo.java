/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
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

import ec.util.grid.swing.XTable;
import ec.util.various.swing.BasicSwingLauncher;
import ec.util.various.swing.JCommand;
import ec.util.various.swing.ModernUI;
import ec.util.various.swing.PopupMouseAdapter;
import internal.Colors;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.logging.Level;

import static ec.util.demo.XTableCommand.*;

/**
 *
 * @author Philippe Charles
 */
public final class XTableDemo extends JPanel {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .content(XTableDemo.class)
                .title("XTable Demo")
                .logLevel(Level.FINE)
                .launch();
    }

    public XTableDemo() {
        XTable table = new XTable();
        table.setModel(createModel());
        table.addMouseListener(PopupMouseAdapter.fromMenu(createMenu(table)));

        applyNoDataMessage("<html><center><b><font size=+2>No data</font></b><br>(See popup menu for options)").executeSafely(table);

        setLayout(new BorderLayout());
        add(ModernUI.withEmptyBorders(new JScrollPane(table)));
    }

    private JMenu createMenu(XTable table) {
        JMenu result = new JMenu();

        JMenuItem item;

        item = new JCheckBoxMenuItem(fillModel().toAction(table));
        item.setText("Fill");
        result.add(item);

        item = new JCheckBoxMenuItem(clearModel().toAction(table));
        item.setText("Clear");
        result.add(item);

        item = new JCheckBoxMenuItem(applyDefaultRenderer(Color.class, createCustomRenderer()).toAction(table));
        item.setText("Custom renderer");
        result.add(item);

        item = new JCheckBoxMenuItem(applyDefaultRenderer(Color.class, new DefaultTableCellRenderer()).toAction(table));
        item.setText("Default renderer");
        result.add(item);

        item = new JCheckBoxMenuItem(applyNoDataMessage("Hello world").toAction(table));
        item.setText("Change no-data message");
        result.add(item);

        item = new JMenuItem(applyColumnWidthAsPercentages(.3, .7).toAction(table));
        item.setText("Change columns width");
        result.add(item);

        item = new JMenuItem(new JCommand<>(){

            @Override
            public void execute(@NonNull Object component) throws Exception {
                System.out.println("hello");
            }

            @Override
            public boolean isEnabled(@NonNull Object component) {
                return table.getModel().getRowCount() > 0;
            }
        }.toAction(table).withWeakTableModelListener(table.getModel()));
        item.setText("Enabled on table model row count > 0");
        result.add(item);

        return result;
    }

    static TableModel createModel() {
        return new DefaultTableModel(new String[]{"Color", "Name"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Color.class : String.class;
            }
        };
    }

    static DefaultTableCellRenderer createCustomRenderer() {
        return new DefaultTableCellRenderer() {
            final JToolTip toolTip = super.createToolTip();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Color color = (Color) value;
                setText("Red=" + color.getRed() + ", Green=" + color.getGreen() + ", Blue=" + color.getBlue());
                setToolTipText(Colors.toHex(color));
                setIcon(new ColorIcon(color));
                toolTip.setBackground(color);
                toolTip.setForeground(Colors.isDark(color) ? Color.WHITE : Color.BLACK);
                return this;
            }

            @Override
            public JToolTip createToolTip() {
                return toolTip;
            }
        };
    }

    static class ColorIcon implements Icon {

        private static final int SIZE = 10;
        private final Color color;

        public ColorIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.black);
            g.drawRect(x, y, SIZE - 1, SIZE - 1);
            g.setColor(color);
            g.fillRect(x + 1, y + 1, SIZE - 2, SIZE - 2);
        }

        @Override
        public int getIconWidth() {
            return SIZE;
        }

        @Override
        public int getIconHeight() {
            return SIZE;
        }
    }

    private static XTableCommand fillModel() {
        return new XTableCommand() {
            @Override
            public void execute(@NonNull XTable table) {
                DefaultTableModel result = (DefaultTableModel) table.getModel();
                result.addRow(new Object[]{Color.RED, "Red"});
                result.addRow(new Object[]{Color.GREEN, "Green"});
                result.addRow(new Object[]{Color.BLUE, "Blue"});
                result.addRow(new Object[]{Color.ORANGE, "Orange"});
                result.addRow(new Object[]{Color.BLACK, "Black"});
            }

            @Override
            public boolean isEnabled(@NonNull XTable table) {
                return table.getModel().getRowCount() == 0;
            }

            @Override
            public JCommand.@NonNull ActionAdapter toAction(@NonNull XTable table) {
                return super.toAction(table)
                        .withWeakTableModelListener(table.getModel());
            }
        };
    }

    private static XTableCommand clearModel() {
        return new XTableCommand() {
            @Override
            public void execute(@NonNull XTable table) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                while (model.getRowCount()>0) {
                    model.removeRow(0);
                }
            }

            @Override
            public boolean isEnabled(@NonNull XTable table) {
                return table.getModel().getRowCount() > 0;
            }

            @Override
            public JCommand.@NonNull ActionAdapter toAction(@NonNull XTable table) {
                return super.toAction(table)
                        .withWeakTableModelListener(table.getModel());
            }
        };
    }
}
