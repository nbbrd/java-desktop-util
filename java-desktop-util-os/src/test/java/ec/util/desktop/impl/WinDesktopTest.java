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

import ec.util.desktop.Desktop;
import static ec.util.desktop.Desktop.Action.SEARCH;
import static ec.util.desktop.Desktop.Action.SHOW_IN_FOLDER;
import static ec.util.desktop.Desktop.KnownFolder.DESKTOP;
import static ec.util.desktop.impl.WinDesktop.DESKTOP_DIR;
import static ec.util.desktop.impl.WinDesktop.DESKTOP_SEARCH_KEY_PATH;
import static ec.util.desktop.impl.WinDesktop.SHELL_FOLDERS_KEY_PATH;
import static ec.util.desktop.impl.WinRegistry.Root.HKEY_CURRENT_USER;
import static ec.util.desktop.impl.WinRegistry.Root.HKEY_LOCAL_MACHINE;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class WinDesktopTest {

    static Input GOOD, BAD, UGLY;

    @BeforeClass
    public static void beforeClass() throws IOException {
        File script = File.createTempFile("search", "");
        script.deleteOnExit();
        GOOD = new Input(FakeRegistry.create(), script, new FakeLauncher(), new FakeSearch());
        BAD = new Input(WinRegistry.noOp(), new File("helloworld"), ZSystem.noOp(), WinSearch.noOp());
        UGLY = new Input(WinRegistry.failing(), null, ZSystem.failing(), WinSearch.failing());
    }

    @Test
    public void testIsSupportedShowInFolder() {
        assertTrue(new WinDesktop(BAD.registry, BAD.system, BAD.search).isSupported(SHOW_IN_FOLDER));
    }

    @Test
    public void testIsSupportedSearch() {
        assertFalse(new WinDesktop(BAD.registry, BAD.system, BAD.search).isSupported(SEARCH));
        assertFalse(new WinDesktop(BAD.registry, BAD.system, GOOD.search).isSupported(SEARCH));
        assertTrue(new WinDesktop(GOOD.registry, BAD.system, BAD.search).isSupported(SEARCH));
        assertTrue(new WinDesktop(GOOD.registry, BAD.system, GOOD.search).isSupported(SEARCH));
    }

    @Test()
    public void testShowInfolder1() throws IOException {
        new WinDesktop(BAD.registry, GOOD.system, BAD.search).showInFolder(GOOD.script);
    }

    @Test()
    public void testShowInfolder2() throws IOException {
        new WinDesktop(BAD.registry, BAD.system, BAD.search).showInFolder(GOOD.script);
    }

    @Test(expected = IOException.class)
    public void testShowInfolder3() throws IOException {
        new WinDesktop(BAD.registry, UGLY.system, BAD.search).showInFolder(GOOD.script);
    }

    @Test
    public void testGetKnownFolder() {
        for (Desktop.KnownFolder o : Desktop.KnownFolder.values()) {
            assertNull(new WinDesktop(BAD.registry, BAD.system, BAD.search).getKnownFolder(o));
        }
        assertEquals(new File("hello"), new WinDesktop(GOOD.registry, BAD.system, BAD.search).getKnownFolder(DESKTOP));
        assertNull(new WinDesktop(UGLY.registry, BAD.system, BAD.search).getKnownFolder(DESKTOP));
    }

    @Test
    public void testSearch1() throws IOException {
        assertArrayEquals(new File[]{new File("hello.html")}, new WinDesktop(GOOD.registry, GOOD.system, GOOD.search).search("hello"));
    }

    @Test
    public void testSearch2() throws IOException {
//        assertFalse(new WinDesktop(GOOD.registry, BAD.system, BAD.search).isSupported(SEARCH));
    }

    @Test(expected = IOException.class)
    public void testSearch3() throws IOException {
        new WinDesktop(GOOD.registry, UGLY.system, UGLY.search).search("hello");
    }

    //<editor-fold defaultstate="collapsed" desc="Details">
    private static final class Input {

        final WinRegistry registry;
        final File script;
        final ZSystem system;
        final WinSearch search;

        public Input(WinRegistry registry, File script, ZSystem launcher, WinSearch search) {
            this.registry = registry;
            this.script = script;
            this.system = launcher;
            this.search = search;
        }
    }

    private static final class FakeRegistry extends WinRegistry {

        static FakeRegistry create() {
            return new FakeRegistry().putKey(HKEY_LOCAL_MACHINE, DESKTOP_SEARCH_KEY_PATH)
                    .putStringValue(HKEY_CURRENT_USER, SHELL_FOLDERS_KEY_PATH, DESKTOP_DIR, "hello");
        }

        private final Map<Root, Map<String, Map<String, String>>> data;

        public FakeRegistry() {
            this.data = new HashMap<>();
            for (WinRegistry.Root o : WinRegistry.Root.values()) {
                data.put(o, new HashMap<>());
            }
        }

        public FakeRegistry putKey(Root root, String key) {
            data.get(root).put(key, new HashMap<>());
            return this;
        }

        public FakeRegistry putStringValue(Root root, String key, String value, String xyz) {
            Map<String, String> map = data.get(root).get(key);
            if (map == null) {
                map = new HashMap<>();
                data.get(root).put(key, map);
            }
            map.put(value, xyz);
            return this;
        }

        @Override
        public boolean keyExists(Root root, String key) throws IOException {
            return data.get(root).containsKey(key);
        }

        @Override
        public Object getValue(Root root, String key, String value) throws IOException {
            return data.get(root).get(key).get(value);
        }

        @Override
        public SortedMap<String, Object> getValues(Root root, String key) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static final class FakeLauncher extends ZSystem {

        @Override
        public String getProperty(String key) throws SecurityException, IllegalArgumentException, NullPointerException {
            return null;
        }

        @Override
        public Process exec(String... cmdArray) throws IOException {
            return new Process() {

                @Override
                public OutputStream getOutputStream() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public InputStream getInputStream() {
                    return new ByteArrayInputStream("hello.html".getBytes(StandardCharsets.UTF_8));
                }

                @Override
                public InputStream getErrorStream() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public int waitFor() throws InterruptedException {
                    return 0;
                }

                @Override
                public int exitValue() {
                    return 0;
                }

                @Override
                public void destroy() {
                }
            };
        }
    }

    private static final class FakeSearch extends WinSearch {

        @Override
        public List<File> getFilesByName(String query) throws IOException {
            return Collections.singletonList(new File("hello.html"));
        }
    }
    //</editor-fold>
}
