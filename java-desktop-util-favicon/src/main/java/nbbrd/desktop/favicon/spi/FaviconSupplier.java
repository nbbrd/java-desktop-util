package nbbrd.desktop.favicon.spi;

import lombok.NonNull;
import nbbrd.desktop.favicon.FaviconRef;
import nbbrd.desktop.favicon.URLConnectionFactory;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceSorter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.*;
import java.io.IOException;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE
)
public interface FaviconSupplier {

    @NonNull String getName();

    @ServiceSorter(reverse = true)
    int getRank();

    @Nullable Image getFaviconOrNull(@NonNull FaviconRef ref, @NonNull URLConnectionFactory client) throws IOException;

    Image NO_FAVICON = null;
}
