package ec.util.list.swing;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.formdev.flatlaf.ui.FlatTextFieldUI;
import com.formdev.flatlaf.util.UIScale;
import ec.util.various.swing.TextPrompt;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated("Changes the global Swing look-and-feel")
class JListSelectionLookAndFeelTest {

    @Test
    void inlineControlsFitInBothDirectionsAndAtLargerFontSizes() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "JListSelection enables Swing drag-and-drop");
        SwingUtilities.invokeAndWait(() -> {
            LookAndFeel original = UIManager.getLookAndFeel();
            try {
                for (LookAndFeel theme : new LookAndFeel[] {new FlatLightLaf(), new FlatDarkLaf()}) {
                    setLookAndFeel(theme);
                    JListSelection<String> c = new JListSelection<>();
                    c.getSourceModel().addElement("Alpha");
                    c.setSearchVisible(true);
                    c.setSearchText("alp");
                    JTextField field = find(c, JTextField.class).get(0);
                    Icon icon = searchIcon(field);
                    assertThat(icon).isInstanceOf(FlatSearchIcon.class);
                    for (int size : new int[] {13, 26}) {
                        field.setFont(field.getFont().deriveFont((float) size));
                        assertThat(icon.getIconWidth()).isEqualTo(UIScale.scale(16));
                        assertThat(icon.getIconHeight()).isEqualTo(icon.getIconWidth());
                        BufferedImage image = new BufferedImage(
                                icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g = image.createGraphics();
                        try {
                            icon.paintIcon(field, g, 0, 0);
                        } finally {
                            g.dispose();
                        }
                        boolean painted = false;
                        for (int x = 0; x < image.getWidth(); x++) {
                            for (int y = 0; y < image.getHeight(); y++) {
                                painted |= (image.getRGB(x, y) >>> 24) != 0;
                            }
                        }
                        assertThat(painted).isTrue();
                        for (ComponentOrientation direction : new ComponentOrientation[] {
                            ComponentOrientation.LEFT_TO_RIGHT, ComponentOrientation.RIGHT_TO_LEFT
                        }) {
                            c.applyComponentOrientation(direction);
                            c.setSize(500, 300);
                            layout(c);
                            JLabel status = find(
                                            (Component) field.getClientProperty("JTextField.trailingComponent"),
                                            JLabel.class)
                                    .get(0);
                            JButton clear = find(field, JButton.class).get(0);
                            assertThat(status.isEnabled()).isFalse();
                            assertThat(status.getInsets().left).isPositive();
                            assertThat(status.getInsets().right).isPositive();
                            assertThat(clear.getIcon()).isNotNull();
                            assertThat(status.getText()).isEqualTo("1/1");
                            assertThat(status.getWidth()).isGreaterThanOrEqualTo(status.getPreferredSize().width);
                            assertThat(status.getHeight()).isPositive();
                            assertThat(clear.getWidth()).isPositive();
                            assertThat(clear.getBounds().intersects(status.getBounds()))
                                    .isFalse();
                            java.awt.Rectangle statusBounds =
                                    SwingUtilities.convertRectangle(status.getParent(), status.getBounds(), field);
                            java.awt.Rectangle clearBounds =
                                    SwingUtilities.convertRectangle(clear.getParent(), clear.getBounds(), field);
                            assertThat(field.contains(statusBounds.x, statusBounds.y))
                                    .isTrue();
                            assertThat(field.contains(clearBounds.x, clearBounds.y))
                                    .isTrue();
                            if (direction.isLeftToRight()) {
                                assertThat(statusBounds.x).isGreaterThan(field.getWidth() / 2);
                                assertThat(clearBounds.x).isGreaterThanOrEqualTo(statusBounds.x + statusBounds.width);
                            } else {
                                assertThat(statusBounds.x).isLessThan(field.getWidth() / 2);
                                assertThat(clearBounds.x + clearBounds.width).isLessThanOrEqualTo(statusBounds.x);
                            }
                        }
                    }
                }
            } finally {
                setLookAndFeel(original);
            }
        });
    }

    @Test
    void searchAdaptsToThemeChangesWithoutLosingQueryOrDuplicatingControls() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "JListSelection enables Swing drag-and-drop");
        SwingUtilities.invokeAndWait(() -> {
            LookAndFeel original = UIManager.getLookAndFeel();
            try {
                for (LookAndFeel initial : new LookAndFeel[] {new MetalLookAndFeel(), new FlatLightLaf()}) {
                    setLookAndFeel(initial);
                    JListSelection<String> c = new JListSelection<>();
                    c.getSourceModel().addElement("Alpha");
                    c.getSourceModel().addElement("Beta");
                    c.setSearchVisible(true);
                    for (LookAndFeel theme : new LookAndFeel[] {
                        new FlatLightLaf(),
                        new FlatDarkLaf(),
                        new MetalLookAndFeel(),
                        new FlatLightLaf(),
                        new MetalLookAndFeel()
                    }) {
                        c.setSearchText("alp");
                        setLookAndFeel(theme);
                        SwingUtilities.updateComponentTreeUI(c);
                        c.setSize(900, 600);
                        layout(c);
                        JTextField field = find(c, JTextField.class).get(0);
                        boolean flatLaf = theme instanceof FlatLightLaf || theme instanceof FlatDarkLaf;
                        assertThat(c.getSearchText()).isEqualTo("alp");
                        assertThat(field.getText()).isEqualTo("alp");
                        assertThat(field.getAccessibleContext().getAccessibleName())
                                .isEqualTo("Search available items");
                        assertThat(field.getClientProperty("JTextField.placeholderText"))
                                .isEqualTo("Search available items...");
                        List<JButton> clearButtons = find(field.getParent(), JButton.class);
                        assertThat(clearButtons).hasSize(1);
                        JButton clear = clearButtons.get(0);
                        assertThat(clear.isVisible()).isTrue();
                        assertThat(clear.isEnabled()).isTrue();
                        JLabel status = find(c, JLabel.class).stream()
                                .filter(label -> "1/2".equals(label.getText()))
                                .findFirst()
                                .orElseThrow(AssertionError::new);
                        if (flatLaf) {
                            assertThat(clear.getParent()).isSameAs(status.getParent());
                            assertThat(status.getParent().getParent()).isSameAs(field);
                            assertThat(status.isEnabled()).isFalse();
                            assertThat(status.getInsets().right).isPositive();
                            assertThat(clear.getX()).isGreaterThanOrEqualTo(status.getX() + status.getWidth());
                            assertThat(searchIcon(field)).isInstanceOf(FlatSearchIcon.class);
                            assertThat(find(field, TextPrompt.class)).isEmpty();
                            assertThat(field.getBounds().width)
                                    .isEqualTo(field.getParent().getWidth());
                            assertThat(status.getParent().getX())
                                    .isGreaterThan(searchIcon(field).getIconWidth());
                            assertThat(clear.getBounds().intersects(status.getBounds()))
                                    .isFalse();
                        } else {
                            assertThat(clear.getParent()).isNotSameAs(field);
                            assertThat(field.getClientProperty("JTextField.leadingIcon"))
                                    .isNull();
                            assertThat(find(field, TextPrompt.class)).hasSize(1);
                            assertThat(find(field, TextPrompt.class).get(0).isVisible())
                                    .isFalse();
                        }
                        clear.doClick();
                        assertThat(c.getSearchText()).isEmpty();
                        assertThat(status.getText()).isEqualTo("2/2");
                        if (flatLaf) {
                            assertThat(clear.isVisible()).isFalse();
                        } else {
                            assertThat(clear.isEnabled()).isFalse();
                            assertThat(find(field, TextPrompt.class).get(0).isVisible())
                                    .isTrue();
                        }
                    }
                }
            } finally {
                setLookAndFeel(original);
            }
        });
    }

    private static void setLookAndFeel(LookAndFeel lookAndFeel) {
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            throw new AssertionError(ex);
        }
    }

    private static Icon searchIcon(JTextField field) {
        return (Icon) ((FlatTextFieldUI) field.getUI()).getStyleableValue(field, "leadingIcon");
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container) {
                layout((Container) child);
            }
        }
    }

    private static <T> List<T> find(Component c, Class<T> type) {
        List<T> result = new ArrayList<>();
        if (type.isInstance(c)) {
            result.add(type.cast(c));
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                result.addAll(find(child, type));
            }
        }
        return result;
    }
}
