/*
 * Copyright 2026 National Bank of Belgium
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

import ec.util.demo.ext.JDemoPane;
import ec.util.various.swing.BasicSwingLauncher;
import ec.util.various.swing.JCommand;
import nbbrd.desktop.swing.JRangeSlider;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * Demo for {@link JRangeSlider}.
 *
 * @author Philippe Charles
 */
public final class JRangeSliderDemo extends JPanel {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .content(() -> JDemoPane.of(new JRangeSliderDemo()))
                .title("RangeSlider Demo")
                .size(500, 300)
                .launch();
    }

    private final JRangeSlider horizontal;
    private final JRangeSlider vertical;

    public JRangeSliderDemo() {
        this.horizontal = new JRangeSlider();
        this.vertical = new JRangeSlider();

        vertical.setOrientation(SwingConstants.VERTICAL);

        // Value labels
        JLabel horizLabel = newValueLabel(horizontal);
        JLabel vertLabel = newValueLabel(vertical);
        horizontal.addChangeListener(e -> horizLabel.setText(formatRange(horizontal)));
        vertical.addChangeListener(e -> vertLabel.setText(formatRange(vertical)));

        JPopupMenu popup = createPopupMenu();
        horizontal.setComponentPopupMenu(popup);
        vertical.setComponentPopupMenu(popup);

        // Layout
        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel horizPanel = new JPanel(new BorderLayout(8, 4));
        horizPanel.add(new JLabel("Horizontal:"), BorderLayout.NORTH);
        horizPanel.add(horizontal, BorderLayout.CENTER);
        horizPanel.add(horizLabel, BorderLayout.SOUTH);

        JPanel vertPanel = new JPanel(new BorderLayout(4, 8));
        vertPanel.add(new JLabel("Vertical:"), BorderLayout.WEST);
        vertPanel.add(vertical, BorderLayout.CENTER);
        vertPanel.add(vertLabel, BorderLayout.SOUTH);

        center.add(horizPanel, BorderLayout.NORTH);
        center.add(vertPanel, BorderLayout.CENTER);

        JLabel hint = new JLabel("Right-click to toggle options", SwingConstants.CENTER);
        hint.setForeground(UIManager.getColor("Label.disabledForeground"));
        center.add(hint, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(center, BorderLayout.CENTER);
    }

    private static JLabel newValueLabel(JRangeSlider slider) {
        JLabel label = new JLabel(formatRange(slider));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.PLAIN));
        label.setForeground(UIManager.getColor("Label.disabledForeground"));
        return label;
    }

    private static String formatRange(JRangeSlider slider) {
        return "Low: " + slider.getLowValue() + "  —  High: " + slider.getHighValue();
    }

    //<editor-fold defaultstate="collapsed" desc="Popup menu">
    private JPopupMenu createPopupMenu() {
        JPopupMenu result = new JPopupMenu();

        result.add(new JCheckBoxMenuItem(toggle(horizontal, "paintTicks", vertical))).setText("Paint Ticks");
        result.add(new JCheckBoxMenuItem(toggle(horizontal, "snapToTicks", vertical))).setText("Snap to Ticks");

        result.addSeparator();
        JMenu minorMenu = new JMenu("Minor Tick Spacing");
        for (int spacing : new int[]{0, 5, 10}) {
            minorMenu.add(new JCheckBoxMenuItem(apply(horizontal, "minorTickSpacing", spacing, vertical)))
                    .setText(spacing == 0 ? "None" : String.valueOf(spacing));
        }
        result.add(minorMenu);

        JMenu majorMenu = new JMenu("Major Tick Spacing");
        for (int spacing : new int[]{10, 25, 50}) {
            majorMenu.add(new JCheckBoxMenuItem(apply(horizontal, "majorTickSpacing", spacing, vertical)))
                    .setText(String.valueOf(spacing));
        }
        result.add(majorMenu);

        return result;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Commands">
    /** Apply the same value to two sliders, tracked on the first. */
    private static Action apply(JRangeSlider primary, String propertyName, Object value, JRangeSlider secondary) {
        return new ApplyBoth<>(JRangeSlider.class, propertyName, value, secondary).toAction(primary);
    }

    /** Toggle a boolean property on two sliders, tracked on the first. */
    private static Action toggle(JRangeSlider primary, String propertyName, JRangeSlider secondary) {
        return new ToggleBoth<>(JRangeSlider.class, propertyName, secondary).toAction(primary);
    }

    private static PropertyDescriptor lookupProperty(Class<?> clazz, String propertyName) {
        try {
            for (PropertyDescriptor o : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                if (o.getName().equals(propertyName)) return o;
            }
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }
        throw new IllegalArgumentException(propertyName);
    }

    private static final class ApplyBoth<X extends Component> extends JCommand<X> {

        private final PropertyDescriptor property;
        private final Object value;
        private final Component secondary;

        ApplyBoth(Class<X> clazz, String propertyName, Object value, Component secondary) {
            this.property = lookupProperty(clazz, propertyName);
            this.value = value;
            this.secondary = secondary;
        }

        @Override
        public void execute(@NonNull X component) throws Exception {
            property.getWriteMethod().invoke(component, value);
            property.getWriteMethod().invoke(secondary, value);
        }

        @Override
        public boolean isSelected(@NonNull X component) {
            try {
                return Objects.equals(property.getReadMethod().invoke(component), value);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public JCommand.@NonNull ActionAdapter toAction(@NonNull X component) {
            return super.toAction(component).withWeakPropertyChangeListener(component, property.getName());
        }
    }

    private static final class ToggleBoth<X extends Component> extends JCommand<X> {

        private final PropertyDescriptor property;
        private final Component secondary;

        ToggleBoth(Class<X> clazz, String propertyName, Component secondary) {
            this.property = lookupProperty(clazz, propertyName);
            if (!property.getPropertyType().equals(boolean.class)) {
                throw new IllegalArgumentException("Not a boolean property: " + propertyName);
            }
            this.secondary = secondary;
        }

        @Override
        public void execute(@NonNull X component) throws Exception {
            boolean current = (Boolean) property.getReadMethod().invoke(component);
            property.getWriteMethod().invoke(component, !current);
            property.getWriteMethod().invoke(secondary, !current);
        }

        @Override
        public boolean isSelected(@NonNull X component) {
            try {
                return (Boolean) property.getReadMethod().invoke(component);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(property.getName(), ex);
            }
        }

        @Override
        public JCommand.@NonNull ActionAdapter toAction(@NonNull X component) {
            return super.toAction(component).withWeakPropertyChangeListener(component, property.getName());
        }
    }
    //</editor-fold>
}

