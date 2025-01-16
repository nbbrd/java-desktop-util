package ec.util.desktop.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static ec.util.desktop.impl.Util.fileFromPathname;
import static org.assertj.core.api.Assertions.assertThat;

class UtilTest {

    @Test
    void testFileFromPathname(@TempDir Path tmp) {
        assertThat(fileFromPathname(null)).isNull();
        assertThat(fileFromPathname("")).isNull();
        assertThat(fileFromPathname("!#:")).isNull();
        assertThat(fileFromPathname(tmp.toString())).isEqualTo(tmp.toFile());
    }
}