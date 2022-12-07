package _test;

import nbbrd.desktop.favicon.FaviconRef;
import nbbrd.desktop.favicon.FaviconSupport;
import org.assertj.core.api.Condition;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@lombok.Getter
public final class FaviconHelper {

    final List<Runnable> requests = new LinkedList<>();

    final List<Runnable> dispatch = new LinkedList<>();

    final Map<FaviconRef, Image> cache = new HashMap<>();

    final List<IOException> errors = new ArrayList<>();

    public Executor asExecutor() {
        return requests::add;
    }

    public Executor asEdt() {
        return dispatch::add;
    }

    public void executeAll() {
        consume(requests);
    }

    public void dispatchAll() {
        consume(dispatch);
    }

    private static void consume(List<Runnable> queue) {
        queue.forEach(Runnable::run);
        queue.clear();
    }

    public FaviconSupport.Builder toFaviconSupportBuilder() {
        return FaviconSupport
                .builder()
                .executor(asExecutor())
                .edt(asEdt())
                .cache(getCache())
                .onAsyncError((ref, supplier, ex) -> errors.add(ex));
    }

    public FaviconSupport toFaviconSupport(ImageFunction... suppliers) {
        return toFaviconSupportBuilder()
                .suppliers(Stream.of(suppliers).map(ImageFunction::asSupplier).collect(Collectors.toList()))
                .build();
    }

    public static Condition<FaviconHelper> emptyCache() {
        return new Condition<>(helper -> helper.getCache().isEmpty(), "empty cache");
    }

    public static Condition<FaviconHelper> noRequest() {
        return new Condition<>(helper -> helper.getRequests().isEmpty(), "no request");
    }

    public static Condition<FaviconHelper> noDispatch() {
        return new Condition<>(helper -> helper.getDispatch().isEmpty(), "no dispatch");
    }

    public static Condition<FaviconHelper> noError() {
        return new Condition<>(helper -> helper.getErrors().isEmpty(), "no error");
    }

    public static Condition<FaviconHelper> requestCount(int count) {
        return new Condition<>(helper -> helper.getRequests().size() == count, "request count " + count);
    }

    public static Condition<FaviconHelper> dispatchCount(int count) {
        return new Condition<>(helper -> helper.getDispatch().size() == count, "dispatch count " + count);
    }

    public static Condition<FaviconHelper> errorCount(int count) {
        return new Condition<>(helper -> helper.getErrors().size() == count, "error count " + count);
    }
}
