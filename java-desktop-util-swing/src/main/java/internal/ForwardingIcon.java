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

import java.awt.Component;
import java.awt.Graphics;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.swing.Icon;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
public final class ForwardingIcon implements Icon {

    @lombok.NonNull
    private final Supplier<Icon> delegate;

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        delegate.get().paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        return delegate.get().getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return delegate.get().getIconHeight();
    }

    public static ForwardingIcon of(BooleanSupplier condition, Icon first, Icon second) {
        return ForwardingIcon.of(() -> condition.getAsBoolean() ? first : second);
    }
}
