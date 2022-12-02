package internal.desktop.favicon;

import lombok.NonNull;
import nbbrd.desktop.favicon.FaviconSupplier;
import nbbrd.desktop.favicon.DomainName;
import nbbrd.desktop.favicon.URLConnectionFactory;
import nbbrd.service.ServiceProvider;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
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
        return 100;
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
                return resize(ImageIO.read(stream));
            }
        } finally {
            http.disconnect();
        }
    }

    private static URL getFaviconRequest(DomainName domainName) throws MalformedURLException {
        return new URL("https://api.faviconkit.com/" + domainName + "/57"); //16
    }

    private static boolean isDefaultFavicon(HttpURLConnection http) throws IllegalArgumentException, IOException {
        return http.getContentType().equals("image/svg+xml");
    }

    private static Image resize(BufferedImage img) {
        return img.getWidth() > 16 || img.getHeight() > 16 ? img.getScaledInstance(16, 16, Image.SCALE_SMOOTH) : img;
    }
}
