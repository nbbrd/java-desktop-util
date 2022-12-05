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

import ec.util.grid.swing.AbstractGridModel;
import ec.util.grid.swing.JGrid;
import ec.util.various.swing.BasicSwingLauncher;
import ec.util.various.swing.FontAwesome;
import lombok.NonNull;
import nbbrd.desktop.favicon.DomainName;
import nbbrd.desktop.favicon.FaviconListener;
import nbbrd.desktop.favicon.FaviconRef;
import nbbrd.desktop.favicon.FaviconSupport;
import nbbrd.desktop.favicon.spi.FaviconSupplier;
import nbbrd.desktop.favicon.spi.FaviconSupplierLoader;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
                .size(300, 400)
                .logLevel(Level.FINE)
                .launch();
    }

    @lombok.Value
    static class Favicon {
        @NonNull FaviconRef ref;
        @NonNull FaviconSupport faviconSupport;

        public Icon getIcon(Component listener) {
            return faviconSupport.get(ref, listener);
        }
    }

    static Component create() {
        JGrid grid = new JGrid();

        grid.setModel(new AbstractGridModel() {
            final DomainName[] domainNames = getDomainNames();
            final FaviconSupport[] faviconSupports = getFaviconSupports(
                    getListener(Function.identity()), getListener(IOException::getMessage)
            );

            @Override
            public int getRowCount() {
                return domainNames.length;
            }

            @Override
            public int getColumnCount() {
                return faviconSupports.length;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return new Favicon(FaviconRef.of(domainNames[rowIndex], 16), faviconSupports[columnIndex]);
            }

            @Override
            public String getRowName(int rowIndex) {
                return domainNames[rowIndex].toString();
            }

            @Override
            public String getColumnName(int column) {
                return faviconSupports[column]
                        .getSuppliers()
                        .stream()
                        .map(FaviconSupplier::getName)
                        .collect(Collectors.joining(", ", "", faviconSupports[column].isIgnoreParentDomain() ? "" : " ++"));
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Favicon.class;
            }
        });

        grid.setDefaultRenderer(Favicon.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel result = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                result.setText(null);
                result.setIcon(getIcon(table, result, (Favicon) value));
                result.setHorizontalAlignment(JLabel.CENTER);
                return result;
            }

            private Icon getIcon(JTable table, JLabel renderer, Favicon value) {
                Icon result = value.getIcon(table);
                return result != null ? result : getFallbackIcon(renderer);
            }

            private Icon getFallbackIcon(JLabel renderer) {
                return FontAwesome.FA_QUESTION.getIcon(renderer.getForeground(), renderer.getFont().getSize2D());
            }
        });

        grid.setRowSelectionAllowed(true);
        grid.setColumnSelectionAllowed(true);

        return grid;
    }

    private static <T> FaviconListener<T> getListener(Function<T, String> toString) {
        return (host, supplier, value) -> log.info(String.format("%s(%s): %s", host, supplier, toString.apply(value)));
    }

    private static FaviconSupport[] getFaviconSupports(
            FaviconListener<String> onMessage,
            FaviconListener<IOException> onError
    ) {
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
        return Stream.concat(first, second).toArray(FaviconSupport[]::new);
    }

    private static DomainName[] getDomainNames() {
        return new DomainName[]{
                parseHost("https://explore.data.abs.gov.au"),
                parseHost("https://www.bundesbank.de/en/statistics/time-series-databases"),
                parseHost("https://stats.bis.org/statx/toc/LBS.html"),
                parseHost("http://camstat.nis.gov.kh/?locale=en&start=0"),
                parseHost("https://sdw.ecb.europa.eu"),
                parseHost("https://dataexplorer.unescap.org/"),
                parseHost("https://ec.europa.eu/eurostat/data/database"),
                parseHost("https://ilostat.ilo.org/data/"),
                parseHost("https://data.imf.org"),
                parseHost("https://sdmx.snieg.mx"),
                parseHost("https://www.insee.fr/fr/statistiques"),
                parseHost("https://www.istat.it/en/analysis-and-products"),
                parseHost("https://www.norges-bank.no/en/topics/Statistics/"),
                parseHost("https://stat.nbb.be"),
                parseHost("https://stats.oecd.org"),
                parseHost("http://andmebaas.stat.ee"),
                parseHost("https://registry.sdmx.org/overview.html"),
                parseHost("https://datasimel.mtps.gob.sv/"),
                parseHost("https://stats.pacificdata.org/?locale=en"),
                parseHost("https://www150.statcan.gc.ca/n1/en/type/data?MM=1"),
                parseHost("https://lustat.statec.lu"),
                parseHost("https://statfin.stat.fi/PxWeb/pxweb/en/StatFin/"),
                parseHost("https://oshub.nso.go.th/?lc=en"),
                parseHost("http://data.uis.unesco.org"),
                parseHost("https://stats2.digitalresources.jisc.ac.uk/"),
                parseHost("https://data.un.org/SdmxBrowser/start"),
                parseHost("https://data.worldbank.org"),
                parseHost("https://wits.worldbank.org")
        };
    }

    private static DomainName parseHost(String text) {
        try {
            return DomainName.of(new URL(text));
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
