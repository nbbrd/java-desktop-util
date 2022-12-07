package internal.desktop.favicon.spi;

import lombok.NonNull;
import nbbrd.desktop.favicon.FaviconRef;
import nbbrd.desktop.favicon.URLConnectionFactory;
import nbbrd.desktop.favicon.spi.FaviconSupplier;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_OK;

@ServiceProvider
public final class FaviconkitSupplier implements FaviconSupplier {

    @Override
    public @NonNull String getName() {
        return "Faviconkit";
    }

    @Override
    public int getRank() {
        return 700;
    }

    @Override
    public Image getFaviconOrNull(@NonNull FaviconRef ref, @NonNull URLConnectionFactory client) throws IOException {
        try (ImageConnection conn = ImageConnection.open(client, getFaviconRequest(ref))) {
            if (conn.getResponseCode() == HTTP_OK && !isDefaultFavicon(conn.getContentType())) {
                return conn.readImage();
            }
        }
        return NO_FAVICON;
    }

    private static URL getFaviconRequest(FaviconRef ref) throws MalformedURLException {
        return new URL("https://api.faviconkit.com/" + ref.getDomain() + "/" + ref.getSize());
    }

    private static boolean isDefaultFavicon(@Nullable String contentType) {
        return "image/svg+xml".equals(contentType);
    }
}
