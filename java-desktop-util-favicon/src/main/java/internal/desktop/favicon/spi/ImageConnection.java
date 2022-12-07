package internal.desktop.favicon.spi;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.desktop.favicon.URLConnectionFactory;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ImageConnection implements Closeable {

    public static @NonNull ImageConnection open(@NonNull URLConnectionFactory factory, @NonNull URL url) throws IOException {
        URLConnection connection = factory.openConnection(url);
        if (!(connection instanceof HttpURLConnection)) {
            throw new IOException("Invalid url " + url);
        }
        connection.connect();
        return new ImageConnection((HttpURLConnection) connection);
    }

    private final @NonNull HttpURLConnection connection;

    public int getResponseCode() throws IOException {
        return connection.getResponseCode();
    }

    public @Nullable String getContentType() {
        return connection.getContentType();
    }

    public @Nullable BufferedImage readImage() throws IOException {
        try (InputStream stream = connection.getInputStream()) {
            return ImageIO.read(stream);
        }
    }

    @Override
    public void close() {
        connection.disconnect();
    }
}
