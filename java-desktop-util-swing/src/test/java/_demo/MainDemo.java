package _demo;

import _demo.ext.JDemoPane;
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
        result.add("JAutoCompletion", new JAutoCompletionDemo());
        result.add("JListOrdering", JListOrderingDemo.create());
        result.add("JListSelection", JListSelectionDemo.create());
        result.add("SpinningIcon", new SpinningIconDemo());
        result.add("StandardSwingColor", StandardSwingColorDemo.create());
        result.add("ToolBarIcon", ToolBarIconDemo.create());
        result.add("XPopup", XPopupDemo.create());
        result.add("XTable", new XTableDemo());
        result.add("JGrid", new JGridDemo());
        return JDemoPane.of(result);
    }
}
