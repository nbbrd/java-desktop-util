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
package nbbrd.desktop.swing;

import ec.util.various.swing.ModernUI;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;

/**
 * A Swing component that provides a master/detail layout using a split pane.
 * The master view is permanently visible; the detail view can be toggled on or
 * off and placed on any of the four sides of the master.
 *
 * <p>Inspired by ControlsFX {@code MasterDetailPane}. No animations are
 * provided.</p>
 *
 * <p>Bound properties:</p>
 * <ul>
 *   <li>{@link #MASTER_NODE_PROPERTY}</li>
 *   <li>{@link #DETAIL_NODE_PROPERTY}</li>
 *   <li>{@link #DETAIL_SIDE_PROPERTY}</li>
 *   <li>{@link #DETAIL_VISIBLE_PROPERTY}</li>
 *   <li>{@link #DIVIDER_POSITION_PROPERTY}</li>
 * </ul>
 *
 * @author Philippe Charles
 */
public final class JMasterDetail extends JPanel {

    // <editor-fold defaultstate="collapsed" desc="API definition">

    /**
     * The side where the detail node is placed relative to the master node.
     */
    public enum DetailSide {
        TOP, BOTTOM, LEFT, RIGHT
    }

    public static final String MASTER_NODE_PROPERTY = "masterNode";
    public static final String DETAIL_NODE_PROPERTY = "detailNode";
    public static final String DETAIL_SIDE_PROPERTY = "detailSide";
    public static final String DETAIL_VISIBLE_PROPERTY = "detailVisible";
    /**
     * Proportional divider position in the range [0.0, 1.0].
     * 0.0 means the divider is at the beginning, 1.0 at the end.
     */
    public static final String DIVIDER_POSITION_PROPERTY = "dividerPosition";

    // </editor-fold>

    private Component masterNode;
    private Component detailNode;
    private DetailSide detailSide;
    private boolean detailVisible;
    private double dividerPosition;

    private final JSplitPane splitPane;
    private boolean updatingDivider = false;

    public JMasterDetail() {
        this.masterNode = new JPanel();
        this.detailNode = new JPanel();
        this.detailSide = DetailSide.RIGHT;
        this.detailVisible = true;
        this.dividerPosition = 0.5;

        this.splitPane = ModernUI.withEmptyBorders(new JSplitPane());
        this.splitPane.setContinuousLayout(true);
        this.splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
            if (!updatingDivider) {
                Dimension size = splitPane.getSize();
                int total = (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT)
                        ? size.width - splitPane.getDividerSize()
                        : size.height - splitPane.getDividerSize();
                if (total > 0) {
                    double newRatio = Math.max(0.0, Math.min(1.0,
                            (double) splitPane.getDividerLocation() / total));
                    double old = dividerPosition;
                    dividerPosition = newRatio;
                    firePropertyChange(DIVIDER_POSITION_PROPERTY, old, dividerPosition);
                }
            }
        });

        setLayout(new BorderLayout());

        addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case MASTER_NODE_PROPERTY:
                case DETAIL_NODE_PROPERTY:
                case DETAIL_SIDE_PROPERTY:
                case DETAIL_VISIBLE_PROPERTY:
                    onLayoutChange();
                    break;
                case DIVIDER_POSITION_PROPERTY:
                    onDividerPositionChange();
                    break;
            }
        });

        onLayoutChange();
    }

    // <editor-fold defaultstate="collapsed" desc="Event handlers">

    private void onLayoutChange() {
        removeAll();
        if (!detailVisible) {
            add(masterNode, BorderLayout.CENTER);
        } else {
            configureSplitPane();
            add(splitPane, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }

    private void configureSplitPane() {
        switch (detailSide) {
            case TOP:
                splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
                splitPane.setTopComponent(detailNode);
                splitPane.setBottomComponent(masterNode);
                break;
            case BOTTOM:
                splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
                splitPane.setTopComponent(masterNode);
                splitPane.setBottomComponent(detailNode);
                break;
            case LEFT:
                splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                splitPane.setLeftComponent(detailNode);
                splitPane.setRightComponent(masterNode);
                break;
            default: // RIGHT
                splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                splitPane.setLeftComponent(masterNode);
                splitPane.setRightComponent(detailNode);
                break;
        }
        applyDividerPosition();
    }

    private void onDividerPositionChange() {
        if (detailVisible) {
            applyDividerPosition();
        }
    }

    private void applyDividerPosition() {
        updatingDivider = true;
        try {
            splitPane.setResizeWeight(dividerPosition);
            splitPane.setDividerLocation(dividerPosition);
        } finally {
            updatingDivider = false;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">

    /**
     * Returns the master (primary) node component.
     *
     * @return non-null component
     */
    @NonNull
    public Component getMasterNode() {
        return masterNode;
    }

    /**
     * Sets the master (primary) node component.
     *
     * @param masterNode non-null component
     */
    public void setMasterNode(@NonNull Component masterNode) {
        Component old = this.masterNode;
        this.masterNode = masterNode;
        firePropertyChange(MASTER_NODE_PROPERTY, old, this.masterNode);
    }

    /**
     * Returns the detail node component.
     *
     * @return non-null component
     */
    @NonNull
    public Component getDetailNode() {
        return detailNode;
    }

    /**
     * Sets the detail node component.
     *
     * @param detailNode non-null component
     */
    public void setDetailNode(@NonNull Component detailNode) {
        Component old = this.detailNode;
        this.detailNode = detailNode;
        firePropertyChange(DETAIL_NODE_PROPERTY, old, this.detailNode);
    }

    /**
     * Returns the side where the detail node is placed.
     *
     * @return non-null side
     */
    @NonNull
    public DetailSide getDetailSide() {
        return detailSide;
    }

    /**
     * Sets the side where the detail node is placed.
     *
     * @param detailSide non-null side
     */
    public void setDetailSide(@NonNull DetailSide detailSide) {
        DetailSide old = this.detailSide;
        this.detailSide = detailSide;
        firePropertyChange(DETAIL_SIDE_PROPERTY, old, this.detailSide);
    }

    /**
     * Returns whether the detail node is currently visible.
     *
     * @return {@code true} if visible
     */
    public boolean isDetailVisible() {
        return detailVisible;
    }

    /**
     * Shows or hides the detail node. When hidden, only the master node
     * is displayed.
     *
     * @param detailVisible {@code true} to show, {@code false} to hide
     */
    public void setDetailVisible(boolean detailVisible) {
        boolean old = this.detailVisible;
        this.detailVisible = detailVisible;
        firePropertyChange(DETAIL_VISIBLE_PROPERTY, old, this.detailVisible);
    }

    /**
     * Returns the proportional divider position in the range [0.0, 1.0].
     *
     * @return divider position
     */
    public double getDividerPosition() {
        return dividerPosition;
    }

    /**
     * Sets the proportional divider position.
     *
     * @param dividerPosition value in the range [0.0, 1.0]
     * @throws IllegalArgumentException if the value is outside [0.0, 1.0]
     */
    public void setDividerPosition(double dividerPosition) {
        if (dividerPosition < 0.0 || dividerPosition > 1.0) {
            throw new IllegalArgumentException("dividerPosition must be in range [0.0, 1.0]: " + dividerPosition);
        }
        double old = this.dividerPosition;
        this.dividerPosition = dividerPosition;
        firePropertyChange(DIVIDER_POSITION_PROPERTY, old, this.dividerPosition);
    }

    // </editor-fold>
}

