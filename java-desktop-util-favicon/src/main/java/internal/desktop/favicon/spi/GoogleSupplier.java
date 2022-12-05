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
public final class GoogleSupplier implements FaviconSupplier {

    @Override
    public @NonNull String getName() {
        return "Google";
    }

    @Override
    public int getRank() {
        return 100;
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

    private URL getFaviconRequest(FaviconRef ref) throws MalformedURLException {
        int preferredSize = getPreferredSize(ref.getSize());
        return new URL("https://www.google.com/s2/favicons?domain=" + ref.getDomain() + "&sz=" + preferredSize);
    }

    private static boolean isDefaultFavicon(HttpURLConnection http) throws IOException {
        return http.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND;
    }

    private static int getPreferredSize(int size) {
        for (int value : SIZES) {
            if (size <= value) {
                return value;
            }
        }
        return size;
    }

    private static final int[] SIZES = {16, 24, 32, 64};
}
