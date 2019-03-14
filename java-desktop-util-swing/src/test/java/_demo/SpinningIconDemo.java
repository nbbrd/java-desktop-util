/*
 * Copyright 2019 National Bank of Belgium
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

import ec.util.various.swing.BasicSwingLauncher;
import internal.InternalUtil;
import internal.SpinningIcon;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

/**
 *
 * @author Philippe Charles
 */
public class SpinningIconDemo extends JComponent {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .content(SpinningIconDemo::new)
                .size(200, 200)
                .launch();
    }

    private static final String FACTORY_PROPERTY = "factory";
    private static final String SPINNING_PROPERTY = "spinning";
    private static final String DISABLED_PROPERTY = "disabled";

    private final JComboBox<IconFactory> factories;
    private final JCheckBox spinningBox;
    private final JCheckBox disabledBox;
    private final JButton button;
    private final JLabel status;

    private IconFactory factory;
    private boolean spinning;
    private boolean disabled;

    public SpinningIconDemo() {
        this.factories = new JComboBox<>(IconFactory.values());
        this.spinningBox = new JCheckBox();
        this.disabledBox = new JCheckBox();
        this.button = new JButton();
        this.status = new JLabel();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new FlowLayout());
        factories.addItemListener(event -> firePropertyChange(FACTORY_PROPERTY, factory, factory = (IconFactory) factories.getSelectedItem()));
        header.add(factories);
        spinningBox.addChangeListener(event -> firePropertyChange(SPINNING_PROPERTY, spinning, spinning = spinningBox.isSelected()));
        header.add(spinningBox);
        disabledBox.addChangeListener(event -> firePropertyChange(DISABLED_PROPERTY, disabled, disabled = disabledBox.isSelected()));
        header.add(disabledBox);
        add(header);

        JPanel body = new JPanel(new FlowLayout());
        body.add(button);
        add(body);

        JPanel footer = new JPanel(new FlowLayout());
        footer.add(status);
        add(footer);

        addPropertyChangeListener(FACTORY_PROPERTY, this::onFactoryChange);
        addPropertyChangeListener(SPINNING_PROPERTY, this::onSpinningChange);
        addPropertyChangeListener(DISABLED_PROPERTY, this::onDisabledChange);

        factories.setSelectedIndex(-1);
        factories.setSelectedIndex(0);
    }

    private void onFactoryChange(PropertyChangeEvent event) {
        refreshIcon();
    }

    private void onSpinningChange(PropertyChangeEvent event) {
        refreshIcon();
    }

    private void onDisabledChange(PropertyChangeEvent event) {
        button.setEnabled(!disabled);
    }

    private void refreshIcon() {
        if (factory != null) {
            Icon x = factory.getIcon(50, Color.BLUE);
            button.setIcon(spinning ? SpinningIcon.of(x) : x);
            status.setText(x.getIconWidth() + "x" + x.getIconHeight());
        }
    }

    private enum IconFactory {
        DEBUG {
            @Override
            Icon getIcon(float size, Color color) {
                return new DebugIcon((int) size, (int) size, color);
            }
        }, MDI1 {
            @Override
            Icon getIcon(float size, Color color) {
                Ikon icon = MaterialDesign.MDI_AUTORENEW;
                return FontIcon.of(icon, (int) size, color);
            }
        }, MDI2 {
            @Override
            Icon getIcon(float size, Color color) {
                Ikon icon = MaterialDesign.MDI_AUTORENEW;
                return Ikons.of(icon, size, color);
            }
        }, ARROW {
            @Override
            Icon getIcon(float size, Color color) {
                char icon = InternalUtil.DOWNWARDS_DOUBLE_ARROW;
                Font font = new JLabel().getFont().deriveFont(size);
                return internal.FontIcon.of(icon, font, color, 0);
            }
        };

        abstract Icon getIcon(float size, Color color);
    }

    @lombok.RequiredArgsConstructor
    private static final class DebugIcon implements Icon {

        private final int width;
        private final int height;
        private final Color color;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x, y, width, height);
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }
}
