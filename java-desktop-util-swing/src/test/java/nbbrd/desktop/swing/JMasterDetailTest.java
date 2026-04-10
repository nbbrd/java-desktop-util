package nbbrd.desktop.swing;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class JMasterDetailTest {

    @Test
    public void testDefaults() {
        JMasterDetail x = new JMasterDetail();

        assertThat(x.getMasterNode()).isNotNull();
        assertThat(x.getDetailNode()).isNotNull();
        assertThat(x.getDetailSide()).isEqualTo(JMasterDetail.DetailSide.RIGHT);
        assertThat(x.isDetailVisible()).isTrue();
        assertThat(x.getDividerPosition()).isEqualTo(0.5);
    }

    @Test
    public void testSetMasterNode() {
        JMasterDetail x = new JMasterDetail();
        JList<?> master = new JList<>();

        x.setMasterNode(master);

        assertThat(x.getMasterNode()).isSameAs(master);
    }

    @Test
    public void testSetDetailNode() {
        JMasterDetail x = new JMasterDetail();
        JPanel detail = new JPanel();

        x.setDetailNode(detail);

        assertThat(x.getDetailNode()).isSameAs(detail);
    }

    @Test
    public void testSetDetailSideAllValues() {
        JMasterDetail x = new JMasterDetail();

        for (JMasterDetail.DetailSide side : JMasterDetail.DetailSide.values()) {
            x.setDetailSide(side);
            assertThat(x.getDetailSide()).isEqualTo(side);
        }
    }

    @Test
    public void testSetDetailVisible() {
        JMasterDetail x = new JMasterDetail();

        x.setDetailVisible(false);
        assertThat(x.isDetailVisible()).isFalse();

        x.setDetailVisible(true);
        assertThat(x.isDetailVisible()).isTrue();
    }

    @Test
    public void testSetDividerPosition() {
        JMasterDetail x = new JMasterDetail();

        x.setDividerPosition(0.3);
        assertThat(x.getDividerPosition()).isEqualTo(0.3);

        x.setDividerPosition(0.0);
        assertThat(x.getDividerPosition()).isEqualTo(0.0);

        x.setDividerPosition(1.0);
        assertThat(x.getDividerPosition()).isEqualTo(1.0);
    }

    @Test
    public void testSetDividerPositionOutOfRange() {
        JMasterDetail x = new JMasterDetail();

        assertThatIllegalArgumentException().isThrownBy(() -> x.setDividerPosition(-0.1));
        assertThatIllegalArgumentException().isThrownBy(() -> x.setDividerPosition(1.1));
    }

    @Test
    public void testNullMasterNodeThrows() {
        JMasterDetail x = new JMasterDetail();
        assertThatNullPointerException().isThrownBy(() -> x.setMasterNode(null));
    }

    @Test
    public void testNullDetailNodeThrows() {
        JMasterDetail x = new JMasterDetail();
        assertThatNullPointerException().isThrownBy(() -> x.setDetailNode(null));
    }

    @Test
    public void testNullDetailSideThrows() {
        JMasterDetail x = new JMasterDetail();
        assertThatNullPointerException().isThrownBy(() -> x.setDetailSide(null));
    }

    @Test
    public void testPropertyChangeEvents() {
        JMasterDetail x = new JMasterDetail();
        List<String> fired = new ArrayList<>();
        x.addPropertyChangeListener(evt -> fired.add(evt.getPropertyName()));

        x.setMasterNode(new JPanel());
        x.setDetailNode(new JPanel());
        x.setDetailSide(JMasterDetail.DetailSide.BOTTOM);
        x.setDetailVisible(false);
        x.setDividerPosition(0.7);

        assertThat(fired).contains(
                JMasterDetail.MASTER_NODE_PROPERTY,
                JMasterDetail.DETAIL_NODE_PROPERTY,
                JMasterDetail.DETAIL_SIDE_PROPERTY,
                JMasterDetail.DETAIL_VISIBLE_PROPERTY,
                JMasterDetail.DIVIDER_POSITION_PROPERTY
        );
    }
}

