package ec.util.desktop.impl;

import ec.util.desktop.Desktop;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class MacDesktopTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testGetKnownFolder(@TempDir Path tmp) throws IOException {
        assertThatNullPointerException()
                .isThrownBy(() -> new MacDesktop(ZSystem.noOp()).getKnownFolder(null));

        Path desktop = tmp.resolve("Desktop");
        Files.createDirectory(desktop);

        MacDesktop noHome = new MacDesktop(onUserHome(null));
        for (Desktop.KnownFolder o : Desktop.KnownFolder.values()) {
            assertThat(noHome.getKnownFolder(o)).isNull();
        }

        MacDesktop invalidHome = new MacDesktop(onUserHome("*\"/\\<>:|?\0"));
        for (Desktop.KnownFolder o : Desktop.KnownFolder.values()) {
            assertThat(invalidHome.getKnownFolder(o)).isNull();
        }

        MacDesktop missingHome = new MacDesktop(onUserHome(tmp.resolve("missing").toString()));
        for (Desktop.KnownFolder o : Desktop.KnownFolder.values()) {
            assertThat(missingHome.getKnownFolder(o)).isNull();
        }

        MacDesktop validHome = new MacDesktop(onUserHome(tmp.toString()));
        for (Desktop.KnownFolder o : Desktop.KnownFolder.values()) {
            if (o.equals(Desktop.KnownFolder.DESKTOP)) {
                assertThat(validHome.getKnownFolder(o)).isEqualTo(desktop.toFile());
            } else {
                assertThat(validHome.getKnownFolder(o)).isNull();
            }
        }
    }

    private static ZSystem onUserHome(String path) {
        return MapSystem
                .builder()
                .property("user.home", path)
                .build();
    }
}