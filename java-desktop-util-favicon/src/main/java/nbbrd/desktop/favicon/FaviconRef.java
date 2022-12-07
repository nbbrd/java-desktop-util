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

    @NonNull FaviconRef scale(double scale) throws IllegalArgumentException {
        if (scale <= 0) {
            throw new IllegalArgumentException("Invalid scale " + scale);
        }
        return scale == NO_SCALE ? this : new FaviconRef(domain, (int) (size * scale));
    }

    static final double NO_SCALE = 1.0;
}
