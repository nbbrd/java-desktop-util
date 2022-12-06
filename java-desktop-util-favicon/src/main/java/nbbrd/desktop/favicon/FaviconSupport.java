package nbbrd.desktop.favicon;

import lombok.NonNull;
import nbbrd.desktop.favicon.spi.FaviconSupplier;
import nbbrd.desktop.favicon.spi.FaviconSupplierLoader;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    Map<FaviconRef, Image> cache = new HashMap<>();

    @NonNull
    @lombok.Builder.Default
    FaviconListener<? super String> onAsyncMessage = FaviconListener.noOp();

    @NonNull
    @lombok.Builder.Default
    FaviconListener<? super IOException> onAsyncError = FaviconListener.noOp();

    public @Nullable Icon get(@NonNull FaviconRef ref, @NonNull Component listener) {
        FaviconRef scaledRef = ScaledIcon.computeScaledRef(ref, listener);
        return toIcon(cache.computeIfAbsent(scaledRef, key -> sendRequest(key, listener::repaint)), ref);
    }

    public @Nullable Icon get(@NonNull FaviconRef ref, @NonNull Runnable onUpdate) {
        FaviconRef scaledRef = ScaledIcon.computeScaledRef(ref, null);
        return toIcon(cache.computeIfAbsent(scaledRef, key -> sendRequest(key, onUpdate)), ref);
    }

    public @Nullable Icon peek(@NonNull FaviconRef ref, @NonNull Component anchor) {
        FaviconRef scaledRef = ScaledIcon.computeScaledRef(ref, anchor);
        return toIcon(cache.getOrDefault(scaledRef, NULL_IMAGE), ref);
    }

    private @NonNull Image sendRequest(FaviconRef ref, Runnable onUpdate) {
        executor.execute(() -> asyncLoadIntoCache(ref, onUpdate));
        return NULL_IMAGE;
    }

    private void updateCacheAndNotify(FaviconRef ref, Image favicon, Runnable onUpdate) {
        cache.put(ref, favicon);
        onUpdate.run();
    }

    private void asyncLoadIntoCache(FaviconRef ref, Runnable onUpdate) {
        Image image = asyncLoadOrNull(ref);
        if (image != null) {
            SwingUtilities.invokeLater(() -> updateCacheAndNotify(ref, image, onUpdate));
        }
    }

    private Image asyncLoadOrNull(FaviconRef ref) {
        for (FaviconSupplier supplier : suppliers) {
            Image result = asyncLoadOrNull(ref, supplier);
            if (result != null) return result;
        }
        if (!ignoreParentDomain) {
            Optional<DomainName> parent = ref.getDomain().getParent();
            if (parent.isPresent()) {
                return asyncLoadOrNull(ref.withDomain(parent.get()));
            }
        }
        return null;
    }

    private Image asyncLoadOrNull(FaviconRef ref, FaviconSupplier supplier) {
        try {
            long start = System.currentTimeMillis();
            Image result = supplier.getFaviconOrNull(ref, client);
            long stop = System.currentTimeMillis();
            if (result != FaviconSupplier.NO_FAVICON) {
                onAsyncMessage.accept(ref, supplier.getName(), String.format("Loaded %sx%s in %sms", result.getWidth(null), result.getHeight(null), stop - start));
                return result;
            } else {
                onAsyncMessage.accept(ref, supplier.getName(), "Missing");
                return null;
            }
        } catch (IOException ex) {
            onAsyncError.accept(ref, supplier.getName(), ex);
            return null;
        }
    }

    private static Thread newLowPriorityDaemonThread(Runnable runnable) {
        Thread result = new Thread(runnable);
        result.setDaemon(true);
        result.setPriority(Thread.MIN_PRIORITY);
        return result;
    }

    private static final Image NULL_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    public static @Nullable Icon toIcon(@Nullable Image image, @NonNull FaviconRef ref) {
        return image == NULL_IMAGE || image == null ? null : new ScaledIcon(ref, image);
    }

    @lombok.RequiredArgsConstructor
    private static final class ScaledIcon implements Icon {

        public static FaviconRef computeScaledRef(FaviconRef ref, Component c) {
            double scale = getScale(c);
            return scale == 1.0 ? ref : ref.withSize((int) (ref.getSize() * scale));
        }

        private static @NonNull GraphicsConfiguration getGraphicsConfiguration(@Nullable Component c) {
            if (c != null) {
                Window window = SwingUtilities.getWindowAncestor(c);
                if (window != null) {
                    return window.getGraphicsConfiguration();
                }
            }
            return GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration();
        }

        public static double getScale(@Nullable Component c) {
            return getGraphicsConfiguration(c)
                    .getDefaultTransform()
                    .getScaleX();
        }

        private final FaviconRef ref;
        private final Image image;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(image, x, y, ref.getSize(), ref.getSize(), c);
            g2d.dispose();
        }

        @Override
        public int getIconWidth() {
            return ref.getSize();
        }

        @Override
        public int getIconHeight() {
            return ref.getSize();
        }
    }
}
