package nbbrd.desktop.favicon;

import lombok.NonNull;

@FunctionalInterface
public interface FaviconListener<T> {

    void accept(@NonNull FaviconRef ref, @NonNull String supplier, @NonNull T value);

    static <X> @NonNull FaviconListener<X> noOp() {
        return (ref, supplier, message) -> {
        };
    }
}
