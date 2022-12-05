package nbbrd.desktop.favicon;

import lombok.NonNull;
import org.checkerframework.checker.index.qual.NonNegative;

@lombok.Value(staticConstructor = "of")
@lombok.With
public class FaviconRef {

    @NonNull DomainName domain;

    @NonNegative int size;
}
