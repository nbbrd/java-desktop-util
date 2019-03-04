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
package _demo;

import ec.util.various.swing.BasicSwingLauncher;
import internal.SpinningIcon;
import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SpinningIconDemo {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .content(SpinningIconDemo::create)
                .launch();
    }

    private JComponent create() {
        JLabel result = new JLabel();
        result.setText("Some icon");
        result.setIcon(new SpinningIcon(result, FontIcon.of(MaterialDesign.MDI_AUTORENEW, 30, Color.BLACK)));
        return result;
    }
}
