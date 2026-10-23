package nbbrd.desktop.swing;

import static org.assertj.core.api.Assertions.*;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import org.junit.jupiter.api.Test;

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
    public void testSetDetailSideKeepsBothNodes() {
        JMasterDetail x = new JMasterDetail();
        JPanel master = new JPanel();
        JPanel detail = new JPanel();
        x.setMasterNode(master);
        x.setDetailNode(detail);

        for (JMasterDetail.DetailSide from : JMasterDetail.DetailSide.values()) {
            for (JMasterDetail.DetailSide to : JMasterDetail.DetailSide.values()) {
                x.setDetailSide(from);
                x.setDetailSide(to);
                JSplitPane splitPane = (JSplitPane) x.getComponent(0);
                boolean detailFirst = to == JMasterDetail.DetailSide.TOP || to == JMasterDetail.DetailSide.LEFT;
                assertThat(splitPane.getLeftComponent()).isSameAs(detailFirst ? detail : master);
                assertThat(splitPane.getRightComponent()).isSameAs(detailFirst ? master : detail);
            }
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
    public void testDividerPositionAppliedOnLayout() {
        for (JMasterDetail.DetailSide side : JMasterDetail.DetailSide.values()) {
            JMasterDetail x = new JMasterDetail();
            x.setDetailSide(side);
            x.setDividerPosition(0.3);

            x.setSize(400, 400);
            x.doLayout();
            JSplitPane splitPane = (JSplitPane) x.getComponent(0);
            splitPane.doLayout();

            int total = 400 - splitPane.getDividerSize();
            assertThat(x.getDividerPosition()).isEqualTo(0.3);
            assertThat(splitPane.getDividerLocation()).isEqualTo((int) (total * 0.3));
        }
    }

    @Test
    public void testDetailHiddenShowsOnlyMaster() {
        JMasterDetail x = new JMasterDetail();
        JPanel master = new JPanel();
        JPanel detail = new JPanel();
        x.setMasterNode(master);
        x.setDetailNode(detail);

        x.setDetailVisible(false);
        assertThat(x.getComponents()).containsExactly(master);
        assertThat(detail.getParent()).isNull();

        x.setDetailVisible(true);
        assertThat(x.getComponentCount()).isEqualTo(1);
        JSplitPane splitPane = (JSplitPane) x.getComponent(0);
        assertThat(splitPane.getLeftComponent()).isSameAs(master);
        assertThat(splitPane.getRightComponent()).isSameAs(detail);
    }

    @Test
    public void testReplaceNodesWhileVisible() {
        JMasterDetail x = new JMasterDetail();
        JPanel oldMaster = new JPanel();
        JPanel oldDetail = new JPanel();
        x.setMasterNode(oldMaster);
        x.setDetailNode(oldDetail);

        JPanel newMaster = new JPanel();
        JPanel newDetail = new JPanel();
        x.setMasterNode(newMaster);
        x.setDetailNode(newDetail);

        JSplitPane splitPane = (JSplitPane) x.getComponent(0);
        assertThat(splitPane.getLeftComponent()).isSameAs(newMaster);
        assertThat(splitPane.getRightComponent()).isSameAs(newDetail);
        assertThat(oldMaster.getParent()).isNull();
        assertThat(oldDetail.getParent()).isNull();
    }

    @Test
    public void testReplaceNodesWhileHidden() {
        JMasterDetail x = new JMasterDetail();
        JPanel oldDetail = new JPanel();
        x.setDetailNode(oldDetail);
        x.setDetailVisible(false);

        JPanel master = new JPanel();
        JPanel detail = new JPanel();
        x.setMasterNode(master);
        x.setDetailNode(detail);
        assertThat(x.getComponents()).containsExactly(master);
        assertThat(oldDetail.getParent()).isNull();
        assertThat(detail.getParent()).isNull();

        x.setDetailVisible(true);
        JSplitPane splitPane = (JSplitPane) x.getComponent(0);
        assertThat(splitPane.getLeftComponent()).isSameAs(master);
        assertThat(splitPane.getRightComponent()).isSameAs(detail);
    }

    @Test
    public void testUserDividerDragUpdatesPosition() {
        JMasterDetail x = new JMasterDetail();
        layout(x, 400, 400);
        JSplitPane splitPane = (JSplitPane) x.getComponent(0);
        List<PropertyChangeEvent> events = new ArrayList<>();
        x.addPropertyChangeListener(JMasterDetail.DIVIDER_POSITION_PROPERTY, events::add);

        splitPane.setDividerLocation(100);

        int total = 400 - splitPane.getDividerSize();
        assertThat(x.getDividerPosition()).isCloseTo(100.0 / total, within(1e-9));
        assertThat(events).hasSize(1);
    }

    @Test
    public void testLayoutDoesNotChangePosition() {
        JMasterDetail x = new JMasterDetail();
        x.setDividerPosition(0.3);
        List<PropertyChangeEvent> events = new ArrayList<>();
        x.addPropertyChangeListener(JMasterDetail.DIVIDER_POSITION_PROPERTY, events::add);

        layout(x, 400, 400);
        layout(x, 800, 600);
        layout(x, 300, 200);

        assertThat(x.getDividerPosition()).isEqualTo(0.3);
        assertThat(events).isEmpty();
    }

    @Test
    public void testDividerPositionAppliedWhenShownAgain() {
        JMasterDetail x = new JMasterDetail();
        layout(x, 400, 400);

        x.setDetailVisible(false);
        x.setDividerPosition(0.3);
        x.setDetailVisible(true);
        layout(x, 400, 400);

        JSplitPane splitPane = (JSplitPane) x.getComponent(0);
        int total = 400 - splitPane.getDividerSize();
        assertThat(x.getDividerPosition()).isEqualTo(0.3);
        assertThat(splitPane.getDividerLocation()).isEqualTo((int) (total * 0.3));
    }

    @Test
    public void testDividerPositionKeptOnSideChange() {
        JMasterDetail x = new JMasterDetail();
        x.setDividerPosition(0.3);
        layout(x, 400, 400);

        for (JMasterDetail.DetailSide side : JMasterDetail.DetailSide.values()) {
            x.setDetailSide(side);
            layout(x, 400, 400);

            JSplitPane splitPane = (JSplitPane) x.getComponent(0);
            int total = 400 - splitPane.getDividerSize();
            assertThat(x.getDividerPosition()).isEqualTo(0.3);
            assertThat(splitPane.getDividerLocation()).isEqualTo((int) (total * 0.3));
        }
    }

    @Test
    public void testResizeKeepsProportion() {
        JMasterDetail x = new JMasterDetail();
        x.setDividerPosition(0.3);
        layout(x, 400, 400);
        // BasicSplitPaneUI distributes extra space according to resizeWeight only once painted
        BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            x.paint(g);
        } finally {
            g.dispose();
        }

        layout(x, 800, 400);

        JSplitPane splitPane = (JSplitPane) x.getComponent(0);
        int total = 800 - splitPane.getDividerSize();
        assertThat(splitPane.getDividerLocation()).isCloseTo((int) (total * 0.3), within(2));
    }

    private static void layout(JMasterDetail x, int width, int height) {
        x.setSize(width, height);
        x.doLayout();
        Component c = x.getComponent(0);
        if (c instanceof JSplitPane) {
            c.doLayout();
        }
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
    public void testSameNodeForMasterAndDetailThrows() {
        JMasterDetail x = new JMasterDetail();
        assertThatIllegalArgumentException().isThrownBy(() -> x.setMasterNode(x.getDetailNode()));
        assertThatIllegalArgumentException().isThrownBy(() -> x.setDetailNode(x.getMasterNode()));
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

        assertThat(fired)
                .contains(
                        JMasterDetail.MASTER_NODE_PROPERTY,
                        JMasterDetail.DETAIL_NODE_PROPERTY,
                        JMasterDetail.DETAIL_SIDE_PROPERTY,
                        JMasterDetail.DETAIL_VISIBLE_PROPERTY,
                        JMasterDetail.DIVIDER_POSITION_PROPERTY);
    }
}
