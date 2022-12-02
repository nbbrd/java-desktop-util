package nbbrd.desktop.favicon;

@FunctionalInterface
public interface FaviconListener<T> {

    void accept(DomainName domainName, String supplier, T value);

    static <X> FaviconListener<X> noOp() {
        return (host, supplier, message) -> {
        };
    }
}
