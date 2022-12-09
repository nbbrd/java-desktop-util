package nbbrd.desktop.favicon;

import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.design.swing.OnAnyThread;
import nbbrd.design.swing.OnEDT;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

/**
 * Tool used to retrieve favicons from any website.
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class FaviconSupport {

    /**
     * Creates a new instance using Java's {@link java.util.ServiceLoader} to gather resources.
     *
     * @return a new non-null instance
     */
    public static @NonNull FaviconSupport ofServiceLoader() {
        return FaviconSupport
                .builder()
                .suppliers(FaviconSupplierLoader.load())
                .build();
    }

    /**
     * Client that deal with web requests.
     */
    @NonNull
    @lombok.Builder.Default
    URLConnectionFactory client = URLConnectionFactory.getDefault();

    /**
     * Ordered list of favicon suppliers.
     */
    @lombok.Singular
    List<FaviconSupplier> suppliers;

    @lombok.Builder.Default
    boolean ignoreParentDomain = false;

    /**
     * Background thread that retrieve favicons from suppliers.
     */
    @NonNull
    @lombok.Builder.Default
    Executor executor = Executors.newCachedThreadPool(FaviconSupport::newLowPriorityDaemonThread);

    /**
     * Event dispatch thread that notify GUI of updates.
     */
    @NonNull
    @lombok.Builder.Default
    Executor dispatcher = SwingUtilities::invokeLater;

    /**
     * Cache that store favicons by domain and size.
     */
    @NonNull
    @lombok.Builder.Default
    Map<FaviconRef, Image> cache = new HashMap<>();

    /**
     * Message event listener. Beware that events are broadcast by the background thread.
     */
    @NonNull
    @lombok.Builder.Default
    FaviconListener<? super String> onExecutorMessage = FaviconListener.noOp();

    /**
     * Error event listener. Beware that events are broadcast by the background thread.
     */
    @NonNull
    @lombok.Builder.Default
    FaviconListener<? super IOException> onExecutorError = FaviconListener.noOp();

    /**
     * Get a favicon from a domain and a size.
     *
     * @param ref a non-null favicon reference
     * @return a non-null icon
     */
    @OnEDT
    public @NonNull Icon get(@NonNull FaviconRef ref) {
        return new Favicon(ref, FaviconSupport::doNothing, this::getOrLoadImage, null);
    }

    /**
     * Get a favicon from a domain and a size with a fallback icon.
     *
     * @param ref      a non-null favicon reference
     * @param fallback a non-null fallback icon
     * @return a non-null icon
     */
    @OnEDT
    public @NonNull Icon getOrDefault(@NonNull FaviconRef ref, @NonNull Icon fallback) {
        return new Favicon(ref, FaviconSupport::doNothing, this::getOrLoadImage, fallback);
    }

    /**
     * Get a favicon from a domain and a size.
     *
     * @param ref      a non-null favicon reference
     * @param onUpdate a non-null callback to be triggered when a favicon is retrieved
     * @return a non-null icon
     */
    @OnEDT
    public @NonNull Icon get(@NonNull FaviconRef ref, @NonNull Runnable onUpdate) {
        return new Favicon(ref, onUpdate, this::getOrLoadImage, null);
    }

    /**
     * Get a favicon from a domain and a size with a fallback icon.
     *
     * @param ref      a non-null favicon reference
     * @param onUpdate a non-null callback to be triggered when a favicon is retrieved
     * @param fallback a non-null fallback icon
     * @return a non-null icon
     */
    @OnEDT
    public @NonNull Icon getOrDefault(@NonNull FaviconRef ref, @NonNull Runnable onUpdate, @NonNull Icon fallback) {
        return new Favicon(ref, onUpdate, this::getOrLoadImage, fallback);
    }

    @OnEDT
    private @Nullable Image getOrLoadImage(@NonNull FaviconRef key, @NonNull Runnable onUpdate) {
        Image result = cache.get(key);
        if (result != null) {
            return result != PENDING_IMAGE ? result : null;
        } else {
            cache.put(key, PENDING_IMAGE);
            executor.execute(() -> asyncLoadIntoCache(key, onUpdate));
            return null;
        }
    }

    @VisibleForTesting
    static final Image PENDING_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    @OnEDT
    private void updateCacheAndNotify(FaviconRef ref, Image favicon, Runnable onUpdate) {
        cache.put(ref, favicon);
        onUpdate.run();
    }

    @OnAnyThread
    private void asyncLoadIntoCache(FaviconRef ref, Runnable onUpdate) {
        Image image = asyncLoadOrNull(ref);
        if (image != null) {
            dispatcher.execute(() -> updateCacheAndNotify(ref, image, onUpdate));
        }
    }

    @OnAnyThread
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

    @OnAnyThread
    private Image asyncLoadOrNull(FaviconRef ref, FaviconSupplier supplier) {
        try {
            long start = System.currentTimeMillis();
            Image result = supplier.getFaviconOrNull(ref, client);
            long stop = System.currentTimeMillis();
            if (result != FaviconSupplier.NO_FAVICON) {
                onExecutorMessage.accept(ref, supplier.getName(), String.format("Loaded %sx%s in %sms", result.getWidth(null), result.getHeight(null), stop - start));
                return result;
            } else {
                onExecutorMessage.accept(ref, supplier.getName(), "Missing");
                return null;
            }
        } catch (IOException ex) {
            onExecutorError.accept(ref, supplier.getName(), ex);
            return null;
        } catch (RuntimeException ex) {
            onExecutorError.accept(ref, supplier.getName(), new IOException("Unexpected " + ex.getClass().getName() + ": " + ex.getMessage(), ex));
            return null;
        }
    }

    private static Thread newLowPriorityDaemonThread(Runnable runnable) {
        Thread result = new Thread(runnable);
        result.setDaemon(true);
        result.setPriority(Thread.MIN_PRIORITY);
        return result;
    }

    @VisibleForTesting
    @lombok.RequiredArgsConstructor
    static final class Favicon implements Icon {

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
                g2d.drawImage(image, x, y, getIconWidth(), getIconHeight(), c);
                g2d.dispose();
            } else if (fallback != null) {
                fallback.paintIcon(c, g, x, y);
            }
        }

        @VisibleForTesting
        static double getScale(Graphics g) {
            return g instanceof Graphics2D ? ((Graphics2D) g).getTransform().getScaleX() : FaviconRef.NO_SCALE;
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
