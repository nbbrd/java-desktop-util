package nbbrd.desktop.favicon;

import lombok.AccessLevel;
import lombok.NonNull;

import java.net.URL;
import java.util.Optional;

@lombok.EqualsAndHashCode
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainName implements CharSequence {

    public static @NonNull DomainName of(@NonNull URL url) {
        return new DomainName(url.getHost());
    }

    @lombok.experimental.Delegate(types = CharSequence.class)
    private final @NonNull String value;

    @Override
    public String toString() {
        return value;
    }

    public @NonNull Optional<DomainName> getParent() {
        // TODO
        return Optional.empty();
    }
}
