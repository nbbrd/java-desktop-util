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
package ec.util.various.swing;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A Swing launcher that allows fast GUI prototyping by handling tedious code
 * and using a fluent API.
 *
 * @author Philippe Charles
 */
public final class BasicSwingLauncher {

    private static final Logger LOGGER = Logger.getLogger(BasicSwingLauncher.class.getName());
    private String lookAndFeelClassName = null;
    private String title = null;
    private Dimension size = null;
    private Callable<? extends Component> contentSupplier = null;
    private boolean centerOnScreen = true;
    private Callable<? extends List<? extends Image>> iconsSupplier = null;
    private boolean resizable = true;

    //<editor-fold defaultstate="collapsed" desc="Options setters">
    @NonNull
    public BasicSwingLauncher logLevel(@Nullable Level level) {
        LOGGER.setLevel(level);
        return this;
    }

    @NonNull
    public BasicSwingLauncher systemLookAndFeel() {
        return lookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }

    @NonNull
    public BasicSwingLauncher lookAndFeel(@Nullable String lookAndFeelClassName) {
        this.lookAndFeelClassName = lookAndFeelClassName;
        return this;
    }

    @NonNull
    public BasicSwingLauncher title(@Nullable String title) {
        this.title = title;
        return this;
    }

    @NonNull
    public BasicSwingLauncher size(int width, int height) {
        return size(new Dimension(width, height));
    }

    @NonNull
    public BasicSwingLauncher size(@Nullable Dimension size) {
        this.size = size;
        return this;
    }

    @NonNull
    public BasicSwingLauncher content(@Nullable Class<? extends Component> contentClass) {
        return content(contentClass == null ? null : () -> contentClass.getDeclaredConstructor().newInstance());
    }

    @NonNull
    public BasicSwingLauncher content(@Nullable Callable<? extends Component> contentSupplier) {
        this.contentSupplier = contentSupplier;
        return this;
    }

    @NonNull
    public BasicSwingLauncher centerOnScreen(boolean centerOnScreen) {
        this.centerOnScreen = centerOnScreen;
        return this;
    }

    @NonNull
    public BasicSwingLauncher resizable(boolean resizable) {
        this.resizable = resizable;
        return this;
    }

    @NonNull
    public BasicSwingLauncher icons(@NonNull String... iconsPaths) {
        return icons(newImageList(iconsPaths));
    }

    @NonNull
    public BasicSwingLauncher icons(@Nullable Callable<? extends List<? extends Image>> iconsSupplier) {
        this.iconsSupplier = iconsSupplier;
        return this;
    }
    //</editor-fold>

    /**
     * Launch the application in a new frame with all the configured options.
     */
    public void launch() {
        launch(lookAndFeelClassName != null ? lookAndFeelClassName : UIManager.getSystemLookAndFeelClassName(),
                title != null ? title : "SimpleApp",
                size != null ? size : new Dimension(800, 600),
                contentSupplier != null ? contentSupplier : JPanel::new, iconsSupplier != null ? iconsSupplier : newImageList(),
                centerOnScreen, resizable);
    }

    private static void launch(
            @NonNull final String lookAndFeelClassName,
            @NonNull final String title,
            @NonNull final Dimension size,
            @NonNull final Callable<? extends Component> contentSupplier,
            @NonNull final Callable<? extends List<? extends Image>> iconsSupplier,
            final boolean centerOnScreen, final boolean resizable) {

        LOGGER.log(Level.FINE, "lookAndFeelClassName='%s'", lookAndFeelClassName);
        LOGGER.log(Level.FINE, "title='{0}'", title);
        LOGGER.log(Level.FINE, "size='{0}'", size);
        LOGGER.log(Level.FINE, "contentSupplier='{0}'", contentSupplier);
        LOGGER.log(Level.FINE, "centerOnScreen='{0}'", centerOnScreen);

        try {
            UIManager.setLookAndFeel(lookAndFeelClassName);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            LOGGER.log(Level.WARNING, "Cannot set look&feel", ex);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame();
                frame.setTitle(title);
                frame.setIconImages(iconsSupplier.call());
                frame.getContentPane().add(contentSupplier.call());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(size);
                frame.setResizable(resizable);
                if (centerOnScreen) {
                    frame.setLocationRelativeTo(null);
                }
                frame.setVisible(true);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Cannot launch app", ex);
            }
        });
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    @NonNull
    private static Callable<List<? extends Image>> newImageList(@NonNull final String... iconsPaths) {
        return () -> Arrays.stream(iconsPaths)
                .map(BasicSwingLauncher.class::getResource)
                .filter(Objects::nonNull)
                .map(o -> new ImageIcon(o).getImage())
                .collect(Collectors.toList());
    }
    //</editor-fold>
}
