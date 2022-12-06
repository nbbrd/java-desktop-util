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

import ec.util.demo.ext.DefaultGridCell;
import ec.util.demo.ext.DefaultGridModel;
import ec.util.grid.swing.JGrid;
import ec.util.various.swing.BasicSwingLauncher;
import ec.util.various.swing.FontAwesome;
import lombok.NonNull;
import nbbrd.desktop.favicon.*;
import nbbrd.desktop.favicon.spi.FaviconSupplier;
import nbbrd.desktop.favicon.spi.FaviconSupplierLoader;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.extern.java.Log
public final class FaviconDemo {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .content(FaviconDemo::create)
                .title("Favicon Demo")
                .size(500, 500)
                .logLevel(Level.FINE)
                .launch();
    }

    static Component create() {
        JTabbedPane result = new JTabbedPane();
        result.add("Local", createGrid(getLocalRefs(), getLocalSupports()));
        result.add("Remote", createGrid(getRemoteRefs(), getRemoteSupports()));
        return result;
    }

    private static JGrid createGrid(List<FaviconRef> refs, List<FaviconSupport> supports) {
        JGrid grid = new JGrid();

        grid.setModel(DefaultGridModel
                .builder(FaviconRef.class, FaviconSupport.class)
                .rows(refs)
                .rowName(FaviconDemo::getRowName)
                .columns(supports)
                .columnName(FaviconDemo::getColumnName)
                .build());

        grid.setDefaultRenderer(DefaultGridCell.class, new FaviconTableCellRenderer());

        grid.setRowSelectionAllowed(true);
        grid.setColumnSelectionAllowed(true);

        return grid;
    }

    private static String getRowName(FaviconRef row) {
        return row.getDomain().toString();
    }

    private static String getColumnName(FaviconSupport column) {
        return column
                .getSuppliers()
                .stream()
                .map(FaviconSupplier::getName)
                .collect(Collectors.joining(", ", "", column.isIgnoreParentDomain() ? "" : " #"));
    }

    private static final class FaviconTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel result = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof DefaultGridCell) {
                result.setText(null);
                result.setIcon(getIcon(table, result, (DefaultGridCell<FaviconRef, FaviconSupport>) value));
                result.setHorizontalAlignment(JLabel.CENTER);
            }
            return result;
        }

        private Icon getIcon(JTable table, JLabel renderer, DefaultGridCell<FaviconRef, FaviconSupport> value) {
            Icon result = value.getColumnValue().get(value.getRowValue(), table);
            return result != null ? result : getFallbackIcon(renderer, value);
        }

        private Icon getFallbackIcon(JLabel renderer, DefaultGridCell<FaviconRef, FaviconSupport> value) {
            return FontAwesome.FA_QUESTION.getIcon(renderer.getForeground(), value.getRowValue().getSize());
        }
    }

    private static List<FaviconRef> getLocalRefs() {
        return Arrays.asList(
                FaviconRef.of(DomainName.parse("s8.nbb.be"), 16),
                FaviconRef.of(DomainName.parse("s16.nbb.be"), 16),
                FaviconRef.of(DomainName.parse("s32.nbb.be"), 16),
                FaviconRef.of(DomainName.parse("s64.nbb.be"), 16)
        );
    }

    private static List<FaviconSupport> getLocalSupports() {
        FaviconListener<String> onMessage = getListener(Function.identity());
        FaviconListener<IOException> onError = getListener(IOException::getMessage);
        return Arrays.asList(
                FaviconSupport
                        .builder()
                        .supplier(LocalFaviconSupplier.builder().name("Local").delayInMillis(0).build())
                        .onAsyncMessage(onMessage)
                        .onAsyncError(onError)
                        .build(),
                FaviconSupport
                        .builder()
                        .supplier(LocalFaviconSupplier.builder().name("Local +2s").delayInMillis(2000).build())
                        .onAsyncMessage(onMessage)
                        .onAsyncError(onError)
                        .build()
        );
    }

    @lombok.Value
    @lombok.Builder
    private static class LocalFaviconSupplier implements FaviconSupplier {

        @NonNull String name;

        @NonNegative long delayInMillis;

        @Override
        public int getRank() {
            return 0;
        }

        @Override
        public @Nullable Image getFaviconOrNull(@NonNull FaviconRef ref, @NonNull URLConnectionFactory client) throws IOException {
            try {
                Thread.sleep(delayInMillis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            String size = ref.getDomain().getPart(0);
            try (InputStream stream = FaviconDemo.class.getResourceAsStream("nbb.be_" + size + ".png")) {
                return ImageIO.read(Objects.requireNonNull(stream));
            }
        }
    }

    private static List<FaviconRef> getRemoteRefs() {
        return Stream.of(
                        "explore.data.abs.gov.au",
                        "www.bundesbank.de",
                        "stats.bis.org",
                        "camstat.nis.gov.kh",
                        "sdw.ecb.europa.eu",
                        "dataexplorer.unescap.org",
                        "ec.europa.eu",
                        "ilostat.ilo.org",
                        "data.imf.org",
                        "sdmx.snieg.mx",
                        "www.insee.fr",
                        "www.istat.it",
                        "www.norges-bank.no",
                        "stat.nbb.be",
                        "stats.oecd.org",
                        "andmebaas.stat.ee",
                        "registry.sdmx.org",
                        "datasimel.mtps.gob.sv",
                        "stats.pacificdata.org",
                        "www150.statcan.gc.ca",
                        "lustat.statec.lu",
                        "statfin.stat.fi",
                        "oshub.nso.go.th",
                        "data.uis.unesco.org",
                        "stats2.digitalresources.jisc.ac.uk",
                        "data.un.org",
                        "data.worldbank.org",
                        "wits.worldbank.org"
                ).map(domain -> FaviconRef.of(DomainName.parse(domain), 16))
                .collect(Collectors.toList());
    }

    private static List<FaviconSupport> getRemoteSupports() {
        FaviconListener<String> onMessage = getListener(Function.identity());
        FaviconListener<IOException> onError = getListener(IOException::getMessage);
        Stream<FaviconSupport> first = FaviconSupplierLoader.load()
                .stream()
                .map(supplier -> FaviconSupport
                        .builder()
                        .supplier(supplier)
                        .ignoreParentDomain(true)
                        .onAsyncMessage(onMessage)
                        .onAsyncError(onError)
                        .build());
        Stream<FaviconSupport> second = Stream.of(FaviconSupport.ofServiceLoader());
        return Stream.concat(first, second).collect(Collectors.toList());
    }

    private static <T> FaviconListener<T> getListener(Function<T, String> toString) {
        return (host, supplier, value) -> System.out.printf("%s(%s): %s%n", host, supplier, toString.apply(value));
    }
}
