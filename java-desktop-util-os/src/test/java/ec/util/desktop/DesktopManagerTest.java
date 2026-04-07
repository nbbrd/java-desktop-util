package ec.util.desktop;

import ec.util.desktop.impl.AwtDesktop;
import ec.util.desktop.impl.MacDesktop;
import ec.util.desktop.impl.WinDesktop;
import ec.util.desktop.impl.XdgDesktop;
import nbbrd.io.sys.OS;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DesktopManagerTest {

    @Test
    void testGet() {
        Desktop desktop = DesktopManager.get();
        switch (OS.NAME) {
            case WINDOWS:
                assertThat(desktop).isInstanceOf(WinDesktop.class);
                break;
            case MACOS:
                assertThat(desktop).isInstanceOf(MacDesktop.class);
                break;
            case LINUX:
                assertThat(desktop).isInstanceOf(XdgDesktop.class);
                break;
            default:
                assertThat(desktop).isInstanceOf(AwtDesktop.class);
        }
    }
}