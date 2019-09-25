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
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A basic {@link Desktop} implementation based on
 * <code>java.awt.Desktop</code>.
 *
 * @author Philippe Charles
 */
public class AwtDesktop implements Desktop {

    private final java.awt.Desktop awt;
    private final Trash trash;

    public AwtDesktop() {
        this.awt = java.awt.Desktop.getDesktop();
        this.trash = Trash.getDefault();
    }

    @Override
    public boolean isSupported(Desktop.Action action) {
        switch (action) {
            case BROWSE:
                return awt.isSupported(java.awt.Desktop.Action.BROWSE);
            case EDIT:
                return awt.isSupported(java.awt.Desktop.Action.EDIT);
            case MAIL:
                return awt.isSupported(java.awt.Desktop.Action.MAIL);
            case OPEN:
                return awt.isSupported(java.awt.Desktop.Action.OPEN);
            case PRINT:
                return awt.isSupported(java.awt.Desktop.Action.PRINT);
            case SHOW_IN_FOLDER:
                return awt.isSupported(java.awt.Desktop.Action.OPEN);
            case MOVE_TO_TRASH:
                return trash.hasTrash();
            case SEARCH:
                return false;
            case KNOWN_FOLDER_LOOKUP:
                return true;
        }
        return false;
    }

    @Override
    public void open(File file) throws IOException {
        awt.open(file);
    }

    @Override
    public void edit(File file) throws IOException {
        awt.edit(file);
    }

    @Override
    public void print(File file) throws IOException {
        awt.print(file);
    }

    @Override
    public void browse(URI uri) throws IOException {
        awt.browse(uri);
    }

    @Override
    public void mail() throws IOException {
        awt.mail();
    }

    @Override
    public void mail(URI mailtoURI) throws IOException {
        awt.mail(mailtoURI);
    }

    @Override
    public void showInFolder(File file) throws IOException {
        awt.open(file.isDirectory() ? file : file.getParentFile());
    }

    @Override
    public void moveToTrash(File... files) throws IOException {
        trash.moveToTrash(files);
    }

    @Override
    public File getKnownFolderPath(KnownFolder knownFolder) throws IOException {
        return getKnownFolder(knownFolder);
    }

    @Override
    public File getKnownFolder(Desktop.KnownFolder userDir) {
        return null;
    }

    @Override
    public File[] search(String query) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @ServiceProvider(Desktop.Factory.class)
    public static class Factory implements Desktop.Factory {

        @Override
        public Desktop.Factory.SupportType getSupportType(String osArch, String osName, String osVersion) {
            return Desktop.Factory.SupportType.BASIC;
        }

        @Override
        public Desktop create(String osArch, String osName, String osVersion) {
            return new AwtDesktop();
        }
    }

    @Deprecated
    @NonNull
    protected static File checkFile(File file) throws NullPointerException, IllegalArgumentException {
        return Util.checkFileValidation(file);
    }

    @Deprecated
    protected static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ex) {
                // do nothing
            }
        }
    }

    @Deprecated
    @NonNull
    protected static File extractResource(@NonNull String resourceName, @NonNull String filePrefix, @NonNull String fileSuffix) throws IOException {
        return Util.extractResource(resourceName, filePrefix, fileSuffix);
    }

    @Deprecated
    @NonNull
    protected static File[] toFiles(@NonNull Process p, @NonNull Charset charset) throws IOException {
        return Util.toFiles(p, charset);
    }
}
