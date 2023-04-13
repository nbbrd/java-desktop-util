/*
 * Copyright 2013 National Bank of Belgium
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
package ec.util.demo;

import ec.util.various.swing.BasicFileViewer;
import ec.util.various.swing.BasicFileViewer.BasicFileHandler;
import ec.util.various.swing.BasicSwingLauncher;
import ec.util.various.swing.ModernUI;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * @author Philippe Charles
 */
public final class BasicFileViewerDemo {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .content(BasicFileViewerDemo::create)
                .launch();
    }

    public static Component create() {
        BasicFileViewer result = new BasicFileViewer();
        result.setFileHandler(new TxtFileHandler());
        result.setFailureRenderer(CustomFailureRenderer.INSTANCE);
        return result;
    }

    private static final class TxtFileHandler implements BasicFileHandler {

        private final JTextArea textArea;
        private final Component uniqueViewer;

        public TxtFileHandler() {
            this.textArea = new JTextArea();
            this.uniqueViewer = ModernUI.withEmptyBorders(new JScrollPane(textArea));
        }

        private boolean isTxtFile(File file) {
            return file.getName().toLowerCase(Locale.ROOT).endsWith(".txt");
        }

        @Override
        public Object asyncLoad(File file, BasicFileViewer.ProgressCallback progress) throws Exception {
            if (!isTxtFile(file)) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            String lineSeparator = System.getProperty("line.separator");
            StringBuilder result = new StringBuilder();
            try (Scanner s = new Scanner(file, StandardCharsets.UTF_8.name())) {
                while (s.hasNextLine()) {
                    result.append(s.nextLine()).append(lineSeparator);
                }
            }
            for (int i = 0; i < 100; i++) {
                TimeUnit.MILLISECONDS.sleep(10);
                progress.setProgress(0, 100, i);
            }
            return result.toString();
        }

        @Override
        public boolean isViewer(Component c) {
            return c == uniqueViewer;
        }

        @Override
        public Component borrowViewer(Object data) {
            textArea.setText((String) data);
            textArea.setCaretPosition(0);
            return uniqueViewer;
        }

        @Override
        public void recycleViewer(Component c) {
            textArea.setText("");
        }

        @Override
        public boolean accept(File pathname) {
            // we want to generate exceptions in asyncload
            return !pathname.isDirectory();
        }
    }

    private static final class CustomFailureRenderer implements BasicFileViewer.FailureRenderer {

        public static final CustomFailureRenderer INSTANCE = new CustomFailureRenderer();

        private final JLabel component;

        private CustomFailureRenderer() {
            this.component = new JLabel();
            component.setOpaque(true);
            component.setBackground(Color.BLACK);
            component.setForeground(Color.WHITE);
            component.setIcon(new ImageIcon(CustomFailureRenderer.class.getResource("/ec/util/demo/HAL-9000-icon_128x128.png")));
            component.setHorizontalAlignment(JLabel.CENTER);
            component.setVerticalTextPosition(JLabel.BOTTOM);
            component.setHorizontalTextPosition(JLabel.CENTER);
            component.setIconTextGap(10);
            component.setText("<html><center><code><b>I'm sorry, " + getUserName() + ".<br>I'm afraid I can't do that.");
        }

        @Override
        public Component getFailureComponent(File file, Throwable cause) {
            return component;
        }

        private static String getUserName() {
            return Optional.ofNullable(System.getProperty("user.name")).orElse("Dave");
        }
    }
}
