package internal.desktop.favicon.spi;

import lombok.NonNull;
import nbbrd.desktop.favicon.FaviconRef;
import nbbrd.desktop.favicon.URLConnectionFactory;
import nbbrd.desktop.favicon.spi.FaviconSupplier;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_OK;

@ServiceProvider
public final class IconHorseSupplier implements FaviconSupplier {

    @Override
    public @NonNull String getName() {
        return "IconHorse";
    }

    @Override
    public int getRank() {
        return 500;
    }

    @Override
    public Image getFaviconOrNull(@NonNull FaviconRef ref, @NonNull URLConnectionFactory client) throws IOException {
        try (ImageConnection conn = ImageConnection.open(client, getFaviconRequest(ref))) {
            if (conn.getResponseCode() == HTTP_OK) {
                BufferedImage result = conn.readImage();
                if (!isDefaultFavicon(conn.getContentType(), result)) {
                    return result;
                }
            }
        }
        return NO_FAVICON;
    }

    private static URL getFaviconRequest(FaviconRef ref) throws MalformedURLException {
        return new URL("https://icon.horse/icon/" + ref.getDomain());
    }

    private static boolean isDefaultFavicon(@Nullable String contentType, @Nullable BufferedImage image) {
        return "image/png".equals(contentType)
                && image != null
                && image.getHeight(null) == 512
                && image.getWidth(null) == 512
                && image.getRGB(0, 0) == -14735049;
    }
}
