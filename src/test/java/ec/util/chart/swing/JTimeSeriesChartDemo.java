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
package ec.util.chart.swing;

import static ec.util.chart.swing.JTimeSeriesChartCommand.*;
import ec.util.chart.ColorScheme;
import ec.util.chart.ColorSchemeSupport;
import ec.util.chart.ObsFunction;
import ec.util.chart.ObsIndex;
import ec.util.chart.ObsPredicate;
import ec.util.chart.TimeSeriesChart.Element;
import ec.util.chart.TimeSeriesChart.RendererType;
import ec.util.chart.SeriesFunction;
import ec.util.chart.TimeSeriesChart.CrosshairOrientation;
import ec.util.chart.impl.AndroidColorScheme;
import ec.util.various.swing.BasicSwingLauncher;
import ec.util.various.swing.FontAwesome;
import ec.util.various.swing.JCommand;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import static javax.swing.BorderFactory.createCompoundBorder;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createLineBorder;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Philippe Charles
 */
public final class JTimeSeriesChartDemo extends JPanel {

    public static void main(String[] args) {
        new BasicSwingLauncher()
                .content(JTimeSeriesChartDemo.class)
                .title("Time Series Chart Demo")
                .icons(new Callable<List<? extends Image>>() {
                    @Override
                    public List<? extends Image> call() throws Exception {
                        return FontAwesome.FA_LINE_CHART.getImages(Color.BLACK, 16f, 32f, 64f);
                    }
                })
                .logLevel(Level.FINE)
                .launch();
    }

    private final JTimeSeriesChart chart;
    private final CustomTooltip customTooltip;

    public JTimeSeriesChartDemo() {
        this.chart = new JTimeSeriesChart();
        this.customTooltip = new CustomTooltip();

        chart.setTitle("Some random data");
        chart.setNoDataMessage("No data available ?");
        chart.setSeriesFormatter(new SeriesFunction<String>() {
            @Override
            public String apply(int series) {
                return "Series n°" + (series + 1);
            }
        });
        chart.setObsFormatter(new ObsFunction<String>() {
            @Override
            public String apply(int series, int obs) {
                boolean dash = chart.getDashPredicate().apply(series, obs);
                String value = chart.getValueFormatter().apply(series, obs);
                return "[" + chart.getSeriesFormatter().apply(series)
                        + "]\n" + chart.getPeriodFormatter().apply(series, obs)
                        + " : " + value + (dash ? ("\nForecast") : "");
            }
        });
        chart.setDashPredicate(lastObsPredicate(3));
        chart.setPlotWeights(new int[]{2, 1});

        chart.setPopupMenu(newMenu().getPopupMenu());

        applyRandomData().executeSafely(chart);
        chart.setPlotDispatcher(SeriesFunction.array(0, 0, 1));
        chart.setSeriesRenderer(SeriesFunction.array(RendererType.LINE, RendererType.LINE, RendererType.COLUMN));
        chart.setColorSchemeSupport(SwingColorSchemeSupport.from(new AndroidColorScheme.AndroidDarkColorScheme()));
        chart.setLineThickness(2);

        customTooltip.enable(chart);

        setLayout(new BorderLayout());
        add(chart, BorderLayout.CENTER);
    }

    //<editor-fold defaultstate="collapsed" desc="Menus factories">
    private JMenu newMenu() {
        JMenu result = new JMenu();

        {
            final JMenu moveTo = new JMenu("Move to");
            moveTo.add(new MoveToPlot(0).toAction(chart)).setText("First plot");
            moveTo.add(new MoveToPlot(1).toAction(chart)).setText("Second plot");
            result.add(moveTo);

            final JMenu renderAs = new JMenu("Render as");
            for (RendererType o : chart.getSupportedRendererTypes()) {
                renderAs.add(new SetRenderer(o).toAction(chart)).setText(o.name());
            }
            result.add(renderAs);

            final JSeparator separator = new JSeparator();
            result.add(separator);

            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    boolean hasSelection = !chart.getSeriesSelectionModel().isSelectionEmpty();
                    moveTo.setVisible(hasSelection && chart.getPlotWeights().length > 1);
                    renderAs.setVisible(hasSelection);
                    separator.setVisible(hasSelection);
                }
            };
            r.run();
            chart.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String p = evt.getPropertyName();
                    if (p.equals(JTimeSeriesChart.PLOT_WEIGHTS_PROPERTY)) {
                        r.run();
                    }
                }
            });
            chart.getSeriesSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    r.run();
                }
            });
        }

        JMenu item;

        item = new JMenu("Data");
        item.add(applyRandomData().toAction(chart)).setText("Random data");
        item.add(applyDataset(SomeTimeSeries.getCol1()).toAction(chart)).setText("AutoRangeIncludesZero?");
        result.add(item);

        result.add(selectAll().toAction(chart)).setText("Select all");
        result.add(clearDataset().toAction(chart)).setText("Clear");

        result.addSeparator();
        item = new JMenu("Show element");
        item.add(new JCheckBoxMenuItem(toggleElementVisibility(Element.TITLE).toAction(chart))).setText("Title");
        item.add(new JCheckBoxMenuItem(toggleElementVisibility(Element.LEGEND).toAction(chart))).setText("Legend");
        item.add(new JCheckBoxMenuItem(toggleElementVisibility(Element.AXIS).toAction(chart))).setText("Axis");
        item.add(new JCheckBoxMenuItem(toggleElementVisibility(Element.TOOLTIP).toAction(chart))).setText("Tooltip");
        item.add(new JCheckBoxMenuItem(toggleElementVisibility(Element.CROSSHAIR).toAction(chart))).setText("Crosshair");
        result.add(item);

        item = new JMenu("Color scheme");
        for (ColorScheme o : ServiceLoader.load(ColorScheme.class)) {
            JMenuItem subItem = item.add(new JCheckBoxMenuItem(applyColorScheme(o).toAction(chart)));
            subItem.setText(o.getDisplayName());
            subItem.setIcon(new ColorSchemeIcon(o));
        }
        result.add(item);

        item = new JMenu("Line thickness");
        item.add(new JCheckBoxMenuItem(applyLineThickness(1f).toAction(chart))).setText("Thin");
        item.add(new JCheckBoxMenuItem(applyLineThickness(2f).toAction(chart))).setText("Thick");
        result.add(item);

        item = new JMenu("Crosshair orientation");
        item.add(new JCheckBoxMenuItem(applyCrosshairOrientation(CrosshairOrientation.BOTH).toAction(chart))).setText("Both");
        item.add(new JCheckBoxMenuItem(applyCrosshairOrientation(CrosshairOrientation.HORIZONTAL).toAction(chart))).setText("Horizontal");
        item.add(new JCheckBoxMenuItem(applyCrosshairOrientation(CrosshairOrientation.VERTICAL).toAction(chart))).setText("Vertical");
        result.add(item);

        item = new JMenu("Custom tooltip");
        item.add(new JCheckBoxMenuItem(new CustomTooltipCommand(CustomTooltip.Type.DISABLED).toAction(customTooltip))).setText("Disabled");
        item.add(new JCheckBoxMenuItem(new CustomTooltipCommand(CustomTooltip.Type.ON_MOUSE_OVER).toAction(customTooltip))).setText("On mouse over");
        item.add(new JCheckBoxMenuItem(new CustomTooltipCommand(CustomTooltip.Type.ON_MOUSE_CLICK).toAction(customTooltip))).setText("On mouse click");
        result.add(item);

        item = new JMenu("Highlighter");
        item.add(new JCheckBoxMenuItem(applyObsHighlighter(chart.getObsHighlighter()).toAction(chart))).setText("Active obs");
        item.add(new JCheckBoxMenuItem(applyObsHighlighter(highlightPassive(chart)).toAction(chart))).setText("Passive obs");
        item.add(new JCheckBoxMenuItem(applyObsHighlighter(highlightSerie(chart)).toAction(chart))).setText("Serie");
        item.add(new JCheckBoxMenuItem(applyObsHighlighter(highlightObs(chart)).toAction(chart))).setText("Period");
        item.add(new JCheckBoxMenuItem(applyObsHighlighter(ObsPredicate.alwaysTrue()).toAction(chart))).setText("All");
        item.add(new JCheckBoxMenuItem(applyObsHighlighter(ObsPredicate.alwaysFalse()).toAction(chart))).setText("None");
        result.add(item);

        result.addSeparator();
        result.add(resetZoom().toAction(chart)).setText("Reset zoom");
        result.add(newExportMenu());
        result.add(newConfigureMenu());

        return result;
    }

    private JMenu newExportMenu() {
        JMenu result = new JMenu("Export image to");
        result.add(printImage().toAction(chart)).setText("Printer...");
        result.add(copyImage().toAction(chart)).setText("Clipboard");
        result.add(saveImage().toAction(chart)).setText("File...");
        return result;
    }

    private JMenu newConfigureMenu() {
        JMenu result = new JMenu("Configure");

        result.add(new JCheckBoxMenuItem(applyWeights(1).toAction(chart))).setText("Single chart");
        result.add(new JCheckBoxMenuItem(applyWeights(2, 1).toAction(chart))).setText("Dual chart");

        result.addSeparator();
        result.add(editTitle("New title:").toAction(chart)).setText("Title...");
        result.add(editNoDataMessage("New no-data message:").toAction(chart)).setText("No-data message...");
        result.add(editPeriodFormat("New period format:").toAction(chart)).setText("Period format...");
        result.add(editValueFormat("New value format:").toAction(chart)).setText("Value format...");

        return result;
    }
    //</editor-fold>

    private ObsPredicate lastObsPredicate(final int count) {
        return new ObsPredicate() {
            @Override
            public boolean apply(int series, int obs) {
                return obs >= chart.getDataset().getItemCount(series) - count;
            }
        };
    }

    private static ObsPredicate highlightPassive(final JTimeSeriesChart chart) {
        return new ObsPredicate() {
            @Override
            public boolean apply(int series, int obs) {
                return !chart.getActiveObs().equals(series, obs);
            }
        };
    }

    private static ObsPredicate highlightSerie(final JTimeSeriesChart chart) {
        return new ObsPredicate() {
            @Override
            public boolean apply(int series, int obs) {
                return chart.getActiveObs().getSeries() == series;
            }
        };
    }

    private static ObsPredicate highlightObs(final JTimeSeriesChart chart) {
        return new ObsPredicate() {
            @Override
            public boolean apply(int series, int obs) {
                return chart.getActiveObs().getObs() == obs;
            }
        };
    }

    private static final class MoveToPlot extends SeriesFunctionCommand<Integer> {

        public MoveToPlot(int plotIndex) {
            super(plotIndex, 0);
        }

        @Override
        protected SeriesFunction<Integer> get(JTimeSeriesChart chart) {
            return chart.getPlotDispatcher();
        }

        @Override
        protected void set(JTimeSeriesChart chart, SeriesFunction<Integer> function) {
            chart.setPlotDispatcher(function);
        }
    }

    private static final class SetRenderer extends SeriesFunctionCommand<RendererType> {

        public SetRenderer(RendererType value) {
            super(value, RendererType.LINE);
        }

        @Override
        protected SeriesFunction<RendererType> get(JTimeSeriesChart chart) {
            return chart.getSeriesRenderer();
        }

        @Override
        protected void set(JTimeSeriesChart chart, SeriesFunction<RendererType> function) {
            chart.setSeriesRenderer(function);
        }
    }

    private abstract static class SeriesFunctionCommand<X> extends JTimeSeriesChartCommand {

        final X value;
        final X defaultValue;

        public SeriesFunctionCommand(X value, X defaultValue) {
            this.value = value;
            this.defaultValue = defaultValue;
        }

        @Override
        public void execute(JTimeSeriesChart chart) {
            int seriesCount = chart.getDataset().getSeriesCount();
            List<X> result = new ArrayList<>(seriesCount);
            SeriesFunction<X> function = get(chart);
            ListSelectionModel selectionModel = chart.getSeriesSelectionModel();
            for (int series = 0; series < seriesCount; series++) {
                result.add(selectionModel.isSelectedIndex(series) ? value : function.apply(series));
            }
            set(chart, SeriesFunction.array((X[]) result.toArray()));
        }

        @Override
        public boolean isEnabled(JTimeSeriesChart chart) {
            ListSelectionModel selectionModel = chart.getSeriesSelectionModel();
            if (selectionModel.isSelectionEmpty()) {
                return false;
            }
            return !all(selectionModel, get(chart));
        }

        boolean all(ListSelectionModel selectionModel, SeriesFunction<X> function) {
            int min = selectionModel.getMinSelectionIndex();
            int max = selectionModel.getMaxSelectionIndex();
            for (int series = min; series <= max; series++) {
                if (selectionModel.isSelectedIndex(series)) {
                    X result = function.apply(series);
                    if ((result != null ? result : defaultValue) != value) {
                        return false;
                    }
                }
            }
            return true;
        }

        abstract protected SeriesFunction<X> get(JTimeSeriesChart chart);

        abstract protected void set(JTimeSeriesChart chart, SeriesFunction<X> function);
    }

    private static final class CustomTooltip extends JLabel {

        public static final String TYPE_PROPERTY = "type";

        public enum Type {

            DISABLED, ON_MOUSE_OVER, ON_MOUSE_CLICK
        }

        private Type type;
        private Popup popup;

        public CustomTooltip() {
            this.type = Type.DISABLED;
            this.popup = null;
            addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    switch (evt.getPropertyName()) {
                        case TYPE_PROPERTY:
                            if (popup != null) {
                                popup.hide();
                            }
                            break;
                    }
                }
            });
        }

        public void setType(Type type) {
            Type old = this.type;
            this.type = type != null ? type : Type.DISABLED;
            firePropertyChange(TYPE_PROPERTY, old, this.type);
        }

        private static boolean isValidObs(JTimeSeriesChart chart) {
            return chart.getObsExistPredicate().apply(chart.getActiveObs());
        }

        public void enable(final JTimeSeriesChart chart) {
            updateColors(chart);
            chart.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    switch (evt.getPropertyName()) {
                        case JTimeSeriesChart.ACTIVE_OBS_PROPERTY:
                            if (Type.ON_MOUSE_OVER == type) {
                                updateCustomTooltip(chart, isValidObs(chart));
                            }
                            break;
                        case JTimeSeriesChart.COLOR_SCHEME_SUPPORT_PROPERTY:
                            updateColors(chart);
                            break;
                    }
                }
            });
            chart.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (Type.ON_MOUSE_CLICK == type) {
                        updateCustomTooltip(chart, e.getClickCount() > 1 && isValidObs(chart));
                    }
                }
            });
        }

        private void updateColors(JTimeSeriesChart chart) {
            ColorSchemeSupport<? extends Color> csc = chart.getColorSchemeSupport();
            setOpaque(true);
            setBackground(csc.getBackColor());
            setForeground(csc.getTextColor());
            setBorder(createCompoundBorder(createLineBorder(csc.getGridColor(), 1), createEmptyBorder(5, 5, 5, 5)));
        }

        private void updateCustomTooltip(JTimeSeriesChart chart, boolean visible) {
            if (popup != null) {
                popup.hide();
            }
            if (visible) {
                Point p = MouseInfo.getPointerInfo().getLocation();
                popup = PopupFactory.getSharedInstance().getPopup(chart, getCustomTooltip(chart), p.x + 5, p.y + 5);
                popup.show();
            }
        }

        private Component getCustomTooltip(JTimeSeriesChart chart) {
            ObsIndex o = chart.getActiveObs();
            String serie = chart.getSeriesFormatter().apply(o.getSeries());
            String value = chart.getValueFormatter().apply(o);
            String period = chart.getPeriodFormatter().apply(o);
            boolean forecast = chart.getDashPredicate().apply(o);
            Color color = chart.getColorSchemeSupport().getLineColor(o.getSeries());
            setText("<html><b>" + serie + "</b><br>" + period + ": " + value);
            setIcon(forecast ? FontAwesome.FA_REFRESH.getSpinningIcon(this, color, 24f) : getFA(o.getSeries()).getIcon(color, 24f));
            return this;
        }

        private FontAwesome getFA(int series) {
            FontAwesome[] tmp = FontAwesome.values();
            return tmp[series % tmp.length];
        }
    }

    private static final class CustomTooltipCommand extends JCommand<CustomTooltip> {

        private final CustomTooltip.Type type;

        public CustomTooltipCommand(CustomTooltip.Type type) {
            this.type = type;
        }

        @Override
        public void execute(CustomTooltip component) throws Exception {
            component.setType(type);
        }

        @Override
        public boolean isSelected(CustomTooltip component) {
            return component.type == this.type;
        }

        @Override
        public ActionAdapter toAction(CustomTooltip component) {
            return super.toAction(component).withWeakPropertyChangeListener(component);
        }
    }
}
