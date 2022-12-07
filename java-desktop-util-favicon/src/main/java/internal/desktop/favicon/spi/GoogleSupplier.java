package internal.desktop.favicon.spi;

import lombok.NonNull;
import nbbrd.desktop.favicon.FaviconRef;
import nbbrd.desktop.favicon.URLConnectionFactory;
import nbbrd.desktop.favicon.spi.FaviconSupplier;
import nbbrd.service.ServiceProvider;

import java.awt.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_OK;

@ServiceProvider
public final class GoogleSupplier implements FaviconSupplier {

    @Override
    public @NonNull String getName() {
        return "Google";
    }

    @Override
    public int getRank() {
        return 600;
    }

    @Override
    public Image getFaviconOrNull(@NonNull FaviconRef ref, @NonNull URLConnectionFactory client) throws IOException {
        try (ImageConnection conn = ImageConnection.open(client, getFaviconRequest(ref))) {
            if (conn.getResponseCode() == HTTP_OK && !isDefaultFavicon(conn.getResponseCode())) {
                return conn.readImage();
            }
            return NO_FAVICON;
        }
    }

    private URL getFaviconRequest(FaviconRef ref) throws MalformedURLException {
        int roundedSize = roundSize(ref.getSize());
        return new URL("https://t0.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=http://" + ref.getDomain() + "&size=" + roundedSize);
    }

    private static boolean isDefaultFavicon(int responseCode) {
        return responseCode == HttpURLConnection.HTTP_NOT_FOUND;
    }

    private static int roundSize(int size) {
        for (int value : COMMON_SIZES) {
            if (size <= value) {
                return value;
            }
        }
        return size;
    }

    private static final int[] COMMON_SIZES = {16, 24, 32, 64};
}
