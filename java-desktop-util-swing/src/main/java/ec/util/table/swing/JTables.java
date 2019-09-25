/*
 * Copyright 2018 National Bank of Belgium
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
package ec.util.table.swing;

import java.awt.Component;
import java.util.function.BiConsumer;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class JTables {

    @NonNull
    public <E> TableCellRenderer cellRendererOf(@NonNull BiConsumer<JLabel, E> consumer) {
        return new LabelTableCellRenderer<>(consumer);
    }

    /**
     * Set the width of the columns as percentages.
     *
     * @param table the {@link JTable} whose columns will be set
     * @param percentages the widths of the columns as percentages; note: this
     * method does NOT verify that all percentages add up to 100% and for the
     * columns to appear properly, it is recommended that the widths for ALL
     * columns be specified
     *
     * @see
     * http://kahdev.wordpress.com/2011/10/30/java-specifying-the-column-widths-of-a-jtable-as-percentages/
     */
    public void setWidthAsPercentages(@NonNull JTable table, @NonNull double... percentages) {
        final double factor = 10000;
        TableColumnModel model = table.getColumnModel();
        for (int columnIndex = 0; columnIndex < percentages.length; columnIndex++) {
            TableColumn column = model.getColumn(columnIndex);
            column.setPreferredWidth((int) (percentages[columnIndex] * factor));
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class LabelTableCellRenderer<T> implements TableCellRenderer {

        @lombok.NonNull
        private final BiConsumer<JLabel, T> consumer;

        private final DefaultTableCellRenderer delegate = new DefaultTableCellRenderer();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            consumer.accept(delegate, (T) value);
            return delegate;
        }
    }
}
