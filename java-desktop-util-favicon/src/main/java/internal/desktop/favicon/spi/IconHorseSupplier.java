package internal.desktop.favicon.spi;

import lombok.NonNull;
import nbbrd.desktop.favicon.FaviconRef;
import nbbrd.desktop.favicon.URLConnectionFactory;
import nbbrd.desktop.favicon.spi.FaviconSupplier;
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
        URLConnection connection = client.openConnection(getFaviconRequest(ref));
        if (!(connection instanceof HttpURLConnection)) {
            throw new IOException("Invalid url");
        }
        HttpURLConnection http = (HttpURLConnection) connection;
        try {
            try (InputStream stream = http.getInputStream()) {
                BufferedImage result = ImageIO.read(stream);
                return isDefaultFavicon(http, result) ? null : result;
            }
        } finally {
            http.disconnect();
        }
    }

    private static URL getFaviconRequest(FaviconRef ref) throws MalformedURLException {
        return new URL("https://icon.horse/icon/" + ref.getDomain());
    }

    private static boolean isDefaultFavicon(HttpURLConnection http, BufferedImage image) throws IllegalArgumentException {
        return "image/png".equals(http.getContentType())
                && image.getHeight(null) == 512
                && image.getWidth(null) == 512
                && image.getRGB(0, 0) == -14735049;
    }
}
