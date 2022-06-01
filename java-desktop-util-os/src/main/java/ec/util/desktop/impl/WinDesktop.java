/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
import ec.util.desktop.Desktop.Action;
import ec.util.desktop.Desktop.KnownFolder;
import static ec.util.desktop.impl.WinRegistry.Root.HKEY_CURRENT_USER;
import static ec.util.desktop.impl.WinRegistry.Root.HKEY_LOCAL_MACHINE;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nbbrd.service.ServiceProvider;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A generic {@link Desktop} implementation for Windows.
 *
 * @author Philippe Charles
 */
public class WinDesktop extends AwtDesktop {

    private static final Logger LOGGER = Logger.getLogger(WinDesktop.class.getName());

    //<editor-fold defaultstate="collapsed" desc="Resources">
    /**
     * http://msdn.microsoft.com/en-us/library/dd378457(v=vs.85).aspx
     */
    static final String SHELL_FOLDERS_KEY_PATH = "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders";
    static final String DESKTOP_SEARCH_KEY_PATH = "Software\\Microsoft\\Windows Desktop Search";
    static final String DESKTOP_DIR = "Desktop";
    static final String DOCUMENTS_DIR = "Personal";
    static final String DOWNLOAD_DIR = "{374DE290-123F-4565-9164-39C4925E467B}";
    static final String MUSIC_DIR = "My Music";
    static final String PICTURES_DIR = "My Pictures";
    static final String PUBLICSHARE_DIR = "{ED4824AF-DCE4-45A8-81E2-FC7965083634}";
    static final String TEMPLATES_DIR = "Templates";
    static final String VIDEOS_DIR = "My Video";
    //
    static final String QUOTE = "\"";
    //</editor-fold>

    @NonNull
    private final WinRegistry registry;
    @NonNull
    private final ZSystem system;
    @NonNull
    private final WinSearch search;

    // VisibleForTesting
    WinDesktop(@NonNull WinRegistry registry, @NonNull ZSystem launcher, @NonNull WinSearch search) {
        this.registry = registry;
        this.search = search;
        this.system = launcher;
    }

    @Override
    public boolean isSupported(@NonNull Action action) {
        switch (action) {
            case SHOW_IN_FOLDER:
                return true;
            case SEARCH:
                return isSearchEngineInstalled(registry);
        }
        return super.isSupported(action);
    }

    @Override
    public void showInFolder(@NonNull File file) throws IOException {
        Util.checkFileValidation(file);
        showInFolder(system, file);
    }

    @Override
    public File getKnownFolderPath(@NonNull KnownFolder knownFolder) throws IOException {
        switch (knownFolder) {
            case DESKTOP:
                return getKnownFolderByName(registry, DESKTOP_DIR);
            case DOCUMENTS:
                return getKnownFolderByName(registry, DOCUMENTS_DIR);
            case DOWNLOAD:
                return getKnownFolderByName(registry, DOWNLOAD_DIR);
            case MUSIC:
                return getKnownFolderByName(registry, MUSIC_DIR);
            case PICTURES:
                return getKnownFolderByName(registry, PICTURES_DIR);
            case PUBLICSHARE:
                return getKnownFolderByName(registry, PUBLICSHARE_DIR);
            case TEMPLATES:
                return getKnownFolderByName(registry, TEMPLATES_DIR);
            case VIDEOS:
                return getKnownFolderByName(registry, VIDEOS_DIR);
        }
        return null;
    }

    @Override
    public File getKnownFolder(@NonNull KnownFolder knownFolder) {
        try {
            return getKnownFolderPath(knownFolder);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "While getting known folder", ex);
            return null;
        }
    }

    @Override
    public File[] search(@NonNull String query) throws IOException {
        if (!isSupported(Action.SEARCH)) {
            throw new UnsupportedOperationException(Action.SEARCH.name());
        }
        return search.search(query);
    }

    @ServiceProvider(Desktop.Factory.class)
    public static class Factory implements Desktop.Factory {

        @Override
        public @NonNull SupportType getSupportType(String osArch, String osName, String osVersion) {
            return osName.startsWith("Windows ") ? SupportType.GENERIC : SupportType.NONE;
        }

        @Override
        public @NonNull Desktop create(String osArch, String osName, String osVersion) {
            WinRegistry registry = WinRegistry.getDefault();
            ZSystem launcher = ZSystem.getDefault();
            WinSearch search = WinSearch.getDefault();
            return new WinDesktop(registry, launcher, search);
        }
    }

    private static void showInFolder(@NonNull ZSystem system, @NonNull File file) throws IOException {
        // http://support.microsoft.com/kb/152457
        system.exec("explorer.exe", "/select,", quote(file.getAbsolutePath()));
    }

    private static boolean isSearchEngineInstalled(@NonNull WinRegistry registry) {
        try {
            return registry.keyExists(HKEY_LOCAL_MACHINE, DESKTOP_SEARCH_KEY_PATH);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "While checking desktop search existence", ex);
            return false;
        }
    }

    @Nullable
    private static File getKnownFolderByName(@NonNull WinRegistry registry, @NonNull String winFolderName) throws IOException {
        Object result = registry.getValue(HKEY_CURRENT_USER, SHELL_FOLDERS_KEY_PATH, winFolderName);
        return result instanceof String && !((String) result).isEmpty() ? new File((String) result) : null;
    }

    @NonNull
    private static String quote(@NonNull String input) {
        return QUOTE + input + QUOTE;
    }
}
