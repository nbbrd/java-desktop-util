/*
 * Copyright 2015 National Bank of Belgium
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
package ec.util.chart.swing;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

/**
 *
 * @author Philippe Charles
 * @since 2.1.0
 */
@ServiceDefinition(
        singleton = true,
        quantifier = Quantifier.MULTIPLE,
        mutability = Mutability.CONCURRENT,
        loaderName = "internal.chart.swing.JFreeChartWriterLoader"
)
public abstract class JFreeChartWriter {

    @NonNull
    abstract public String getMediaType();

    abstract public void writeChart(@NonNull OutputStream stream, @NonNull JFreeChart chart, @NonNegative int width, @NonNegative int height) throws IOException;

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    @ServiceProvider(JFreeChartWriter.class)
    public static final class SvgWriter extends JFreeChartWriter {

        @Override
        public String getMediaType() {
            return "image/svg+xml";
        }

        @Override
        public void writeChart(OutputStream stream, JFreeChart chart, int width, int height) throws IOException {
            Charts.writeChartAsSVG(stream, chart, width, height);
        }
    }

    @ServiceProvider(JFreeChartWriter.class)
    public static final class SvgzWriter extends JFreeChartWriter {

        @Override
        public String getMediaType() {
            return "image/svg+xml-compressed";
        }

        @Override
        public void writeChart(OutputStream stream, JFreeChart chart, int width, int height) throws IOException {
            try (GZIPOutputStream gzip = new GZIPOutputStream(stream)) {
                Charts.writeChartAsSVG(gzip, chart, width, height);
            }
        }
    }

    @ServiceProvider(JFreeChartWriter.class)
    public static final class PngWriter extends JFreeChartWriter {

        @Override
        public String getMediaType() {
            return "image/png";
        }

        @Override
        public void writeChart(OutputStream stream, JFreeChart chart, int width, int height) throws IOException {
            ChartUtilities.writeChartAsPNG(stream, chart, width, height);
        }
    }

    @ServiceProvider(JFreeChartWriter.class)
    public static final class JpegWriter extends JFreeChartWriter {

        @Override
        public String getMediaType() {
            return "image/jpeg";
        }

        @Override
        public void writeChart(OutputStream stream, JFreeChart chart, int width, int height) throws IOException {
            ChartUtilities.writeChartAsJPEG(stream, chart, width, height);
        }
    }
    //</editor-fold>
}
