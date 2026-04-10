/*
 * Copyright 2026 National Bank of Belgium
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

import ec.util.chart.TimeSeriesChart;
import ec.util.chart.swing.JTimeSeriesChart;
import ec.util.demo.ext.JDemoPane;
import ec.util.various.swing.BasicSwingLauncher;
import nbbrd.desktop.swing.JRangeSlider;
import org.jfree.data.time.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Demo combining {@link JRangeSlider} with {@link JTimeSeriesChart}.
 * The range slider at the bottom controls which portion of the time series
 * is visible in the main chart (observation-index based zoom).
 *
 * @author Philippe Charles
 */
public final class JRangeSliderTimeSeriesDemo extends JPanel {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .content(() -> JDemoPane.of(new JRangeSliderTimeSeriesDemo()))
                .title("RangeSlider + TimeSeriesChart Demo")
                .size(750, 420)
                .launch();
    }

    private final JTimeSeriesChart chart;
    private final JRangeSlider slider;
    private final TimeSeriesCollection fullDataset;

    public JRangeSliderTimeSeriesDemo() {
        this.chart = new JTimeSeriesChart();
        this.fullDataset = createDataset();
        int obsCount = fullDataset.getSeries(0).getItemCount(); // 60

        this.slider = new JRangeSlider();
        slider.setMinimum(0);
        slider.setMaximum(obsCount - 1);
        slider.setLowValue(0);
        slider.setHighValue(obsCount - 1);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(12); // yearly ticks
        slider.setMinorTickSpacing(3);  // quarterly ticks

        // Chart setup
        chart.setElementVisible(TimeSeriesChart.Element.TITLE, false);
        chart.setElementVisible(TimeSeriesChart.Element.LEGEND, true);
        chart.setDataset(subDataset(fullDataset, 0, obsCount - 1));

        // ---- Top toolbar ----
        // Right: range label
        JLabel rangeLabel = new JLabel(formatPeriodRange(fullDataset, 0, obsCount - 1));
        rangeLabel.setFont(rangeLabel.getFont().deriveFont(Font.PLAIN));
        rangeLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        rangeLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));

        // Left: quick-range toggle buttons
        ButtonGroup group = new ButtonGroup();
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        int[][] ranges = {
                {obsCount - 12, obsCount - 1},   // 1Y
                {obsCount - 36, obsCount - 1},   // 3Y
                {obsCount - 60, obsCount - 1},   // 5Y
                {0,             obsCount - 1}    // Full
        };
        String[] labels = {"1Y", "3Y", "5Y", "Full"};

        for (int i = 0; i < labels.length; i++) {
            int low  = Math.max(0, ranges[i][0]);
            int high = ranges[i][1];
            JToggleButton btn = new JToggleButton(labels[i]);
            btn.putClientProperty("low",  low);
            btn.putClientProperty("high", high);
            btn.putClientProperty("JButton.buttonType", "toolBarButton");
            btn.addActionListener(e -> {
                slider.setHighValue(high);
                slider.setLowValue(low);
            });
            group.add(btn);
            btnPanel.add(btn);
        }

        // Toolbar panel
        JPanel toolbar = new JPanel(new BorderLayout(4, 0));
        toolbar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        toolbar.add(btnPanel,    BorderLayout.WEST);
        toolbar.add(rangeLabel,  BorderLayout.EAST);

        // Wire slider → chart + label + button sync
        slider.addChangeListener(e -> {
            int low  = slider.getLowValue();
            int high = slider.getHighValue();
            chart.setDataset(subDataset(fullDataset, low, high));
            rangeLabel.setText(formatPeriodRange(fullDataset, low, high));
            // Keep the matching quick-range button selected (or clear if none match)
            boolean matched = false;
            for (Enumeration<AbstractButton> btns = group.getElements(); btns.hasMoreElements(); ) {
                AbstractButton btn = btns.nextElement();
                if (btn.getClientProperty("low").equals(low) &&
                        btn.getClientProperty("high").equals(high)) {
                    btn.setSelected(true);
                    matched = true;
                    break;
                }
            }
            if (!matched) group.clearSelection();
        });

        // Select "Full" initially
        group.getElements().nextElement(); // skip 1Y
        group.getElements().nextElement(); // skip 3Y  — iterate properly
        selectButton(group, 0, obsCount - 1);

        // South panel: just the slider
        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 8, 16));
        sliderPanel.add(slider, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(toolbar,     BorderLayout.NORTH);
        add(chart,       BorderLayout.CENTER);
        add(sliderPanel, BorderLayout.SOUTH);
    }

    /** Selects the quick-range button whose low/high match, if any. */
    private static void selectButton(ButtonGroup group, int low, int high) {
        for (Enumeration<AbstractButton> btns = group.getElements(); btns.hasMoreElements(); ) {
            AbstractButton btn = btns.nextElement();
            if (btn.getClientProperty("low").equals(low) &&
                    btn.getClientProperty("high").equals(high)) {
                btn.setSelected(true);
                return;
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Data helpers">

    /** 5 years (60 months) of synthetic random-walk data for 3 series. */
    private static TimeSeriesCollection createDataset() {
        TimeSeriesCollection result = new TimeSeriesCollection();
        result.setXPosition(TimePeriodAnchor.MIDDLE);
        Calendar cal = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        cal.set(2019, Calendar.JANUARY, 1);
        Month start = new Month(cal.getTime());
        Random rng = new Random(42);
        result.addSeries(SomeTimeSeries.newTimeSeries("Series 1", start, randomWalk(rng, 60, 100)));
        result.addSeries(SomeTimeSeries.newTimeSeries("Series 2", start, randomWalk(rng, 60, 80)));
        result.addSeries(SomeTimeSeries.newTimeSeries("Series 3", start, randomWalk(rng, 60, 120)));
        return result;
    }

    private static double[] randomWalk(Random rng, int n, double seed) {
        double[] v = new double[n];
        v[0] = seed;
        for (int i = 1; i < n; i++) {
            v[i] = v[i - 1] + (rng.nextDouble() - 0.48) * 5;
        }
        return v;
    }

    /**
     * Returns a new {@link TimeSeriesCollection} containing only observations
     * [low, high] (inclusive, by index) from each series in {@code source}.
     */
    private static TimeSeriesCollection subDataset(TimeSeriesCollection source, int low, int high) {
        TimeSeriesCollection result = new TimeSeriesCollection();
        result.setXPosition(TimePeriodAnchor.MIDDLE);
        for (int s = 0; s < source.getSeriesCount(); s++) {
            TimeSeries src = source.getSeries(s);
            TimeSeries sub = new TimeSeries(src.getKey());
            int from = Math.max(0, low);
            int to = Math.min(high, src.getItemCount() - 1);
            for (int i = from; i <= to; i++) {
                sub.add(src.getDataItem(i));
            }
            result.addSeries(sub);
        }
        return result;
    }

    private static String formatPeriodRange(TimeSeriesCollection ds, int low, int high) {
        if (ds.getSeriesCount() == 0) return "";
        TimeSeries ts = ds.getSeries(0);
        int n = ts.getItemCount();
        if (n == 0) return "";
        String from = ts.getDataItem(Math.max(0, low)).getPeriod().toString();
        String to = ts.getDataItem(Math.min(high, n - 1)).getPeriod().toString();
        int count = Math.min(high, n - 1) - Math.max(0, low) + 1;
        return from + "  —  " + to + "  (" + count + " obs)";
    }

    // </editor-fold>
}

