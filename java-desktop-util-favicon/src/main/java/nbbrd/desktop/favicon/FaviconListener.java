package nbbrd.desktop.favicon;

@FunctionalInterface
public interface FaviconListener<T> {

    void accept(FaviconRef ref, String supplier, T value);

    static <X> FaviconListener<X> noOp() {
        return (host, supplier, message) -> {
        };
    }
}
