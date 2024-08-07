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
package ec.util.demo;

import ec.util.grid.swing.XTable;
import ec.util.table.swing.JTables;
import ec.util.various.swing.JCommand;
import lombok.NonNull;

import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Philippe Charles
 */
abstract class XTableCommand extends JCommand<XTable> {

    @Override
    public @NonNull ActionAdapter toAction(@NonNull XTable table) {
        return super.toAction(table)
                .withWeakPropertyChangeListener(table)
                .withWeakListSelectionListener(table.getSelectionModel());
    }

    public static XTableCommand applyDefaultRenderer(final Class<?> columnClass, final TableCellRenderer renderer) {
        return new XTableCommand() {
            @Override
            public void execute(@NonNull XTable table) {
                table.setDefaultRenderer(columnClass, renderer);
                table.repaint();
            }

            @Override
            public boolean isEnabled(@NonNull XTable component) {
                return component.getModel().getRowCount() > 0;
            }

            @Override
            public boolean isSelected(@NonNull XTable table) {
                return table.getDefaultRenderer(columnClass).equals(renderer);
            }
        };
    }

    public static XTableCommand applyNoDataRenderer(final XTable.NoDataRenderer renderer) {
        return new XTableCommand() {
            @Override
            public void execute(@NonNull XTable table) {
                table.setNoDataRenderer(renderer);
                table.repaint();
            }

            @Override
            public boolean isSelected(@NonNull XTable component) {
                return component.getNoDataRenderer().equals(renderer);
            }
        };
    }

    public static XTableCommand applyNoDataMessage(String message) {
        return applyNoDataRenderer(new XTable.DefaultNoDataRenderer(message));
    }

    public static XTableCommand applyColumnWidthAsPercentages(final double... percentages) {
        return new XTableCommand() {
            @Override
            public void execute(@NonNull XTable component) {
                JTables.setWidthAsPercentages(component, percentages);
            }
        };
    }
}
