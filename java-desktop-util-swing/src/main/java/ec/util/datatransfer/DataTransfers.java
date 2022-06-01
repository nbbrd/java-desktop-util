/*
 * Copyright 2018 National Bank of Belgium
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
package ec.util.datatransfer;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import lombok.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public final class DataTransfers {

    @NonNull
    public <T> Optional<T> getTransferData(@NonNull Transferable t, @NonNull DataFlavor flavor) {
        if (t.isDataFlavorSupported(flavor)) {
            try {
                return Optional.of((T) t.getTransferData(flavor));
            } catch (UnsupportedFlavorException | ClassCastException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                // data no more available
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @NonNull
    public Transferable systemClipboardAsTransferable() {
        return new ClipboardAsTransferable(Toolkit.getDefaultToolkit().getSystemClipboard());
    }

    /**
     * Provides a way to avoid use of method
     * {@link Clipboard#getContents(java.lang.Object)} that might throw
     * OutOfMemoryError.
     */
    @lombok.extern.java.Log
    @lombok.RequiredArgsConstructor
    private static final class ClipboardAsTransferable implements Transferable {

        @lombok.NonNull
        private final Clipboard clipboard;

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            try {
                return clipboard.getAvailableDataFlavors();
            } catch (IllegalStateException ex) {
                log.log(Level.WARNING, "While getting data flavors from clipboard", ex);
                return new DataFlavor[0];
            }
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            try {
                return clipboard.isDataFlavorAvailable(flavor);
            } catch (IllegalStateException ex) {
                log.log(Level.WARNING, "While checking data flavor from clipboard", ex);
                return false;
            }
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            try {
                return clipboard.getData(flavor);
            } catch (IllegalStateException | OutOfMemoryError ex) {
                log.log(Level.WARNING, "While getting data from clipboard", ex);
                return new IOException(ex);
            }
        }
    }
}
