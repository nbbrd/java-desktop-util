package _demo.ext;

import lombok.NonNull;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.util.function.Consumer;
import java.util.function.Supplier;

@lombok.AllArgsConstructor
public final class ComponentUIType<UI extends ComponentUI> {

    private final @NonNull Class<UI> type;
    private final @NonNull Supplier<? extends UI> defaultType;

    public String getUIClassID() {
        return type.getSimpleName();
    }

    public void updateUI(JComponent component, Consumer<UI> setter) {
        if (UIManager.get(getUIClassID()) != null) {
            setter.accept(type.cast(UIManager.getUI(component)));
        } else {
            setter.accept(defaultType.get());
        }
    }
}
