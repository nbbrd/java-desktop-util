package _demo.ext;

import ec.util.list.swing.JLists;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.beans.PropertyChangeEvent;

public final class BasicDemoPaneUI extends JDemoPane.DemoPaneUI {

    @Override
    public void installUI(JComponent c) {
        JDemoPane target = (JDemoPane) c;
        target.setLayout(new BorderLayout());
        target.add(createLafChooser(target.getLookAndFeelModel()), BorderLayout.NORTH);
        target.add(target.getMainComponent(), BorderLayout.CENTER);
        target.addPropertyChangeListener(JDemoPane.LOOK_AND_FEEL_MODEL_PROPERTY, BasicDemoPaneUI::onLookAndFeelModelChange);
        target.addPropertyChangeListener(JDemoPane.MAIN_COMPONENT_PROPERTY, BasicDemoPaneUI::onMainComponentChange);
    }

    @Override
    public void uninstallUI(JComponent c) {
        JDemoPane target = (JDemoPane) c;
        target.removePropertyChangeListener(JDemoPane.MAIN_COMPONENT_PROPERTY, BasicDemoPaneUI::onMainComponentChange);
        target.removePropertyChangeListener(JDemoPane.LOOK_AND_FEEL_MODEL_PROPERTY, BasicDemoPaneUI::onLookAndFeelModelChange);
        target.removeAll();
        target.setLayout(null);
    }

    private static JComboBox<LookAndFeelInfo> createLafChooser(ComboBoxModel<LookAndFeelInfo> model) {
        JComboBox<LookAndFeelInfo> result = new JComboBox<>(model);
        result.setRenderer(JLists.cellRendererOf((label, value) -> label.setText(value.getName())));
        return result;
    }

    private static void onLookAndFeelModelChange(PropertyChangeEvent evt) {
        JDemoPane source = (JDemoPane) evt.getSource();
        JComboBox<LookAndFeelInfo> lafChooser = (JComboBox<LookAndFeelInfo>) source.getComponent(0);
        lafChooser.setModel((ComboBoxModel<LookAndFeelInfo>) evt.getNewValue());
    }

    private static void onMainComponentChange(PropertyChangeEvent evt) {
        JDemoPane source = (JDemoPane) evt.getSource();
        source.add((Component) evt.getNewValue(), BorderLayout.CENTER);
    }
}
