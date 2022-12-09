package nbbrd.desktop.favicon;

import _test.FaviconHelper;
import _test.ImageFunction;
import _test.ImageUtil;
import _test.MonochromeIcon;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import static _test.FaviconHelper.*;
import static _test.ImageFunction.*;
import static _test.ImageUtil.*;
import static nbbrd.desktop.favicon.FaviconSupport.PENDING_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.condition.Not.not;

public class FaviconSupportTest {

    @Test
    public void testOfServiceLoader() {
        FaviconSupport main = FaviconSupport.ofServiceLoader();
        assertThat(main.getSuppliers()).hasSize(3);
        assertThat(main.getCache()).isEmpty();
        assertThat(main.getClient()).isNotNull();
        assertThat(main.getExecutor()).isNotNull();
        assertThat(main.getDispatcher()).isNotNull();
        assertThat(main.getOnExecutorError()).isNotNull();
        assertThat(main.getOnExecutorMessage()).isNotNull();
        assertThat(main.isIgnoreParentDomain()).isFalse();
    }

    @Test
    public void testBuilder() {
        FaviconSupport main = FaviconSupport.builder().build();
        assertThat(main.getSuppliers()).isEmpty();
        assertThat(main.getCache()).isEmpty();
        assertThat(main.getClient()).isNotNull();
        assertThat(main.getExecutor()).isNotNull();
        assertThat(main.getDispatcher()).isNotNull();
        assertThat(main.getOnExecutorError()).isNotNull();
        assertThat(main.getOnExecutorMessage()).isNotNull();
        assertThat(main.isIgnoreParentDomain()).isFalse();
    }

    @Test
    public void testGetWithoutSupplier() {
        FaviconHelper helper = new FaviconHelper();

        FaviconSupport x = helper.toFaviconSupport();

        // retrievals without rendering
        for (int i = 0; i < 2; i++) {
            assertThat(x.getOrDefault(ref16, fallback16))
                    .has(iconSize(16));
            assertThat(helper)
                    .has(emptyCache())
                    .has(noRequest())
                    .has(noDispatch())
                    .has(noError());
        }

        // retrievals with rendering before execution and dispatching
        for (int i = 0; i < 2; i++) {
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRendering())
                    .has(imageSize(16))
                    .has(imageBinaryContent(fallback16));
            assertThat(helper)
                    .has(pendingCacheFor(ref16))
                    .has(requestCount(1))
                    .has(noDispatch())
                    .has(noError());
        }

        // retrievals with rendering after execution but before dispatching
        for (int i = 0; i < 2; i++) {
            helper.executeAll();
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRendering())
                    .has(imageSize(16))
                    .has(imageBinaryContent(fallback16));
            assertThat(helper)
                    .has(pendingCacheFor(ref16))
                    .has(noRequest())
                    .has(noDispatch())
                    .has(noError());
        }

        // retrievals with rendering after execution and dispatching
        for (int i = 0; i < 2; i++) {
            helper.dispatchAll();
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRendering())
                    .has(imageSize(16))
                    .has(imageBinaryContent(fallback16));
            assertThat(helper)
                    .has(pendingCacheFor(ref16))
                    .has(noRequest())
                    .has(noDispatch())
                    .has(noError());
        }
    }

    @Test
    public void testGetWithMissing() {
        FaviconHelper helper = new FaviconHelper();

        FaviconSupport x = helper.toFaviconSupport(onNoFavicon());

        // retrievals without rendering
        for (int i = 0; i < 2; i++) {
            assertThat(x.getOrDefault(ref16, fallback16))
                    .has(iconSize(16));
            assertThat(helper)
                    .has(emptyCache())
                    .has(noRequest())
                    .has(noDispatch())
                    .has(noError());
        }

        // retrievals with rendering before execution and dispatching
        for (int i = 0; i < 2; i++) {
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRendering())
                    .has(imageSize(16))
                    .has(imageBinaryContent(fallback16));
            assertThat(helper)
                    .has(pendingCacheFor(ref16))
                    .has(requestCount(1))
                    .has(noDispatch())
                    .has(noError());
        }

        // retrievals with rendering after execution but before dispatching
        for (int i = 0; i < 2; i++) {
            helper.executeAll();
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRendering())
                    .has(imageSize(16))
                    .has(imageBinaryContent(fallback16));
            assertThat(helper)
                    .has(pendingCacheFor(ref16))
                    .has(noRequest())
                    .has(noDispatch())
                    .has(noError());
        }

        // retrievals with rendering after execution and dispatching
        for (int i = 0; i < 2; i++) {
            helper.dispatchAll();
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRendering())
                    .has(imageSize(16))
                    .has(imageBinaryContent(fallback16));
            assertThat(helper)
                    .has(pendingCacheFor(ref16))
                    .has(noRequest())
                    .has(noDispatch())
                    .has(noError());
        }
    }

    @Test
    public void testGetWithFound() {
        FaviconHelper helper = new FaviconHelper();

        FaviconSupport x = helper.toFaviconSupport(ImageFunction.onMap(getSample()));

        // retrievals without rendering
        for (int i = 0; i < 2; i++) {
            assertThat(x.getOrDefault(ref16, fallback16))
                    .has(iconSize(16));
            assertThat(helper)
                    .has(emptyCache())
                    .has(noRequest())
                    .has(noDispatch())
                    .has(noError());
        }

        // retrievals with rendering before execution and dispatching
        for (int i = 0; i < 2; i++) {
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRendering())
                    .has(imageSize(16))
                    .has(imageBinaryContent(fallback16));
            assertThat(helper)
                    .has(pendingCacheFor(ref16))
                    .has(requestCount(1))
                    .has(noDispatch())
                    .has(noError());
        }

        // retrievals with rendering after execution but before dispatching
        for (int i = 0; i < 2; i++) {
            helper.executeAll();
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRendering())
                    .has(imageSize(16))
                    .has(imageBinaryContent(fallback16));
            assertThat(helper)
                    .has(pendingCacheFor(ref16))
                    .has(noRequest())
                    .has(dispatchCount(1))
                    .has(noError());
        }

        // retrievals with rendering after execution and dispatching
        for (int i = 0; i < 2; i++) {
            helper.dispatchAll();
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRendering())
                    .has(imageSize(16))
                    .has(imageBinaryContent(icon16));
            assertThat(helper)
                    .has(not(pendingCacheFor(ref16)))
                    .has(noRequest())
                    .has(noDispatch())
                    .has(noError());
        }
    }

    @Test
    public void testGetWithScale() {
        FaviconHelper helper = new FaviconHelper();

        FaviconSupport x = helper.toFaviconSupport(ImageFunction.onMap(getSample()));

        // retrievals without rendering
        for (int i = 0; i < 2; i++) {
            assertThat(x.getOrDefault(ref16, fallback16))
                    .has(iconSize(16));
            assertThat(helper)
                    .has(emptyCache())
                    .has(noRequest())
                    .has(noDispatch())
                    .has(noError());
        }

        // retrievals with rendering before execution and dispatching
        for (int i = 0; i < 2; i++) {
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRenderingAtScale(2))
                    .has(imageSize(16))
                    .has(imageBinaryContent(fallback16));
            assertThat(helper)
                    .has(pendingCacheFor(ref32))
                    .has(requestCount(1))
                    .has(noDispatch())
                    .has(noError());
        }

        // retrievals with rendering after execution but before dispatching
        for (int i = 0; i < 2; i++) {
            helper.executeAll();
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRenderingAtScale(2))
                    .has(imageSize(16))
                    .has(imageBinaryContent(fallback16));
            assertThat(helper)
                    .has(pendingCacheFor(ref32))
                    .has(noRequest())
                    .has(dispatchCount(1))
                    .has(noError());
        }

        // retrievals with rendering after execution and dispatching
        for (int i = 0; i < 2; i++) {
            helper.dispatchAll();
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRenderingAtScale(2))
                    .has(imageSize(16))
                    .has(not(imageBinaryContent(icon16)))
                    .has(not(imageBinaryContent(fallback16)))
                    .has(imageBinaryContent(icon32Scale2));
            assertThat(helper)
                    .has(not(pendingCacheFor(ref32)))
                    .has(noRequest())
                    .has(noDispatch())
                    .has(noError());
        }
    }

    @Test
    public void testGetWithExpectedError() {
        FaviconHelper helper = new FaviconHelper();

        FaviconSupport x = helper.toFaviconSupport(onIOException(FileNotFoundException::new));

        // retrievals without rendering
        for (int i = 0; i < 2; i++) {
            assertThat(x.getOrDefault(ref16, fallback16))
                    .has(iconSize(16));
            assertThat(helper)
                    .has(emptyCache())
                    .has(noRequest())
                    .has(noDispatch())
                    .has(noError());
        }

        // retrievals with rendering before execution and dispatching
        for (int i = 0; i < 2; i++) {
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRendering())
                    .has(imageSize(16))
                    .has(imageBinaryContent(fallback16));
            assertThat(helper)
                    .has(pendingCacheFor(ref16))
                    .has(requestCount(1))
                    .has(noDispatch())
                    .has(noError());
        }

        // retrievals with rendering after execution but before dispatching
        for (int i = 0; i < 2; i++) {
            helper.executeAll();
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRendering())
                    .has(imageSize(16))
                    .has(imageBinaryContent(fallback16));
            assertThat(helper)
                    .has(pendingCacheFor(ref16))
                    .has(noRequest())
                    .has(noDispatch())
                    .has(errorCount(1));
        }

        // retrievals with rendering after execution and dispatching
        for (int i = 0; i < 2; i++) {
            helper.dispatchAll();
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRendering())
                    .has(imageSize(16))
                    .has(imageBinaryContent(fallback16));
            assertThat(helper)
                    .has(pendingCacheFor(ref16))
                    .has(noRequest())
                    .has(noDispatch())
                    .has(errorCount(1));
        }
    }

    @Test
    public void testGetWithUnexpectedError() {
        FaviconHelper helper = new FaviconHelper();

        FaviconSupport x = helper.toFaviconSupport(onRuntimeException(NullPointerException::new));

        // retrievals without rendering
        for (int i = 0; i < 2; i++) {
            assertThat(x.getOrDefault(ref16, fallback16))
                    .has(iconSize(16));
            assertThat(helper)
                    .has(emptyCache())
                    .has(noRequest())
                    .has(noDispatch())
                    .has(noError());
        }

        // retrievals with rendering before execution and dispatching
        for (int i = 0; i < 2; i++) {
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRendering())
                    .has(imageSize(16))
                    .has(imageBinaryContent(fallback16));
            assertThat(helper)
                    .has(pendingCacheFor(ref16))
                    .has(requestCount(1))
                    .has(noDispatch())
                    .has(noError());
        }

        // retrievals with rendering after execution but before dispatching
        for (int i = 0; i < 2; i++) {
            helper.executeAll();
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRendering())
                    .has(imageSize(16))
                    .has(imageBinaryContent(fallback16));
            assertThat(helper)
                    .has(pendingCacheFor(ref16))
                    .has(noRequest())
                    .has(noDispatch())
                    .has(errorCount(1));
        }

        // retrievals with rendering after execution and dispatching
        for (int i = 0; i < 2; i++) {
            helper.dispatchAll();
            assertThat(x.getOrDefault(ref16, fallback16))
                    .extracting(byRendering())
                    .has(imageSize(16))
                    .has(imageBinaryContent(fallback16));
            assertThat(helper)
                    .has(pendingCacheFor(ref16))
                    .has(noRequest())
                    .has(noDispatch())
                    .has(errorCount(1));
        }
    }

    @Test
    public void testHelpers() {
        assertThat(icon16)
                .extracting(byRendering())
                .has(imageBinaryContent(icon16))
                .has(not(imageBinaryContent(icon32)))
                .has(not(imageBinaryContent(fallback16)));

        assertThat(icon16)
                .extracting(byRenderingAtScale(2))
                .has(imageBinaryContent(icon16))
                .has(not(imageBinaryContent(icon32)))
                .has(not(imageBinaryContent(fallback16)));
    }

    private final DomainName domain = DomainName.parse("nbb.be");
    private final FaviconRef ref16 = FaviconRef.of(domain, 16);
    private final FaviconRef ref32 = FaviconRef.of(domain, 32);
    private final Icon icon16 = new MonochromeIcon(Color.RED, 16);
    private final Icon icon32 = new MonochromeIcon(Color.GREEN, 32);
    private final Icon icon32Scale2 = new MonochromeIcon(Color.GREEN, 16);
    private final Icon fallback16 = new MonochromeIcon(Color.BLUE, 16);

    private Map<FaviconRef, Image> getSample() {
        Map<FaviconRef, Image> result = new HashMap<>();
        result.put(ref16, ImageUtil.render(icon16, 1));
        result.put(ref32, ImageUtil.render(icon32, 1));
        return result;
    }

    private static Condition<FaviconHelper> pendingCacheFor(FaviconRef ref) {
        return new Condition<>(helper -> helper.getCache().get(ref) == PENDING_IMAGE, "pending cache for " + ref);
    }
}
