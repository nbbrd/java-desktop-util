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
import java.util.function.BiFunction;

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

    public @NonNull Icon get(@NonNull FaviconRef ref) {
        return new Favicon(ref, FaviconSupport::doNothing, this::getOrLoadImage, null);
    }

    public @NonNull Icon getOrDefault(@NonNull FaviconRef ref, @NonNull Icon fallback) {
        return new Favicon(ref, FaviconSupport::doNothing, this::getOrLoadImage, fallback);
    }

    public @NonNull Icon get(@NonNull FaviconRef ref, @NonNull Runnable onUpdate) {
        return new Favicon(ref, onUpdate, this::getOrLoadImage, null);
    }

    public @NonNull Icon getOrDefault(@NonNull FaviconRef ref, @NonNull Runnable onUpdate, @NonNull Icon fallback) {
        return new Favicon(ref, onUpdate, this::getOrLoadImage, fallback);
    }

    private @Nullable Image getOrLoadImage(@NonNull FaviconRef key, @NonNull Runnable onUpdate) {
        Image result = cache.get(key);
        if (result != null) {
            return result != PENDING_IMAGE ? result : null;
        }
        executor.execute(() -> asyncLoadIntoCache(key, onUpdate));
        cache.put(key, PENDING_IMAGE);
        return null;
    }

    private static final Image PENDING_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

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
            Optional<FaviconRef> parent = ref.getParent();
            if (parent.isPresent()) {
                return asyncLoadOrNull(parent.get());
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

    @lombok.RequiredArgsConstructor
    private static final class Favicon implements Icon {

        private final @NonNull FaviconRef ref;

        private final @NonNull Runnable onUpdate;

        private final @NonNull BiFunction<FaviconRef, Runnable, Image> engine;

        private final @Nullable Icon fallback;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Image image = engine.apply(ref.scale(getScale(g)), onUpdate);
            if (image != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.drawImage(image, x, y, ref.getSize(), ref.getSize(), c);
                g2d.dispose();
            } else if (fallback != null) {
                fallback.paintIcon(c, g, x, y);
            }
        }

        private double getScale(Graphics g) {
            return g instanceof Graphics2D ? ((Graphics2D) g).getTransform().getScaleX() : 1.0;
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

    private static void doNothing() {
    }
}
