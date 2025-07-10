package ec.util.grid.swing;

import org.junit.jupiter.api.Test;

import javax.swing.table.DefaultTableCellRenderer;

import static org.assertj.core.api.Assertions.assertThat;

public class JGridTest {

    @Test
    public void testRenderers() {
        JGrid x = new JGrid();

        DefaultTableCellRenderer corner = new DefaultTableCellRenderer();
        DefaultTableCellRenderer column = new DefaultTableCellRenderer();
        DefaultTableCellRenderer row = new DefaultTableCellRenderer();

        assertThat(x.getCornerRenderer()).isNotEqualTo(corner);
        assertThat(x.getColumnRenderer()).isNotEqualTo(column);
        assertThat(x.getRowRenderer()).isNotEqualTo(row);

        x.setCornerRenderer(corner);
        x.setColumnRenderer(column);
        x.setRowRenderer(row);

        assertThat(x.getCornerRenderer()).isEqualTo(corner);
        assertThat(x.getColumnRenderer()).isEqualTo(column);
        assertThat(x.getRowRenderer()).isEqualTo(row);
    }
}
