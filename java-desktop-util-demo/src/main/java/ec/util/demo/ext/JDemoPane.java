package ec.util.demo.ext;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import ec.util.list.swing.JLists;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.ComponentUI;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

public final class JDemoPane extends JComponent {

    static {
        FlatLightLaf.installLafInfo();
        FlatDarkLaf.installLafInfo();
        FlatIntelliJLaf.installLafInfo();
    }

    public static JDemoPane of(JComponent component) {
        JDemoPane result = new JDemoPane();
        result.setMainComponent(component);
        return result;
    }

    private static final ComponentUIType<DemoPaneUI> UI_TYPE = new ComponentUIType<>(DemoPaneUI.class, BasicDemoPaneUI::new);

    public static final String MAIN_COMPONENT_PROPERTY = "mainComponent";

    public static final String LOOK_AND_FEEL_MODEL_PROPERTY = "lookAndFeelModel";

    private final ListDataListener listener;

    private JComponent mainComponent = newDefaultMainComponent();

    private ComboBoxModel<LookAndFeelInfo> lookAndFeelModel = newDefaultLookAndFeelModel();

    public JDemoPane() {
        this.listener = JLists.dataListenerOf(evt -> {
            LookAndFeelInfo selectedItem = (LookAndFeelInfo) ((ComboBoxModel<?>) evt.getSource()).getSelectedItem();
            LookAndFeels.setCurrent(Optional.ofNullable(selectedItem).orElseGet(LookAndFeels::getSystem));
            SwingUtilities.updateComponentTreeUI(SwingUtilities.getWindowAncestor(JDemoPane.this));
        });
        addPropertyChangeListener(LOOK_AND_FEEL_MODEL_PROPERTY, evt -> {
            ((ComboBoxModel<?>) evt.getOldValue()).removeListDataListener(listener);
            ((ComboBoxModel<?>) evt.getNewValue()).addListDataListener(listener);
        });
        lookAndFeelModel.addListDataListener(listener);
        updateUI();
    }

    @Override
    public String getUIClassID() {
        return UI_TYPE.getUIClassID();
    }

    @Override
    public void updateUI() {
        UI_TYPE.updateUI(this, this::setUI);
    }

    public @NonNull JComponent getMainComponent() {
        return mainComponent;
    }

    public void setMainComponent(@NonNull JComponent mainComponent) {
        firePropertyChange(MAIN_COMPONENT_PROPERTY, this.mainComponent, this.mainComponent = mainComponent);
    }

    public void resetMainComponent() {
        setMainComponent(newDefaultMainComponent());
    }

    public @NonNull ComboBoxModel<LookAndFeelInfo> getLookAndFeelModel() {
        return lookAndFeelModel;
    }

    public void setLookAndFeelModel(@NonNull ComboBoxModel<LookAndFeelInfo> lookAndFeelModel) {
        firePropertyChange(LOOK_AND_FEEL_MODEL_PROPERTY, this.lookAndFeelModel, this.lookAndFeelModel = lookAndFeelModel);
    }

    public void resetLookAndFeelModel() {
        setLookAndFeelModel(newDefaultLookAndFeelModel());
    }

    public static abstract class DemoPaneUI extends ComponentUI {
    }


    private static JComponent newDefaultMainComponent() {
        return new JPanel();
    }

    private static ComboBoxModel<LookAndFeelInfo> newDefaultLookAndFeelModel() {
        String lookAndFeelClassName = LookAndFeels.getCurrent().getClassName();
        ComboBoxModel<LookAndFeelInfo> result = new DefaultComboBoxModel<>(LookAndFeels.getInstalled());
        JLists.stream(result)
                .filter(element -> element.getClassName().equals(lookAndFeelClassName))
                .findFirst()
                .ifPresent(result::setSelectedItem);
        return result;
    }

    private static final class LookAndFeels {

        static @NonNull LookAndFeelInfo[] getInstalled() {
            return UIManager.getInstalledLookAndFeels();
        }

        static @NonNull LookAndFeelInfo getSystem() {
            String systemClassName = UIManager.getSystemLookAndFeelClassName();
            return Stream.of(UIManager.getInstalledLookAndFeels())
                    .filter(element -> element.getClassName().equals(systemClassName))
                    .findFirst()
                    .orElseThrow(NoSuchElementException::new);
        }

        static @NonNull LookAndFeelInfo getCurrent() {
            LookAndFeel result = UIManager.getLookAndFeel();
            return new LookAndFeelInfo(result.getName(), result.getClass().getName());
        }

        static void setCurrent(@NonNull LookAndFeelInfo info) {
            try {
                UIManager.setLookAndFeel(info.getClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                     UnsupportedLookAndFeelException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
