package nbbrd.desktop.favicon;

import lombok.NonNull;
import nbbrd.desktop.favicon.spi.FaviconSupplier;
import nbbrd.desktop.favicon.spi.FaviconSupplierLoader;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class FaviconSupport {

    public static @NonNull FaviconSupport ofServiceLoader() {
        return FaviconSupport
                .builder()
                .suppliers(FaviconSupplierLoader.load())
                .build();
    }

    @NonNull
    @lombok.Builder.Default
    URLConnectionFactory client = URLConnectionFactory.getDefault();

    @lombok.Singular
    List<FaviconSupplier> suppliers;

    @lombok.Builder.Default
    boolean ignoreParentDomain = false;

    @NonNull
    @lombok.Builder.Default
    ExecutorService executor = Executors.newCachedThreadPool(FaviconSupport::newLowPriorityDaemonThread);

    // do not put URL as key because of very-slow first lookup
    @NonNull
    @lombok.Builder.Default
    Map<DomainName, Icon> cache = new HashMap<>();

    @NonNull
    @lombok.Builder.Default
    FaviconListener<? super String> onAsyncMessage = FaviconListener.noOp();

    @NonNull
    @lombok.Builder.Default
    FaviconListener<? super IOException> onAsyncError = FaviconListener.noOp();

    public @Nullable Icon get(@NonNull DomainName domainName, @NonNull Component listener) {
        return get(domainName, listener::repaint);
    }

    public @Nullable Icon get(@NonNull DomainName domainName, @NonNull Runnable onUpdate) {
        return NullIcon.unwrap(cache.computeIfAbsent(domainName, key -> sendRequest(key, onUpdate)));
    }

    public @Nullable Icon peek(@NonNull DomainName domainName) {
        return NullIcon.unwrap(cache.getOrDefault(domainName, NullIcon.INSTANCE));
    }

    private @NonNull NullIcon sendRequest(DomainName domainName, Runnable onUpdate) {
        executor.execute(() -> asyncLoadIntoCache(domainName, onUpdate));
        return NullIcon.INSTANCE;
    }

    private void updateCacheAndNotify(DomainName domainName, Icon favicon, Runnable onUpdate) {
        cache.put(domainName, favicon);
        onUpdate.run();
    }

    private void asyncLoadIntoCache(DomainName domainName, Runnable onUpdate) {
        Icon icon = asyncLoadOrNull(domainName);
        if (icon != null) {
            SwingUtilities.invokeLater(() -> updateCacheAndNotify(domainName, icon, onUpdate));
        }
    }

    private Icon asyncLoadOrNull(DomainName domainName) {
        for (FaviconSupplier supplier : suppliers) {
            Icon result = asyncLoadOrNull(domainName, supplier);
            if (result != null) return result;
        }
        return !ignoreParentDomain ? domainName.getParent().map(this::asyncLoadOrNull).orElse(null) : null;
    }

    private Icon asyncLoadOrNull(DomainName domainName, FaviconSupplier supplier) {
        try {
            long start = System.currentTimeMillis();
            Image result = supplier.getFaviconOrNull(domainName, client);
            long stop = System.currentTimeMillis();
            if (result != FaviconSupplier.NO_FAVICON) {
                onAsyncMessage.accept(domainName, supplier.getName(), String.format("Loaded %sx%s in %sms", result.getWidth(null), result.getHeight(null), stop - start));
                return new ImageIcon(result);
            } else {
                onAsyncMessage.accept(domainName, supplier.getName(), "Missing");
                return null;
            }
        } catch (IOException ex) {
            onAsyncError.accept(domainName, supplier.getName(), ex);
            return null;
        }
    }

    private static Thread newLowPriorityDaemonThread(Runnable runnable) {
        Thread result = new Thread(runnable);
        result.setDaemon(true);
        result.setPriority(Thread.MIN_PRIORITY);
        return result;
    }

    private enum NullIcon implements Icon {
        INSTANCE;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        @Override
        public int getIconWidth() {
            return 0;
        }

        @Override
        public int getIconHeight() {
            return 0;
        }

        public static @Nullable Icon unwrap(@Nullable Icon icon) {
            return icon instanceof NullIcon ? null : icon;
        }
    }
}
