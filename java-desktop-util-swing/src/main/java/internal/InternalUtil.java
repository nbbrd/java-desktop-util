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

import java.awt.Font;
import java.lang.ref.SoftReference;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalUtil {

    @Nonnull
    public Font resizeByFactor(@Nonnull Font font, float factor) {
        return font.deriveFont(font.getSize2D() * factor);
    }

    @Nonnull
    public <X> Supplier<X> getLazyResource(@Nonnull Supplier<X> factory) {
        return new SharedLazyResource<>(factory);
    }

    @lombok.RequiredArgsConstructor
    private static final class SharedLazyResource<X> implements Supplier<X> {

        @lombok.NonNull
        private final Supplier<X> factory;

        private SoftReference<X> lazyResource;

        @Override
        public X get() {
            X result = lazyResource != null ? lazyResource.get() : null;
            if (result == null) {
                result = factory.get();
                lazyResource = new SoftReference<>(result);
            }
            return result;
        }
    }
}
