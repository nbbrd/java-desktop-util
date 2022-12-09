package nbbrd.desktop.favicon;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * String that identify services on Internet such as websites.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Domain_name">wikipedia</a>
 */
@RepresentableAsString
@lombok.EqualsAndHashCode
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainName {

    /**
     * Creates a new instance from an {@link URL}
     *
     * @param url the domain name source
     * @return a non-null domain name
     */
    @StaticFactoryMethod
    public static @NonNull DomainName of(@NonNull URL url) {
        return new DomainName(url.getHost().split("\\.", -1));
    }

    /**
     * Creates a new instance by parsing a text.
     *
     * @param text the text to parse
     * @return a non-null domain name
     * @throws IllegalArgumentException if the text is not a valid domain name
     */
    @StaticFactoryMethod
    public static @NonNull DomainName parse(@NonNull CharSequence text) throws IllegalArgumentException {
        if (!isValid(text)) {
            throw new IllegalArgumentException("Invalid domain name");
        }
        return new DomainName(text.toString().split("\\.", -1));
    }

    private final @NonNull String[] parts;

    public @NonNull String getPart(int index) {
        return parts[index];
    }

    @Override
    public String toString() {
        return String.join(".", parts);
    }

    public @NonNull Optional<DomainName> getParent() {
        return parts.length > 2
                ? Optional.of(new DomainName(Arrays.copyOfRange(parts, 1, parts.length)))
                : Optional.empty();
    }

    /**
     * Check if the input text is a valid domain name.
     *
     * @param text the input text to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(CharSequence text) {
        return MAGIC_PATTERN.matcher(text).find();
    }

    // https://stackoverflow.com/a/26987741
    private static final Pattern MAGIC_PATTERN = Pattern.compile("^(((?!\\-))(xn\\-\\-)?[a-z0-9\\-_]{0,61}[a-z0-9]{1,1}\\.)*(xn\\-\\-)?([a-z0-9\\-]{1,61}|[a-z0-9\\-]{1,30})\\.[a-z]{2,}$");
}
