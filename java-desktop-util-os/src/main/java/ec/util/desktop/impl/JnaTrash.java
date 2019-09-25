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

import com.sun.jna.platform.FileUtils;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Philippe Charles
 */
final class JnaTrash extends Trash {

    private final FileUtils utils = FileUtils.getInstance();

    @Override
    public boolean hasTrash() {
        return utils.hasTrash();
    }

    @Override
    public void moveToTrash(File... files) throws IOException {
        if (!hasTrash()) {
            throw new UnsupportedOperationException();
        }
        utils.moveToTrash(files);
    }
}
