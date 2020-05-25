/*
 * Copyright 2020 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.util.desktop.impl;

import static ec.util.desktop.impl.WinRegistry.Root.HKEY_LOCAL_MACHINE;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import org.junit.Assume;

/**
 *
 * @author Philippe Charles
 */
public class WinRegistryTest {

    static boolean isWindows() {
        return System.getProperty("os.name").contains("Windows");
    }

    private static final String CURRENT_VERSION_NODE = "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion";
    private static final String SYSTEM_ROOT_LEAF = "SystemRoot";
    private static final String MISSING_NODE = "XYZ";

    static void testKeyExists(WinRegistry reg) throws IOException {
        Assume.assumeTrue(isWindows());

        assertThatNullPointerException()
                .isThrownBy(() -> reg.keyExists(null, CURRENT_VERSION_NODE));

        assertThatNullPointerException()
                .isThrownBy(() -> reg.keyExists(HKEY_LOCAL_MACHINE, null));

        assertThat(reg.keyExists(HKEY_LOCAL_MACHINE, CURRENT_VERSION_NODE))
                .isTrue();

        assertThat(reg.keyExists(HKEY_LOCAL_MACHINE, CURRENT_VERSION_NODE + "\\" + SYSTEM_ROOT_LEAF))
                .isFalse();

        assertThat(reg.keyExists(HKEY_LOCAL_MACHINE, MISSING_NODE))
                .isFalse();
    }

    static void testGetValue(WinRegistry reg) throws IOException {
        Assume.assumeTrue(isWindows());

        assertThatNullPointerException()
                .isThrownBy(() -> reg.getValue(null, CURRENT_VERSION_NODE, SYSTEM_ROOT_LEAF));

        assertThatNullPointerException()
                .isThrownBy(() -> reg.getValue(HKEY_LOCAL_MACHINE, null, SYSTEM_ROOT_LEAF));

        assertThatNullPointerException()
                .isThrownBy(() -> reg.getValue(HKEY_LOCAL_MACHINE, CURRENT_VERSION_NODE, null));

        assertThat(reg.getValue(HKEY_LOCAL_MACHINE, CURRENT_VERSION_NODE, SYSTEM_ROOT_LEAF))
                .isEqualTo(System.getenv("SYSTEMROOT"));

        assertThat(reg.getValue(HKEY_LOCAL_MACHINE, CURRENT_VERSION_NODE, "xyz"))
                .isNull();

        assertThat(reg.getValue(HKEY_LOCAL_MACHINE, MISSING_NODE, SYSTEM_ROOT_LEAF))
                .isNull();
    }

    static void testGetValues(WinRegistry reg) throws IOException {
        Assume.assumeTrue(isWindows());

        assertThatNullPointerException()
                .isThrownBy(() -> reg.getValues(null, CURRENT_VERSION_NODE));

        assertThatNullPointerException()
                .isThrownBy(() -> reg.getValues(HKEY_LOCAL_MACHINE, null));

        assertThat(reg.getValues(HKEY_LOCAL_MACHINE, CURRENT_VERSION_NODE))
                .containsEntry(SYSTEM_ROOT_LEAF, System.getenv("SYSTEMROOT"));

        assertThat(reg.getValues(HKEY_LOCAL_MACHINE, MISSING_NODE))
                .isEmpty();
    }
}
