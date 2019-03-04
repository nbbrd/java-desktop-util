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

import static ec.util.datatransfer.DataTransfers.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class DataTransfersTest {

    @Test
    @SuppressWarnings("null")
    public void testGetTransferData() {
        assertThatNullPointerException().isThrownBy(() -> getTransferData(null, DataFlavor.stringFlavor));
        assertThatNullPointerException().isThrownBy(() -> getTransferData(new StringSelection("hello"), null));

        assertThat(getTransferData(new StringSelection("hello"), DataFlavor.imageFlavor)).isEmpty();
        assertThat(getTransferData(new StringSelection("hello"), DataFlavor.stringFlavor)).contains("hello");
    }
}
