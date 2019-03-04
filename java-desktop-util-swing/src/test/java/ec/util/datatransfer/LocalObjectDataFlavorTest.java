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
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class LocalObjectDataFlavorTest {

    @Test
    @SuppressWarnings("null")
    public void testFactory() {
        assertThatNullPointerException().isThrownBy(() -> LocalObjectDataFlavor.of(null));
        assertThat(LocalObjectDataFlavor.of(Integer.class).getLocalObjectType()).isEqualTo(Integer.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testCreateTransferable() throws UnsupportedFlavorException, IOException {
        LocalObjectDataFlavor<Integer> flavor = LocalObjectDataFlavor.of(Integer.class);

        assertThatNullPointerException().isThrownBy(() -> flavor.createTransferable(null));

        Transferable transferable = flavor.createTransferable(123);
        assertThat(transferable).isNotNull();
        assertThat(transferable.getTransferData(flavor)).isEqualTo(123);
        assertThat(transferable.getTransferDataFlavors()).containsOnly(flavor);
        assertThatExceptionOfType(UnsupportedFlavorException.class)
                .isThrownBy(() -> transferable.getTransferData(DataFlavor.stringFlavor));
    }

    @Test
    @SuppressWarnings("null")
    public void getLocalObject() {
        LocalObjectDataFlavor<Integer> flavor = LocalObjectDataFlavor.of(Integer.class);

        assertThatNullPointerException().isThrownBy(() -> flavor.getLocalObject(null));

        assertThat(flavor.getLocalObject(flavor.createTransferable(123))).contains(123);
        assertThat(flavor.getLocalObject(new StringSelection("hello"))).isEmpty();
    }
}
