package nbbrd.desktop.favicon;

import lombok.NonNull;
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

    @Nullable Image getFaviconOrNull(@NonNull DomainName domainName, @NonNull URLConnectionFactory client) throws IOException;

    Image NO_FAVICON = null;
}
