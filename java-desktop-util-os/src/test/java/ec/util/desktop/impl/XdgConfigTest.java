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

import static ec.util.desktop.impl.XdgConfig.getConfigFile;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Philippe Charles
 */
public class XdgConfigTest {

    @Test
    void parseConfig() throws IOException {
        String home = System.getProperty("java.io.tmpdir");
        Map<String, String> env = new HashMap<>();
        env.put("HOME", home);

        String source = "# comment\n"
                + "not-an-assignment\n"
                + "EMPTY=\n"
                + "DOCUMENTS_DIR=\"$HOME/Documents\"\n"
                + "PLAIN_DIR=/srv/\"shared\"\n";
        XdgConfig config =
                XdgConfig.parseConfig(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)), env);

        assertThat(config.keySet()).containsExactlyInAnyOrder("EMPTY", "DOCUMENTS_DIR", "PLAIN_DIR");
        assertThat(config.get("EMPTY")).isEmpty();
        assertThat(Paths.get(config.get("DOCUMENTS_DIR")).toFile())
                .isEqualTo(Paths.get(home, "Documents").toFile());
        assertThat(config.get("PLAIN_DIR")).isEqualTo("/srv/shared");
        assertThat(config.get("MISSING")).isNull();
    }

    @Test
    void getConfigFilePrefersXdgConfigHome(@TempDir Path temp) throws IOException {
        Path xdgConfigHome = Files.createDirectories(temp.resolve("xdg"));
        Path homeConfig = Files.createDirectories(temp.resolve("home").resolve(".config"));
        Path xdgFile = Files.createFile(xdgConfigHome.resolve("user-dirs.dirs"));
        Files.createFile(homeConfig.resolve("user-dirs.dirs"));

        assertThat(getConfigFile(
                        system(xdgConfigHome.toString(), temp.resolve("home").toString())))
                .isEqualTo(xdgFile.toFile());
    }

    @Test
    void getConfigFileFallsBackToHomeConfig(@TempDir Path temp) throws IOException {
        Path home = Files.createDirectories(temp.resolve("home"));
        Path configFile = Files.createDirectories(home.resolve(".config")).resolve("user-dirs.dirs");
        Files.createFile(configFile);

        assertThat(getConfigFile(system(temp.resolve("missing").toString(), home.toString())))
                .isEqualTo(configFile.toFile());
    }

    @Test
    void getConfigFileReturnsNullWhenNoConfigExists(@TempDir Path temp) {
        assertThat(getConfigFile(system(
                        temp.resolve("missing-xdg").toString(),
                        temp.resolve("missing-home").toString())))
                .isNull();
        assertThat(getConfigFile(ZSystem.noOp())).isNull();
        assertThat(getConfigFile(system(null, null))).isNull();
        assertThat(getConfigFile(system(null, ""))).isNull();
        assertThat(getConfigFile(system(null, "\0"))).isNull();
    }

    @Test
    void getInstanceReadsAndExpandsConfig(@TempDir Path temp) throws IOException {
        Path configHome = Files.createDirectories(temp.resolve("config"));
        Files.write(
                configHome.resolve("user-dirs.dirs"),
                "XDG_DOCUMENTS_DIR=\"$HOME/Documents\"\n".getBytes(StandardCharsets.UTF_8));

        XdgConfig config = XdgConfig.getInstance(system(configHome.toString(), temp.toString()));

        assertThat(Paths.get(config.get(XdgDesktop.DOCUMENTS_DIR)).toFile())
                .isEqualTo(temp.resolve("Documents").toFile());
    }

    @Test
    void getInstanceReturnsNoOpWhenConfigIsMissing(@TempDir Path temp) throws IOException {
        XdgConfig config = XdgConfig.getInstance(system(
                temp.resolve("missing-xdg").toString(),
                temp.resolve("missing-home").toString()));

        assertThat(config).isSameAs(XdgConfig.noOp());
        assertThat(config.get("ANY_KEY")).isNull();
        assertThat(config.keySet()).isEmpty();
    }

    @Test
    void noOpConfigIsEmpty() {
        XdgConfig config = XdgConfig.noOp();

        assertThat(XdgConfig.noOp()).isSameAs(config);
        assertThat(config.get("ANY_KEY")).isNull();
        assertThat(config.keySet()).isEmpty();
    }

    private static ZSystem system(String xdgConfigHome, String userHome) {
        return new ZSystem() {
            @Override
            public String getProperty(String key) {
                return "user.home".equals(key) ? userHome : null;
            }

            @Override
            public Process exec(String... cmdarray) throws IOException {
                throw new IOException();
            }

            @Override
            public Map<String, String> getEnv() {
                Map<String, String> result = new HashMap<>();
                result.put("HOME", userHome);
                return result;
            }

            @Override
            public String getEnv(String name) {
                return "XDG_CONFIG_HOME".equals(name) ? xdgConfigHome : null;
            }
        };
    }
}
