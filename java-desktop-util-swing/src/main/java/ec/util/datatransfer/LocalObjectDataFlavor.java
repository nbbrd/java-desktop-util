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
package ec.util.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Flavor that deals with specifics of local objects as described in
 * {@link DataFlavor#javaJVMLocalObjectMimeType}.
 *
 * @author Philippe Charles
 * @param <T>
 * @see https://docs.oracle.com/javase/tutorial/uiswing/dnd/dataflavor.html
 */
public final class LocalObjectDataFlavor<T> extends DataFlavor {

    @Nonnull
    public static <T> LocalObjectDataFlavor<T> of(@Nonnull Class<T> localObjectType) {
        try {
            return new LocalObjectDataFlavor<>(localObjectType);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @lombok.Getter
    private final Class<T> localObjectType;

    private LocalObjectDataFlavor(Class<T> localObjectType) throws ClassNotFoundException {
        super(localObjectMimeTypeOf(localObjectType));
        this.localObjectType = localObjectType;
    }

    @Nonnull
    public Transferable createTransferable(@Nonnull T localObject) {
        return new LocalObjectTransferable(localObject);
    }

    @Nonnull
    public Optional<T> getLocalObject(@Nonnull Transferable t) {
        return DataTransfers.getTransferData(t, this);
    }

    private static String localObjectMimeTypeOf(Class<?> type) {
        return DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + type.getName() + "\"";
    }

    @lombok.RequiredArgsConstructor
    private final class LocalObjectTransferable<T> implements Transferable {

        @lombok.NonNull
        private final T localObject;

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{LocalObjectDataFlavor.this};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return LocalObjectDataFlavor.this.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return localObject;
        }
    }
}