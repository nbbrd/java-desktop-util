package nbbrd.desktop.favicon;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class FaviconRefTest {

    @Test
    public void testOf() {
        assertThatNullPointerException().isThrownBy(() -> FaviconRef.of(null, 16));
    }

    @Test
    public void testGetParent() {
        assertThat(FaviconRef.of(www_google_com, 16).getParent())
                .hasValue(FaviconRef.of(google_com, 16));

        assertThat(FaviconRef.of(google_com, 16).getParent())
                .isEmpty();
    }

    @Test
    public void testScale() {
        FaviconRef ref = FaviconRef.of(google_com, 16);

        assertThatIllegalArgumentException().isThrownBy(() -> ref.scale(-1));

        assertThatIllegalArgumentException().isThrownBy(() -> ref.scale(0));

        assertThat(ref.scale(0.5)).isEqualTo(FaviconRef.of(google_com, 8));

        assertThat(ref.scale(1)).isSameAs(ref);

        assertThat(ref.scale(1.25)).isEqualTo(FaviconRef.of(google_com, 20));
    }

    private final DomainName www_google_com = DomainName.parse("www.google.com");
    private final DomainName google_com = DomainName.parse("google.com");
}
