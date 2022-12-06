package nbbrd.desktop.favicon;

import lombok.NonNull;
import org.checkerframework.checker.index.qual.NonNegative;

import java.util.Optional;

@lombok.Value(staticConstructor = "of")
public class FaviconRef {

    @NonNull DomainName domain;

    @NonNegative int size;

    @NonNull Optional<FaviconRef> getParent() {
        return domain.getParent().map(parent -> new FaviconRef(parent, size));
    }

    @NonNull FaviconRef scale(double scale) {
        return scale == 1.0 ? this : new FaviconRef(domain, (int) (size * scale));
    }
}
