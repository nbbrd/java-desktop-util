package _test;

import lombok.NonNull;
import nbbrd.desktop.favicon.FaviconRef;
import nbbrd.desktop.favicon.URLConnectionFactory;
import nbbrd.desktop.favicon.spi.FaviconSupplier;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.*;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

@FunctionalInterface
public interface ImageFunction {

    @Nullable Image getFaviconOrNull(@NonNull FaviconRef ref, @NonNull URLConnectionFactory client) throws IOException;

    static @NonNull ImageFunction onNoFavicon() {
        return (ref, client) -> FaviconSupplier.NO_FAVICON;
    }

    static @NonNull ImageFunction onMap(@NonNull Map<FaviconRef, Image> map) {
        return (ref, client) -> map.get(ref);
    }

    static @NonNull ImageFunction onIOException(Supplier<? extends IOException> error) {
        return (ref, client) -> {
            throw error.get();
        };
    }

    static @NonNull ImageFunction onRuntimeException(Supplier<? extends RuntimeException> error) {
        return (ref, client) -> {
            throw error.get();
        };
    }

    default @NonNull FaviconSupplier asSupplier() {
        return new FaviconSupplier() {
            @Override
            public @NonNull String getName() {
                return "";
            }

            @Override
            public int getRank() {
                return 0;
            }

            @Override
            public @Nullable Image getFaviconOrNull(@NonNull FaviconRef ref, @NonNull URLConnectionFactory client) throws IOException {
                return ImageFunction.this.getFaviconOrNull(ref, client);
            }
        };
    }
}
