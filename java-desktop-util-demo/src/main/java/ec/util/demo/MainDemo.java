package ec.util.demo;

import ec.util.demo.ext.JDemoPane;
import ec.util.various.swing.BasicSwingLauncher;
import nbbrd.desktop.swing.JMasterDetail;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

public final class MainDemo {

    public static void main(String[] arg) {
        new BasicSwingLauncher()
                .content(MainDemo::create)
                .launch();
    }

    public static Component create() {
        // Ordered registry: name → lazy factory
        LinkedHashMap<String, Supplier<Component>> registry = new LinkedHashMap<>();
        registry.put("BasicFileViewer",          BasicFileViewerDemo::create);
        registry.put("ColorScheme",              ColorSchemeDemo::create);
        registry.put("Desktop",                  DesktopDemo::new);
        registry.put("FontAwesome",              FontAwesomeDemo::new);
        registry.put("FontAwesome2",             FontAwesomeDemo2::new);
        registry.put("JAutoCompletion",          JAutoCompletionDemo::new);
        registry.put("JGrid",                    JGridDemo::new);
        registry.put("JMasterDetail",            JMasterDetailDemo::new);
        registry.put("JRangeSlider",             JRangeSliderDemo::new);
        registry.put("JRangeSlider+TimeSeries",  JRangeSliderTimeSeriesDemo::new);
        registry.put("JListOrdering",            JListOrderingDemo::create);
        registry.put("JListSelection",           JListSelectionDemo::create);
        registry.put("JTimeSeriesChart",         JTimeSeriesChartDemo::new);
        registry.put("JTimeSeriesRendererSupport", JTimeSeriesRendererSupportDemo::new);
        registry.put("SpinningIcon",             SpinningIconDemo::new);
        registry.put("StandardSwingColor",       SwingColorDemo::create);
        registry.put("ToolBarIcon",              ToolBarIconDemo::create);
        registry.put("XPopup",                   XPopupDemo::create);
        registry.put("XTable",                   XTableDemo::new);
        registry.put("Favicon",                  FaviconDemo::create);

        String[] names = registry.keySet().toArray(new String[0]);
        @SuppressWarnings("unchecked")
        Supplier<Component>[] factories = registry.values().toArray(new Supplier[0]);
        Component[] cache = new Component[names.length];

        // Detail panel swaps content on selection
        JPanel detail = new JPanel(new BorderLayout());

        // Master list
        JList<String> masterList = new JList<>(names);
        masterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        masterList.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        masterList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int idx = masterList.getSelectedIndex();
            if (idx < 0) return;
            if (cache[idx] == null) cache[idx] = factories[idx].get();
            detail.removeAll();
            detail.add(cache[idx], BorderLayout.CENTER);
            detail.revalidate();
            detail.repaint();
        });

        // Eagerly show first item
        masterList.setSelectedIndex(0);

        JMasterDetail masterDetail = new JMasterDetail();
        masterDetail.setMasterNode(new JScrollPane(masterList));
        masterDetail.setDetailNode(detail);
        masterDetail.setDetailSide(JMasterDetail.DetailSide.RIGHT);
        masterDetail.setDividerPosition(0.2);

        return JDemoPane.of(masterDetail);
    }
}
