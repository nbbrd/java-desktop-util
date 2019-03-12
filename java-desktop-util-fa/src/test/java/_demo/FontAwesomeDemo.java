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
package _demo;

import ec.util.list.swing.JLists;
import ec.util.various.swing.BasicSwingLauncher;
import ec.util.various.swing.FontAwesome;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author Philippe Charles
 */
public final class FontAwesomeDemo extends JComponent {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .content(FontAwesomeDemo::new)
                .title("Font Awesome Demo")
                .size(300, 200)
                .icons(() -> FontAwesome.FA_FONT.getImages(Color.BLUE, 16f, 32f, 64f))
                .launch();
    }

    private static final String SELECTED_ICON_PROPERTY = "selectedIcon";
    private static final String ANGLE_PROPERTY = "angle";
    private static final String SPINNING_PROPERTY = "spinning";

    private final JComboBox<FontAwesome> master;
    private final JLabel detail;
    private final JSlider angleSlider;
    private final JCheckBox spinningCheckBox;

    private FontAwesome selectedIcon;
    private int angle;
    private boolean spinning;

    public FontAwesomeDemo() {
        this.master = new JComboBox(FontAwesome.values());
        this.detail = new JLabel();
        this.angleSlider = new JSlider(JSlider.HORIZONTAL, 0, 360, 0);
        this.spinningCheckBox = new JCheckBox();
        initComponents();
    }

    private void initComponents() {
        setLayout(new FlowLayout());

        master.setRenderer(JLists.cellRendererOf(FontAwesomeDemo::renderFontAwesome));
        master.addItemListener(this::onMasterChange);
        add(master);

        detail.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(detail);

        angleSlider.addChangeListener(this::onAngleChange);
        add(angleSlider);

        spinningCheckBox.addChangeListener(this::onSpinningChange);
        add(spinningCheckBox);

        addPropertyChangeListener(SELECTED_ICON_PROPERTY, this::onSelectedIconChange);
        addPropertyChangeListener(ANGLE_PROPERTY, this::onAngleChange);
        addPropertyChangeListener(SPINNING_PROPERTY, this::onSpinningChange);

        master.setSelectedItem(FontAwesome.FA_DESKTOP);
    }

    private void onMasterChange(ItemEvent event) {
        firePropertyChange(SELECTED_ICON_PROPERTY, selectedIcon, this.selectedIcon = (FontAwesome) event.getItem());
    }

    private void onAngleChange(ChangeEvent event) {
        if (!angleSlider.getValueIsAdjusting()) {
            firePropertyChange(ANGLE_PROPERTY, angle, this.angle = angleSlider.getValue());
        }
    }

    private void onSpinningChange(ChangeEvent event) {
        firePropertyChange(SPINNING_PROPERTY, spinning, this.spinning = spinningCheckBox.isSelected());
    }

    private void onSelectedIconChange(PropertyChangeEvent event) {
        refreshIcon();
    }

    private void onAngleChange(PropertyChangeEvent event) {
        refreshIcon();
    }

    private void onSpinningChange(PropertyChangeEvent event) {
        refreshIcon();
    }

    private void refreshIcon() {
        detail.setIcon(spinning
                ? selectedIcon.getSpinningIcon(detail, Color.GREEN.darker(), 100)
                : selectedIcon.getIcon(Color.GREEN.darker(), 100, angle));
    }

    private static void renderFontAwesome(JLabel label, Object value) {
        label.setIcon(((FontAwesome) value).getIcon(label.getForeground(), 16));
    }
}
