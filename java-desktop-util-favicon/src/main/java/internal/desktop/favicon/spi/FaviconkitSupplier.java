package internal.desktop.favicon.spi;

import lombok.NonNull;
import nbbrd.desktop.favicon.FaviconRef;
import nbbrd.desktop.favicon.URLConnectionFactory;
import nbbrd.desktop.favicon.spi.FaviconSupplier;
import nbbrd.service.ServiceProvider;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
        URLConnection connection = client.openConnection(getFaviconRequest(ref));
        if (!(connection instanceof HttpURLConnection)) {
            throw new IOException("Invalid url");
        }
        HttpURLConnection http = (HttpURLConnection) connection;
        try {
            if (isDefaultFavicon(http)) {
                return NO_FAVICON;
            }
            try (InputStream stream = http.getInputStream()) {
                return ImageIO.read(stream);
            }
        } finally {
            http.disconnect();
        }
    }

    private static URL getFaviconRequest(FaviconRef ref) throws MalformedURLException {
        return new URL("https://api.faviconkit.com/" + ref.getDomain() + "/" + ref.getSize());
    }

    private static boolean isDefaultFavicon(HttpURLConnection http) throws IllegalArgumentException {
        return "image/svg+xml".equals(http.getContentType());
    }
}
