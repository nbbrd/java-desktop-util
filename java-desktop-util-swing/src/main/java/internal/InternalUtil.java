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
package internal;

import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalUtil {

    @NonNull
    public Font resizeByFactor(@NonNull Font font, float factor) {
        return font.deriveFont(font.getSize2D() * factor);
    }

    @NonNull
    public <X> Supplier<X> getLazyResource(@NonNull Supplier<X> factory) {
        return new LazyResource<>(withUpdateUI(factory));
    }

    @NonNull
    private <X> Supplier<X> withUpdateUI(@NonNull Supplier<X> factory) {
        return () -> {
            X result = factory.get();
            if (result instanceof JComponent) {
                UIManager.addPropertyChangeListener(evt -> ((JComponent) result).updateUI());
            }
            return result;
        };
    }

    @lombok.RequiredArgsConstructor
    private static final class LazyResource<X> implements Supplier<X> {

        private final @NonNull Supplier<X> factory;

        private final AtomicReference<X> resource = new AtomicReference<>();

        @Override
        public X get() {
            X result = resource.get();
            if (result == null) {
                result = factory.get();
                resource.set(result);
            }
            return result;
        }
    }

    public final Supplier<Icon> MISSING_ICON = getLazyResource(InternalUtil::createFallbackIcon);

    private static Icon createFallbackIcon() {
        return FontIcon.of('?', new JLabel().getFont(), null, 0);
    }
}
