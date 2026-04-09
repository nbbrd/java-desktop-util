package ec.util.various.swing;

import org.junit.jupiter.api.Test;

import javax.swing.SwingConstants;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class JRangeSliderTest {

    @Test
    public void testDefaults() {
        JRangeSlider x = new JRangeSlider();
        assertThat(x.getMinimum()).isEqualTo(0);
        assertThat(x.getMaximum()).isEqualTo(100);
        assertThat(x.getLowValue()).isEqualTo(25);
        assertThat(x.getHighValue()).isEqualTo(75);
        assertThat(x.getOrientation()).isEqualTo(SwingConstants.HORIZONTAL);
        assertThat(x.isPaintTicks()).isFalse();
        assertThat(x.getMajorTickSpacing()).isEqualTo(25);
        assertThat(x.getMinorTickSpacing()).isEqualTo(5);
        assertThat(x.isSnapToTicks()).isFalse();
    }

    @Test
    public void testSetLowValue() {
        JRangeSlider x = new JRangeSlider();
        x.setLowValue(30);
        assertThat(x.getLowValue()).isEqualTo(30);
    }

    @Test
    public void testSetHighValue() {
        JRangeSlider x = new JRangeSlider();
        x.setHighValue(80);
        assertThat(x.getHighValue()).isEqualTo(80);
    }

    @Test
    public void testLowValueClamped() {
        JRangeSlider x = new JRangeSlider();
        // Cannot exceed highValue
        x.setLowValue(90);
        assertThat(x.getLowValue()).isEqualTo(x.getHighValue());
        // Cannot go below minimum
        x.setLowValue(-10);
        assertThat(x.getLowValue()).isEqualTo(0);
    }

    @Test
    public void testHighValueClamped() {
        JRangeSlider x = new JRangeSlider();
        // Cannot go below lowValue
        x.setHighValue(10);
        assertThat(x.getHighValue()).isEqualTo(x.getLowValue());
        // Cannot exceed maximum
        x.setHighValue(200);
        assertThat(x.getHighValue()).isEqualTo(100);
    }

    @Test
    public void testSetOrientation() {
        JRangeSlider x = new JRangeSlider();
        x.setOrientation(SwingConstants.VERTICAL);
        assertThat(x.getOrientation()).isEqualTo(SwingConstants.VERTICAL);
        x.setOrientation(SwingConstants.HORIZONTAL);
        assertThat(x.getOrientation()).isEqualTo(SwingConstants.HORIZONTAL);
    }

    @Test
    public void testInvalidOrientation() {
        JRangeSlider x = new JRangeSlider();
        assertThatIllegalArgumentException().isThrownBy(() -> x.setOrientation(99));
    }

    @Test
    public void testSetMinimumClampsValues() {
        JRangeSlider x = new JRangeSlider();
        x.setLowValue(10);
        x.setMinimum(20); // pushes lowValue up
        assertThat(x.getLowValue()).isGreaterThanOrEqualTo(20);
    }

    @Test
    public void testSetMaximumClampsValues() {
        JRangeSlider x = new JRangeSlider();
        x.setHighValue(90);
        x.setMaximum(50); // pushes highValue down
        assertThat(x.getHighValue()).isLessThanOrEqualTo(50);
    }

    @Test
    public void testPropertyChangeEvents() {
        JRangeSlider x = new JRangeSlider();
        List<String> fired = new ArrayList<>();
        x.addPropertyChangeListener(evt -> fired.add(evt.getPropertyName()));

        x.setMinimum(5);
        x.setMaximum(95);
        x.setLowValue(30);
        x.setHighValue(70);
        x.setOrientation(SwingConstants.VERTICAL);
        x.setPaintTicks(true);
        x.setMajorTickSpacing(10);
        x.setMinorTickSpacing(2);
        x.setSnapToTicks(true);

        assertThat(fired).contains(
                JRangeSlider.MINIMUM_PROPERTY,
                JRangeSlider.MAXIMUM_PROPERTY,
                JRangeSlider.LOW_VALUE_PROPERTY,
                JRangeSlider.HIGH_VALUE_PROPERTY,
                JRangeSlider.ORIENTATION_PROPERTY,
                JRangeSlider.PAINT_TICKS_PROPERTY,
                JRangeSlider.MAJOR_TICK_SPACING_PROPERTY,
                JRangeSlider.MINOR_TICK_SPACING_PROPERTY,
                JRangeSlider.SNAP_TO_TICKS_PROPERTY
        );
    }

    @Test
    public void testLowCanEqualHigh() {
        JRangeSlider x = new JRangeSlider();
        x.setLowValue(50);
        x.setHighValue(50);
        assertThat(x.getLowValue()).isEqualTo(50);
        assertThat(x.getHighValue()).isEqualTo(50);
    }

    @Test
    public void testPreferredSizeChangesWithOrientation() {
        JRangeSlider x = new JRangeSlider();
        x.setOrientation(SwingConstants.HORIZONTAL);
        java.awt.Dimension horiz = x.getPreferredSize();

        x.setOrientation(SwingConstants.VERTICAL);
        java.awt.Dimension vert = x.getPreferredSize();

        assertThat(horiz.width).isGreaterThan(horiz.height);
        assertThat(vert.height).isGreaterThan(vert.width);
    }
}

