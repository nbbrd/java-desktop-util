/*
 * Copyright 2019 National Bank of Belgium
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

import java.io.File;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class WinSearchTest {

    @Test
    public void testJnaSearch() throws IOException {
        Assume.assumeTrue(Util.is64bit() && isWindows());

        WinSearch winSearch = new JnaSearch();

        assertThat(winSearch.search("java"))
                .extracting(File::getPath)
                .noneMatch(item -> item.startsWith("mapi"));
    }

    @Test
    public void testVbsSearch() throws IOException {
        Assume.assumeTrue(Util.is64bit() && isWindows());

        WinSearch winSearch = new WinSearch.VbsSearch(
                WinScriptHost.getDefault(),
                Util.extractResource("winsearch.vbs", "winsearch", ".vbs")
        );

        assertThat(winSearch.search("java"))
                .extracting(File::getPath)
                .noneMatch(item -> item.startsWith("mapi"));
    }

    static boolean isWindows() {
        String osName = System.getProperty("os.name");
        return osName != null && osName.startsWith("Windows ");
    }
}
