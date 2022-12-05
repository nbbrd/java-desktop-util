package nbbrd.desktop.favicon;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class DomainNameTest {

    @Test
    public void testOf() throws MalformedURLException {
        assertThat(DomainName.of(new URL("https://www.google.com")))
                .hasToString("www.google.com");
    }

    @Test
    public void testParse() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> DomainName.parse("https://www.google.com"));

        assertThat(DomainName.parse("www.google.com"))
                .hasToString("www.google.com");

        assertThat(DomainName.parse("google.com"))
                .hasToString("google.com");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> DomainName.parse("com"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> DomainName.parse(""));

        assertThat(DomainName.parse("xn--bcher-kva.example"))
                .hasToString("xn--bcher-kva.example");
    }

    @Test
    public void testGetParent() {
        assertThat(DomainName.parse("www.google.com").getParent())
                .hasValue(DomainName.parse("google.com"));

        assertThat(DomainName.parse("google.com").getParent())
                .isEmpty();
    }
}