package ec.util.demo;

import ec.util.demo.ext.JDemoPane;
import ec.util.various.swing.BasicSwingLauncher;

import javax.swing.*;
import java.awt.*;

public final class MainDemo {

    public static void main(String[] arg) {
        new BasicSwingLauncher()
                .content(MainDemo::create)
                .launch();
    }

    public static Component create() {
        JTabbedPane result = new JTabbedPane();
        result.add("BasicFileViewer", BasicFileViewerDemo.create());
        result.add("ColorScheme", ColorSchemeDemo.create());
        result.add("Desktop", new DesktopDemo());
        result.add("FontAwesome", new FontAwesomeDemo());
        result.add("FontAwesome2", new FontAwesomeDemo2());
        result.add("JAutoCompletion", new JAutoCompletionDemo());
        result.add("JGrid", new JGridDemo());
        result.add("JListOrdering", JListOrderingDemo.create());
        result.add("JListSelection", JListSelectionDemo.create());
        result.add("JTimeSeriesChart", new JTimeSeriesChartDemo());
        result.add("JTimeSeriesRendererSupport", new JTimeSeriesRendererSupportDemo());
        result.add("SpinningIcon", new SpinningIconDemo());
        result.add("StandardSwingColor", StandardSwingColorDemo.create());
        result.add("ToolBarIcon", ToolBarIconDemo.create());
        result.add("XPopup", XPopupDemo.create());
        result.add("XTable", new XTableDemo());
        return JDemoPane.of(result);
    }
}
