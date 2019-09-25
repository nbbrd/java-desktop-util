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
package ec.util.chart;

import ec.util.chart.TimeSeriesChart.CrosshairOrientation;
import ec.util.chart.TimeSeriesChart.DisplayTrigger;
import ec.util.chart.TimeSeriesChart.RendererType;
import ec.util.chart.TimeSeriesChart.Element;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Defines a command pattern on a time series chart.
 *
 * @author Philippe Charles
 */
public abstract class TimeSeriesChartCommand {

    /**
     * Executes this command on the specified time series chart.
     *
     * @param chart the input chart
     */
    abstract public void execute(@NonNull TimeSeriesChart chart);

    /**
     * Checks if this command should be enabled with the specified time series
     * chart.
     *
     * @param chart the input chart
     * @return true if enabled; false otherwise
     */
    public boolean isEnabled(@NonNull TimeSeriesChart chart) {
        return true;
    }

    /**
     * Checks if this command should be marked as selected with the specified
     * time series chart.
     *
     * @param chart the input chart
     * @return true if selected; false otherwise
     */
    public boolean isSelected(@NonNull TimeSeriesChart chart) {
        return false;
    }

    /**
     * Creates a time series chart command that resets all the properties.
     *
     * @return a non-null command
     */
    @NonNull
    public static TimeSeriesChartCommand reset() {
        return new TimeSeriesChartCommand() {
            @Override
            public void execute(TimeSeriesChart chart) {
                chart.setDataset(null);
                chart.setColorSchemeSupport(null);
                chart.setTitle(null);
                chart.setNoDataMessage(null);
                for (Element o : Element.values()) {
                    chart.setElementVisible(o, true);
                }
                chart.setPlotWeights(null);
                chart.setPlotDispatcher(null);
                chart.setLineThickness(1f);
                chart.setPeriodFormat(null);
                chart.setValueFormat(null);
                chart.setSeriesRenderer(null);
                chart.setSeriesFormatter(null);
                chart.setObsFormatter(null);
                chart.setDashPredicate(null);
                chart.setLegendVisibilityPredicate(null);
                chart.setCrosshairOrientation(null);
                chart.setHoveredObs(null);
                chart.setObsHighlighter(null);
            }
        };
    }

    /**
     * Creates a time series chart command that clears all the data.
     *
     * @return a non-null command
     */
    @NonNull
    public static TimeSeriesChartCommand clearDataset() {
        return CLEAR;
    }

    /**
     * Creates a time series chart command that toggles the visibility of an
     * element.
     *
     * @param element the element to modify
     * @return a non-null command
     */
    @NonNull
    public static TimeSeriesChartCommand toggleElementVisibility(@NonNull Element element) {
        return EVS.get(element);
    }

    /**
     * Creates a time series chart command that applies a specific line
     * thickness.
     *
     * @param thickness the line thickness to apply
     * @return a non-null command
     */
    @NonNull
    public static TimeSeriesChartCommand applyLineThickness(float thickness) {
        return new QuickCommand("lineThickness", thickness);
    }

    /**
     * Creates a time series chart command that sets the predicate that
     * determines if an observation should be dashed.
     *
     * @param predicate the specified predicate
     * @return a non-null command
     */
    @NonNull
    public static TimeSeriesChartCommand applyDash(@Nullable ObsPredicate predicate) {
        return new QuickCommand("dashPredicate", predicate);
    }

    /**
     * Creates a time series chart command that sets the predicates that
     * determines if a series is shown in the legend.
     *
     * @param predicate the specified predicate
     * @return a non-null command
     */
    @NonNull
    public static TimeSeriesChartCommand applyLegendVisibility(@Nullable SeriesPredicate predicate) {
        return new QuickCommand("legendVisibilityPredicate", predicate);
    }

    @NonNull
    public static TimeSeriesChartCommand applyRenderer(@Nullable SeriesFunction<RendererType> renderer) {
        return new QuickCommand("seriesRenderer", renderer);
    }

    @NonNull
    public static TimeSeriesChartCommand applyRenderer(@NonNull RendererType... typeIndex) {
        return applyRenderer(SeriesFunction.array(typeIndex));
    }

    @NonNull
    public static TimeSeriesChartCommand applyPlotDispatcher(@Nullable SeriesFunction<Integer> plotDispatcher) {
        return new QuickCommand("plotDispatcher", plotDispatcher);
    }

    @NonNull
    public static TimeSeriesChartCommand applyPlotDispatcher(@NonNull Integer... plotIndex) {
        return applyPlotDispatcher(SeriesFunction.array(plotIndex));
    }

    @NonNull
    public static TimeSeriesChartCommand applySeriesFormatter(@Nullable SeriesFunction<String> formatter) {
        return new QuickCommand("seriesFormatter", formatter);
    }

    @NonNull
    public static TimeSeriesChartCommand applySeriesFormatter(@NonNull String... values) {
        return applySeriesFormatter(SeriesFunction.array(values));
    }

    @NonNull
    public static TimeSeriesChartCommand applyPeriod(@Nullable DateFormat periodFormat) {
        return new QuickCommand("periodFormat", periodFormat);
    }

    @NonNull
    public static TimeSeriesChartCommand applyPeriod(String format) {
        return applyPeriod(new SimpleDateFormat(format));
    }

    @NonNull
    public static TimeSeriesChartCommand applyWeights(@NonNull final int... weights) {
        return new TimeSeriesChartCommand() {
            @Override
            public void execute(TimeSeriesChart chart) {
                chart.setPlotWeights(weights);
            }

            @Override
            public boolean isSelected(TimeSeriesChart chart) {
                return Arrays.equals(weights, chart.getPlotWeights());
            }
        };
    }

    @NonNull
    public static TimeSeriesChartCommand applyTitle(@Nullable String title) {
        return new QuickCommand("title", title);
    }

    @NonNull
    public static TimeSeriesChartCommand applyCrosshairOrientation(@NonNull CrosshairOrientation crosshairOrientation) {
        return CTS.get(crosshairOrientation);
    }

    @NonNull
    public static TimeSeriesChartCommand applyObsHighlighter(@Nullable ObsPredicate obsHighlighter) {
        return new QuickCommand("obsHighlighter", obsHighlighter);
    }

    @NonNull
    public static TimeSeriesChartCommand applyTooltipTrigger(@NonNull DisplayTrigger tooltipTrigger) {
        return TTS.get(tooltipTrigger);
    }

    @NonNull
    public static TimeSeriesChartCommand applyCrosshairTrigger(@NonNull DisplayTrigger crosshairTrigger) {
        return XTS.get(crosshairTrigger);
    }

    @NonNull
    public static TimeSeriesChartCommand copyImage() {
        return COPY_IMAGE;
    }

    @NonNull
    public static TimeSeriesChartCommand saveImage() {
        return SAVE_IMAGE;
    }

    @NonNull
    public static TimeSeriesChartCommand printImage() {
        return PRINT_IMAGE;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final class QuickCommand extends TimeSeriesChartCommand {

        private final PropertyDescriptor property;
        private final Object value;

        public QuickCommand(String propertyName, Object value) {
            this.property = lookupProperty(propertyName);
            this.value = value;
        }

        private static PropertyDescriptor lookupProperty(String propertyName) {
            try {
                for (PropertyDescriptor o : Introspector.getBeanInfo(TimeSeriesChart.class).getPropertyDescriptors()) {
                    if (o.getName().equals(propertyName)) {
                        return o;
                    }
                }
            } catch (IntrospectionException ex) {
                throw new RuntimeException(ex);
            }
            throw new IllegalArgumentException(propertyName);
        }

        @Override
        public void execute(TimeSeriesChart chart) {
            try {
                property.getWriteMethod().invoke(chart, value);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public boolean isSelected(TimeSeriesChart chart) {
            try {
                return Objects.equals(property.getReadMethod().invoke(chart), value);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static final TimeSeriesChartCommand CLEAR = new TimeSeriesChartCommand() {
        @Override
        public void execute(TimeSeriesChart chart) {
            chart.setDataset(null);
        }
    };

    private static final Map<Element, TimeSeriesChartCommand> EVS = createEVS();

    private static EnumMap<Element, TimeSeriesChartCommand> createEVS() {
        EnumMap<Element, TimeSeriesChartCommand> result = new EnumMap<>(Element.class);
        for (final Element o : Element.values()) {
            result.put(o, new TimeSeriesChartCommand() {
                @Override
                public void execute(TimeSeriesChart chart) {
                    chart.setElementVisible(o, !chart.isElementVisible(o));
                }

                @Override
                public boolean isSelected(TimeSeriesChart chart) {
                    return chart.isElementVisible(o);
                }
            });
        }
        return result;
    }

    private static <T extends Enum<T>> Map<T, TimeSeriesChartCommand> allOf(Class<T> clazz, String propertyName) {
        EnumMap<T, TimeSeriesChartCommand> result = new EnumMap<>(clazz);
        for (T o : clazz.getEnumConstants()) {
            result.put(o, new QuickCommand(propertyName, o));
        }
        return result;
    }

    private static final Map<CrosshairOrientation, TimeSeriesChartCommand> CTS = allOf(CrosshairOrientation.class, "crosshairOrientation");

    private static final Map<DisplayTrigger, TimeSeriesChartCommand> TTS = allOf(DisplayTrigger.class, "tooltipTrigger");

    private static final Map<DisplayTrigger, TimeSeriesChartCommand> XTS = allOf(DisplayTrigger.class, "crosshairTrigger");

    private static final TimeSeriesChartCommand COPY_IMAGE = new TimeSeriesChartCommand() {
        @Override
        public void execute(TimeSeriesChart chart) {
            try {
                chart.copyImage();
            } catch (IOException ex) {
                Logger.getLogger(TimeSeriesChartCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

    private static final TimeSeriesChartCommand SAVE_IMAGE = new TimeSeriesChartCommand() {
        @Override
        public void execute(TimeSeriesChart chart) {
            try {
                chart.saveImage();
            } catch (IOException ex) {
                Logger.getLogger(TimeSeriesChartCommand.class.getName()).warning(ex.getMessage());
            }
        }
    };

    private static final TimeSeriesChartCommand PRINT_IMAGE = new TimeSeriesChartCommand() {
        @Override
        public void execute(TimeSeriesChart chart) {
            try {
                chart.printImage();
            } catch (IOException ex) {
                Logger.getLogger(TimeSeriesChartCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };
    //</editor-fold>
}
