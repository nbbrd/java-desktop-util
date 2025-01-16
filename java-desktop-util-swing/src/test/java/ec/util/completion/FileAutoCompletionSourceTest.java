package ec.util.completion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class FileAutoCompletionSourceTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testGetValues(@TempDir Path tmp) throws IOException {
        Path a = Files.createTempFile(tmp, "a", "");
        Path b = Files.createTempFile(tmp, "b", "");
        Path c = Files.createDirectory(tmp.resolve("c"));
        Path d = Files.createTempFile(c, "d", "");

        FileAutoCompletionSource x = new FileAutoCompletionSource(false, null, new File[]{c.toFile()});

        assertThatNullPointerException()
                .isThrownBy(() -> x.getValues(null));

        assertThat(x.getValues(""))
                .isEmpty();

        assertThat(x.getValues("!#:"))
                .isEmpty();

        assertThat(x.getValues(tmp.toString()))
                .map(File::toPath)
                .containsExactly(a, b, c);

        assertThat(x.getValues(tmp.resolve("a").toString()))
                .map(File::toPath)
                .containsExactly(a);

        assertThat(x.getValues(tmp.resolve("c").toString()))
                .map(File::toPath)
                .containsExactly(d);

        assertThat(x.getValues(tmp.resolve("d").toString()))
                .map(File::toPath)
                .isEmpty();

        assertThat(x.getValues("a"))
                .map(File::toPath)
                .isEmpty();

        assertThat(x.getValues("c"))
                .map(File::toPath)
                .isEmpty();

        assertThat(x.getValues("d"))
                .map(File::toPath)
                .containsExactly(d.getFileName());
    }
}