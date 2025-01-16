/*
 * Copyright 2013 National Bank of Belgium
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static ec.util.desktop.impl.XdgConfig.getConfigFile;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class XdgConfigTest {

    @Test
    public void testParseConfig() throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        Map<String, String> env = new HashMap<>();
        env.put("HOME", tmpDir);

        try (InputStream fis = XdgConfigTest.class.getResourceAsStream("user-dirs.dirs")) {
            XdgConfig config = XdgConfig.parseConfig(fis, env);
            Assertions.assertEquals(8, config.keySet().size());
            Assertions.assertEquals(Paths.get(tmpDir, "Documents").toFile(), Paths.get(config.get(XdgDesktop.DOCUMENTS_DIR)).toFile());
        }
    }

    @Test
    void getGetConfigFile() {
        assertThat(getConfigFile(ZSystem.noOp())).isNull();
        assertThat(getConfigFile(MapSystem.builder().property("user.home", null).build())).isNull();
        assertThat(getConfigFile(MapSystem.builder().property("user.home", "").build())).isNull();
        assertThat(getConfigFile(MapSystem.builder().property("user.home", "*\"/\\<>:|?").build())).isNull();
    }
}
