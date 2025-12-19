package ec.util.desktop.impl;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

@lombok.Builder
@lombok.AllArgsConstructor
final class MapSystem extends ZSystem {

    @lombok.Singular
    private final @NonNull Map<String, String> properties;

    @Override
    public @Nullable String getProperty(@NonNull String key) {
        return properties.get(key);
    }

    @Override
    public @NonNull Process exec(@NonNull String... cmdarray) throws IOException {
        throw new IOException();
    }
}
