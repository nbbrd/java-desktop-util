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

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.EnumSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Defines the features of a time series chart.
 *
 * @author Philippe Charles
 * @param <DS> the type of the data source
 * @param <COLOR> the type of the color class
 */
public interface TimeSeriesChart<DS, COLOR> {

    @NonNull
    DS getDataset();

    @NonNull
    ColorSchemeSupport<? extends COLOR> getColorSchemeSupport();

    @NonNull
    String getTitle();

    @NonNull
    String getNoDataMessage();

    boolean isElementVisible(@NonNull Element element);

    @NonNull
    int[] getPlotWeights();

    @NonNull
    SeriesFunction<Integer> getPlotDispatcher();

    float getLineThickness();

    @NonNull
    DateFormat getPeriodFormat();

    @NonNull
    NumberFormat getValueFormat();

    @NonNull
    SeriesFunction<RendererType> getSeriesRenderer();

    @NonNull
    SeriesFunction<String> getSeriesFormatter();

    @NonNull
    SeriesFunction<COLOR> getSeriesColorist();

    @NonNull
    ObsFunction<String> getObsFormatter();

    @NonNull
    ObsFunction<COLOR> getObsColorist();

    @NonNull
    ObsPredicate getDashPredicate();

    @NonNull
    SeriesPredicate getLegendVisibilityPredicate();

    @NonNull
    CrosshairOrientation getCrosshairOrientation();

    @NonNull
    ObsIndex getHoveredObs();

    @NonNull
    ObsIndex getSelectedObs();

    @NonNull
    ObsPredicate getObsHighlighter();

    @NonNull
    DisplayTrigger getTooltipTrigger();

    @NonNull
    DisplayTrigger getCrosshairTrigger();

    void setDataset(@Nullable DS dataset);

    void setColorSchemeSupport(@Nullable ColorSchemeSupport<? extends COLOR> colorSchemeSupport);

    void setTitle(@Nullable String title);

    void setNoDataMessage(@Nullable String noDataMessage);

    void setElementVisible(@NonNull Element element, boolean visible);

    void setPlotWeights(@Nullable int[] weights);

    void setPlotDispatcher(@Nullable SeriesFunction<Integer> plotDispatcher);

    void setLineThickness(float lineThickness);

    void setPeriodFormat(@Nullable DateFormat periodFormat);

    void setValueFormat(@Nullable NumberFormat valueFormat);

    void setSeriesRenderer(@Nullable SeriesFunction<RendererType> renderer);

    void setSeriesFormatter(@Nullable SeriesFunction<String> formatter);

    void setSeriesColorist(@Nullable SeriesFunction<COLOR> colorist);

    void setObsFormatter(@Nullable ObsFunction<String> formatter);

    void setObsColorist(@Nullable ObsFunction<COLOR> colorist);

    void setDashPredicate(@Nullable ObsPredicate predicate);

    void setLegendVisibilityPredicate(@Nullable SeriesPredicate predicate);

    void setCrosshairOrientation(@Nullable CrosshairOrientation crosshairOrientation);

    void setHoveredObs(@Nullable ObsIndex hoveredObs);

    void setSelectedObs(@Nullable ObsIndex selectedObs);

    void setObsHighlighter(@Nullable ObsPredicate obsHighlighter);

    void setTooltipTrigger(@Nullable DisplayTrigger tooltipTrigger);

    void setCrosshairTrigger(@Nullable DisplayTrigger crosshairTrigger);

    void copyImage() throws IOException;

    void saveImage() throws IOException;

    void printImage() throws IOException;

    void writeImage(@NonNull String mediaType, @NonNull OutputStream stream) throws IOException;

    @NonNull
    EnumSet<RendererType> getSupportedRendererTypes();

    enum Element {

        TITLE, LEGEND, AXIS, TOOLTIP, CROSSHAIR
    }

    enum RendererType {

        LINE, STACKED_LINE,
        SPLINE, STACKED_SPLINE,
        COLUMN, STACKED_COLUMN,
        AREA, STACKED_AREA,
        MARKER
    }

    enum CrosshairOrientation {

        HORIZONTAL, VERTICAL, BOTH;
    }

    enum DisplayTrigger {

        HOVERING, SELECTION, BOTH
    }
}
