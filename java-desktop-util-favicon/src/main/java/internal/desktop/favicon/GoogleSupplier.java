package internal.desktop.favicon;

import lombok.NonNull;
import nbbrd.desktop.favicon.FaviconSupplier;
import nbbrd.desktop.favicon.DomainName;
import nbbrd.desktop.favicon.URLConnectionFactory;
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
public final class GoogleSupplier implements FaviconSupplier {

    @Override
    public @NonNull String getName() {
        return "Google";
    }

    @Override
    public int getRank() {
        return 200;
    }

    @Override
    public Image getFaviconOrNull(@NonNull DomainName domainName, @NonNull URLConnectionFactory client) throws IOException {
        URLConnection connection = client.openConnection(getFaviconRequest(domainName));
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

    private URL getFaviconRequest(DomainName domainName) throws MalformedURLException {
        return new URL("https://www.google.com/s2/favicons?domain=" + domainName);
    }

    private static boolean isDefaultFavicon(HttpURLConnection http) throws IOException {
        return http.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND;
    }
}
