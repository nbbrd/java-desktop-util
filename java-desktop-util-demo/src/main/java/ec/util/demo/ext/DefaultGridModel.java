package ec.util.demo.ext;

import ec.util.grid.swing.AbstractGridModel;
import lombok.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@lombok.Builder
public final class DefaultGridModel<R, C> extends AbstractGridModel {

    public static <R, C> @NonNull Builder<R, C> builder(@NonNull Class<R> rowType, @NonNull Class<C> columnType) {
        return new Builder<R, C>();
    }

    @lombok.Singular
    @NonNull
    final List<R> rows;

    @lombok.Builder.Default
    final Function<? super R, String> rowName = Objects::toString;

    @lombok.Singular
    @NonNull
    final List<C> columns;

    @lombok.Builder.Default
    final Function<? super C, String> columnName = Objects::toString;

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return new DefaultGridCell<>(rows.get(rowIndex), columns.get(columnIndex));
    }

    @Override
    public String getRowName(int rowIndex) {
        return rowName.apply(rows.get(rowIndex));
    }

    @Override
    public String getColumnName(int column) {
        return columnName.apply(columns.get(column));
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return DefaultGridCell.class;
    }
}
