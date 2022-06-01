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
package ec.util.desktop;

import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * An Desktop implementation that does nothing.<br>It is used as a safe fallback
 * if no suitable implementation is available.
 *
 * @author Philippe Charles
 */
public final class NoOpDesktop implements Desktop {

    @Override
    public boolean isSupported(Desktop.@NonNull Action action) {
        return false;
    }

    @Override
    public void open(@NonNull File file) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void edit(@NonNull File file) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void print(@NonNull File file) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void browse(@NonNull URI uri) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mail() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mail(@NonNull URI mailtoURI) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void showInFolder(@NonNull File file) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void moveToTrash(File @NonNull ... files) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public File getKnownFolderPath(@NonNull KnownFolder knownFolder) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public File getKnownFolder(Desktop.@NonNull KnownFolder knownFolder) {
        return null;
    }

    @Override
    public File[] search(@NonNull String query) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
