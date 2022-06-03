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
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Philippe Charles
 */
public class LocalDataTransferTest {

    @Test
    @SuppressWarnings("null")
    public void testFactory() {
        assertThatNullPointerException().isThrownBy(() -> LocalDataTransfer.of(null));
        assertThat(LocalDataTransfer.of(Integer.class).getDataType()).isEqualTo(Integer.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testCreateTransferable() throws UnsupportedFlavorException, IOException {
        LocalDataTransfer<Integer> dt = LocalDataTransfer.of(Integer.class);

        assertThatNullPointerException().isThrownBy(() -> dt.createTransferable(null));

        Transferable transferable = dt.createTransferable(123);
        assertThat(transferable).isNotNull();
        assertThat(transferable.getTransferData(dt.getDataFlavor())).isEqualTo(123);
        assertThat(transferable.getTransferDataFlavors()).containsOnly(dt.getDataFlavor());
        assertThatExceptionOfType(UnsupportedFlavorException.class)
                .isThrownBy(() -> transferable.getTransferData(DataFlavor.stringFlavor));
    }

    @Test
    @SuppressWarnings("null")
    public void getLocalObject() {
        LocalDataTransfer<Integer> flavor = LocalDataTransfer.of(Integer.class);

        assertThatNullPointerException().isThrownBy(() -> flavor.getData((Transferable)null));

        assertThat(flavor.getData(flavor.createTransferable(123))).contains(123);
        assertThat(flavor.getData(new StringSelection("hello"))).isEmpty();
    }
}
