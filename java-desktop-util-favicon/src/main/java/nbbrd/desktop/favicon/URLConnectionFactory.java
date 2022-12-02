package nbbrd.desktop.favicon;

import lombok.NonNull;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

@FunctionalInterface
public interface URLConnectionFactory {

    @NonNull URLConnection openConnection(@NonNull URL url) throws IOException;

    static @NonNull URLConnectionFactory getDefault() {
        return URL::openConnection;
    }
}
