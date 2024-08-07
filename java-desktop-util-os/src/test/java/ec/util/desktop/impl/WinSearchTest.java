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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class WinSearchTest {

    @Test
    @EnabledOnOs(value = OS.WINDOWS, architectures = "amd64")
    public void testVbsSearch() throws IOException {
        WinSearch winSearch = WinSearch.VbsSearch.init();

        assertThat(winSearch.search("java"))
                .extracting(File::getPath)
                .noneMatch(item -> item.startsWith("mapi"));
    }

    @Test
    @EnabledOnOs(value = OS.WINDOWS, architectures = "amd64")
    public void testPowerShellSearch() throws IOException {
        WinSearch winSearch = WinSearch.PowerShellSearch.init();

        assertThat(winSearch.search("java"))
                .extracting(File::getPath)
                .noneMatch(item -> item.startsWith("mapi"));
    }
}
