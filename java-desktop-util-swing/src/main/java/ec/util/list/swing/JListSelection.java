/*
 * Copyright 2016 National Bank of Belgium
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
package ec.util.list.swing;

import ec.util.datatransfer.LocalDataTransfer;
import ec.util.various.swing.JCommand;
import ec.util.various.swing.TextPrompt;
import internal.ForwardingIcon;
import internal.InternalUtil;
import internal.ToolBarIcon;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

/**
 *
 * @author Philippe Charles
 * @param <E>
 */
public final class JListSelection<E> extends JComponent {

    public static final String SOURCE_MODEL_PROPERTY = "sourceModel";
    public static final String SOURCE_FOOTER_PROPERTY = "sourceFooter";
    public static final String SOURCE_HEADER_PROPERTY = "sourceHeader";
    public static final String TARGET_MODEL_PROPERTY = "targetModel";
    public static final String TARGET_FOOTER_PROPERTY = "targetFooter";
    public static final String TARGET_HEADER_PROPERTY = "targetHeader";
    public static final String CELL_RENDERER_PROPERTY = "cellRenderer";
    public static final String ORIENTATION_PROPERTY = "orientation";
    public static final String SEARCH_VISIBLE_PROPERTY = "searchVisible";
    public static final String SEARCH_TEXT_PROPERTY = "searchText";
    public static final String SEARCH_TEXT_PROVIDER_PROPERTY = "searchTextProvider";

    public static final String SELECT_ACTION = "select";
    public static final String UNSELECT_ACTION = "unselect";
    public static final String SELECT_ALL_ACTION = "selectAll";
    public static final String UNSELECT_ALL_ACTION = "unselectAll";
    public static final String INVERT_ACTION = "invert";
    public static final String APPLY_HORIZONTAL_ACTION = "applyHorizontal";
    public static final String APPLY_VERTICAL_ACTION = "applyVertical";

    private final JPanel sourcePanel;
    private final JList<E> sourceList;
    private final JPanel targetPanel;
    private final JList<E> targetList;
    private final JPanel listsPanel;
    private final SearchBar searchPanel;
    private final ListDataListener sourceListener;
    private DefaultListModel<E> observedSourceModel;
    private boolean updatingModels;

    private JToolBar toolBar;
    private DefaultListModel<E> sourceModel;
    private Component sourceFooter;
    private Component sourceHeader;
    private DefaultListModel<E> targetModel;
    private Component targetFooter;
    private Component targetHeader;
    private ListCellRenderer<? super E> cellRenderer;
    private int orientation;
    private boolean searchVisible;
    private String searchText;
    private Function<? super E, String> searchTextProvider;

    public JListSelection() {
        this.sourcePanel = new JPanel();
        this.sourceList = createList();
        this.targetPanel = new JPanel();
        this.targetList = createList();
        this.listsPanel = new JPanel();
        this.searchPanel = new SearchBar();
        this.sourceListener = JLists.dataListenerOf(event -> {
            if (!updatingModels) {
                refreshSourceView();
            }
        });

        this.toolBar = new JToolBar();
        this.sourceModel = new DefaultListModel<>();
        this.sourceFooter = null;
        this.sourceHeader = null;
        this.targetModel = new DefaultListModel<>();
        this.targetFooter = null;
        this.targetHeader = null;
        this.cellRenderer = new DefaultListCellRenderer();
        this.orientation = SwingConstants.HORIZONTAL;
        this.searchVisible = false;
        this.searchText = "";
        this.searchTextProvider = String::valueOf;

        initComponents();
        enableProperties();
        enableSelectionOnDoubleClick();
    }

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    private JList<E> createList() {
        JList<E> result = new JList<E>() {
            private final JLabel emptyState = new JLabel("", SwingConstants.CENTER);

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getModel().getSize() == 0) {
                    emptyState.setText(emptyMessage(this));
                    emptyState.setFont(getFont());
                    emptyState.setEnabled(false);
                    SwingUtilities.paintComponent(g, emptyState, this, 0, 0, getWidth(), getHeight());
                }
            }

            @Override
            public String getToolTipText(MouseEvent event) {
                int index = locationToIndex(event.getPoint());
                if (index < 0 || !getCellBounds(index, index).contains(event.getPoint())) {
                    return null;
                }
                String rendered = super.getToolTipText(event);
                return rendered != null && !rendered.isEmpty()
                        ? rendered
                        : searchTextProvider.apply(getModel().getElementAt(index));
            }
        };
        result.setToolTipText("");
        return result;
    }

    private String emptyMessage(JList<?> list) {
        return list == targetList ? "No chosen items" : isFiltering() ? "No matches" : "No available items";
    }

    private void initComponents() {
        ActionMap am = getActionMap();
        am.put(SELECT_ACTION, new TransferCommand<E>(true, false).toAction(this));
        am.put(UNSELECT_ACTION, new TransferCommand<E>(false, false).toAction(this));
        am.put(SELECT_ALL_ACTION, new TransferCommand<E>(true, true).toAction(this));
        am.put(UNSELECT_ALL_ACTION, new TransferCommand<E>(false, true).toAction(this));
        am.put(INVERT_ACTION, new InvertCommand<E>().toAction(this));
        am.put(APPLY_HORIZONTAL_ACTION, new OrientationCommand(SwingConstants.HORIZONTAL).toAction(this));
        am.put(APPLY_VERTICAL_ACTION, new OrientationCommand(SwingConstants.VERTICAL).toAction(this));
        nameAction(SELECT_ACTION, "Add");
        nameAction(UNSELECT_ACTION, "Remove");
        nameAction(SELECT_ALL_ACTION, "Add all");
        nameAction(UNSELECT_ALL_ACTION, "Remove all");
        nameAction(INVERT_ACTION, "Invert");
        sourceList.getAccessibleContext().setAccessibleName("Available items");
        targetList.getAccessibleContext().setAccessibleName("Chosen items");

        searchPanel.initSearch();
        TransferHandler transferHandler = new CustomTransferHandler();
        KeyStroke enterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

        sourceList.setDragEnabled(true);
        sourceList.setDropMode(DropMode.INSERT);
        sourceList.setTransferHandler(transferHandler);
        sourceList.getInputMap().put(enterKeyStroke, SELECT_ACTION);
        sourceList.getActionMap().put(SELECT_ACTION, am.get(SELECT_ACTION));

        sourcePanel.setLayout(new BorderLayout());
        sourcePanel.add(new JScrollPane(sourceList), BorderLayout.CENTER);
        sourcePanel.setPreferredSize(new Dimension(10, 10));

        targetList.setDragEnabled(true);
        targetList.setDropMode(DropMode.INSERT);
        targetList.setTransferHandler(transferHandler);
        targetList.getInputMap().put(enterKeyStroke, UNSELECT_ACTION);
        targetList.getActionMap().put(UNSELECT_ACTION, am.get(UNSELECT_ACTION));

        targetPanel.setLayout(new BorderLayout());
        targetPanel.add(new JScrollPane(targetList), BorderLayout.CENTER);
        targetPanel.setPreferredSize(new Dimension(10, 10));

        toolBar = createToolBar();
        toolBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        toolBar.setFloatable(false);
        Stream.of(sourcePanel, toolBar, targetPanel).forEach(listsPanel::add);
        setLayout(new BorderLayout());
        add(searchPanel, BorderLayout.NORTH);
        add(listsPanel, BorderLayout.CENTER);

        onSourceModelChange();
        onTargetModelChange();
        onCellRendererChange();
        onOrientationChange();
    }

    private final class SearchBar extends JPanel {
        private final JTextField searchField = new JTextField();
        private final JLabel searchStatus = new JLabel();
        private final JButton clearSearch = new JButton("Clear");
        private TextPrompt searchPrompt;

        private SearchBar() {
            super(new BorderLayout(5, 5));
        }

        private void initSearch() {
            searchField.setToolTipText("Filter available items (Ctrl+F to search, Escape to clear)");
            searchField.getAccessibleContext().setAccessibleName("Search available items");
            clearSearch.setToolTipText("Clear search");
            clearSearch.addActionListener(event -> {
                setSearchText("");
                searchField.requestFocusInWindow();
            });
            setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            searchField.addPropertyChangeListener("UI", event -> updateSearchAppearance());
            updateSearchAppearance();
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateSearchText();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateSearchText();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateSearchText();
                }
            });
            Action focusSearch = JCommand.<JListSelection<E>>of(c -> {
                        if (c.searchVisible) {
                            searchField.requestFocusInWindow();
                            searchField.selectAll();
                        }
                    })
                    .toAction(JListSelection.this);
            JListSelection.this
                    .getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "focusSearch");
            JListSelection.this.getActionMap().put("focusSearch", focusSearch);
            searchField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSearch");
            searchField
                    .getActionMap()
                    .put(
                            "clearSearch",
                            JCommand.<JTextField>of(c -> setSearchText("")).toAction(searchField));
            searchField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "focusResults");
            searchField
                    .getActionMap()
                    .put(
                            "focusResults",
                            JCommand.<JTextField>of(c -> {
                                        if (sourceList.getModel().getSize() > 0) {
                                            sourceList.setSelectedIndex(0);
                                            sourceList.ensureIndexIsVisible(0);
                                            sourceList.requestFocusInWindow();
                                        }
                                    })
                                    .toAction(searchField));
        }

        private void updateSearchText() {
            String old = searchText;
            searchText = searchField.getText();
            updateClearButton();
            JListSelection.this.firePropertyChange(SEARCH_TEXT_PROPERTY, old, searchText);
        }

        private boolean isFlatLaf() {
            return UIManager.getLookAndFeel().getID().startsWith("FlatLaf");
        }

        private void updateSearchAppearance() {
            boolean flatLaf = isFlatLaf();
            removeAll();
            // Use FlatLaf's public client properties without a runtime dependency.
            searchField.putClientProperty("JTextField.trailingComponent", null);
            searchField.putClientProperty("JTextField.placeholderText", "Search available items...");
            searchField.putClientProperty("JTextField.showClearButton", false);
            searchField.putClientProperty("FlatLaf.style", "leadingIcon: com.formdev.flatlaf.icons.FlatSearchIcon");
            searchStatus.setEnabled(false);
            searchStatus.setBorder(flatLaf ? BorderFactory.createEmptyBorder(0, 6, 0, 6) : null);
            clearSearch.putClientProperty("JButton.buttonType", flatLaf ? "toolBarButton" : null);
            clearSearch.putClientProperty("FlatLaf.styleClass", flatLaf ? "clearButton" : null);
            clearSearch.setText(flatLaf ? null : "Clear");
            if (!flatLaf) {
                clearSearch.setIcon(null);
                clearSearch.setFocusable(true);
            }
            JPanel trailing = new JPanel(new BorderLayout());
            trailing.setOpaque(false);
            trailing.add(searchStatus, BorderLayout.CENTER);
            trailing.add(clearSearch, BorderLayout.LINE_END);
            if (flatLaf) {
                if (searchPrompt != null) {
                    searchField.remove(searchPrompt);
                }
                searchField.putClientProperty("JTextField.trailingComponent", trailing);
            } else {
                if (searchPrompt == null) {
                    searchPrompt = new TextPrompt("Search available items...", searchField);
                } else {
                    searchField.setLayout(new BorderLayout());
                    searchField.add(searchPrompt);
                }
                searchPrompt.setFont(searchField.getFont());
                searchPrompt.setForeground(searchField.getDisabledTextColor());
                searchPrompt.setBorder(BorderFactory.createEmptyBorder(
                        searchField.getInsets().top, searchField.getInsets().left,
                        searchField.getInsets().bottom, searchField.getInsets().right));
                ((BorderLayout) trailing.getLayout()).setHgap(5);
                add(trailing, BorderLayout.EAST);
            }
            updateClearButton();
            clearSearch.getAccessibleContext().setAccessibleName("Clear search");
            add(searchField, BorderLayout.CENTER);
            revalidate();
            repaint();
        }

        private void updateClearButton() {
            clearSearch.setEnabled(!searchText.isEmpty());
            clearSearch.setVisible(!isFlatLaf() || !searchText.isEmpty());
        }

        private void updateCount(int visible, int total, boolean filtering) {
            searchStatus.setText(visible + "/" + total);
            String description =
                    (visible == 0 && filtering ? "No matches. " : "") + visible + " of " + total + " available items";
            searchStatus.setToolTipText(description);
            searchStatus.getAccessibleContext().setAccessibleName(description);
        }
    }

    private void enableProperties() {
        addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case SOURCE_MODEL_PROPERTY:
                    onSourceModelChange();
                    break;
                case TARGET_MODEL_PROPERTY:
                    onTargetModelChange();
                    break;
                case CELL_RENDERER_PROPERTY:
                    onCellRendererChange();
                    break;
                case ORIENTATION_PROPERTY:
                    onOrientationChange();
                    break;
                case SOURCE_FOOTER_PROPERTY:
                    onSourcePanelChange();
                    break;
                case SOURCE_HEADER_PROPERTY:
                    onSourcePanelChange();
                    break;
                case TARGET_FOOTER_PROPERTY:
                    onTargetPanelChange();
                    break;
                case TARGET_HEADER_PROPERTY:
                    onTargetPanelChange();
                    break;
                case SEARCH_VISIBLE_PROPERTY:
                    onOrientationChange();
                    refreshSourceView();
                    break;
                case SEARCH_TEXT_PROPERTY:
                case SEARCH_TEXT_PROVIDER_PROPERTY:
                    refreshSourceView();
                    break;
                case "componentPopupMenu":
                    onComponentPopupMenuChange();
                    break;
            }
        });
    }

    private void enableSelectionOnDoubleClick() {
        sourceList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (isItemDoubleClick(sourceList, evt)) {
                    getActionMap().get(SELECT_ACTION).actionPerformed(null);
                }
            }
        });
        targetList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (isItemDoubleClick(targetList, evt)) {
                    getActionMap().get(UNSELECT_ACTION).actionPerformed(null);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Event handlers">
    private static boolean isItemDoubleClick(JList<?> list, MouseEvent event) {
        int index = list.locationToIndex(event.getPoint());
        return SwingUtilities.isLeftMouseButton(event)
                && event.getClickCount() == 2
                && index >= 0
                && list.getCellBounds(index, index).contains(event.getPoint());
    }

    private void onSourceModelChange() {
        if (observedSourceModel != null) {
            observedSourceModel.removeListDataListener(sourceListener);
        }
        observedSourceModel = sourceModel;
        observedSourceModel.addListDataListener(sourceListener);
        refreshSourceView();
    }

    private boolean isFiltering() {
        return searchVisible && !searchText.trim().isEmpty();
    }

    private void refreshSourceView() {
        if (isFiltering()) {
            String query = searchText.trim().toLowerCase(Locale.ROOT);
            int[] indices = IntStream.range(0, sourceModel.size())
                    .filter(i -> searchTextProvider
                            .apply(sourceModel.get(i))
                            .toLowerCase(Locale.ROOT)
                            .contains(query))
                    .toArray();
            SourceView<E> next = new SourceView<>(sourceModel, indices, query);
            ListModel<E> current = sourceList.getModel();
            if (current instanceof SourceView && ((SourceView<?>) current).sameItems(next)) {
                ((SourceView<?>) current).indices = indices;
            } else {
                sourceList.setModel(next);
            }
        } else if (sourceList.getModel() != sourceModel) {
            sourceList.setModel(sourceModel);
        }
        int visible = sourceList.getModel().getSize();
        searchPanel.updateCount(visible, sourceModel.size(), isFiltering());
        Action selectAll = getActionMap().get(SELECT_ALL_ACTION);
        String text = isFiltering() ? "Add all matches" : "Add all";
        selectAll.putValue(Action.NAME, text);
        selectAll.putValue(Action.SHORT_DESCRIPTION, text);
        sourceList.repaint();
    }

    private int sourceModelIndex(int viewIndex) {
        ListModel<E> model = sourceList.getModel();
        return model instanceof SourceView ? ((SourceView<?>) model).indices[viewIndex] : viewIndex;
    }

    private int[] sourceSelection() {
        return JLists.getSelectionIndexStream(sourceList.getSelectionModel())
                .map(this::sourceModelIndex)
                .toArray();
    }

    private void updateBulkModels(Runnable update) {
        // DefaultListModel has no bulk insertion API on Java 8. Detach the views
        // so each insertion does not trigger list layout and selection updates.
        sourceList.setModel(JLists.emptyModel());
        targetList.setModel(JLists.emptyModel());
        updatingModels = true;
        try {
            update.run();
        } finally {
            updatingModels = false;
            try {
                refreshSourceView();
            } finally {
                targetList.setModel(targetModel);
            }
        }
    }

    private static <T> void appendItems(DefaultListModel<T> model, List<T> items) {
        model.ensureCapacity(model.size() + items.size());
        for (T item : items) {
            model.addElement(item);
        }
    }

    private void selectSourceIndices(int start, int count) {
        JLists.setSelectionIndexStream(
                sourceList.getSelectionModel(),
                IntStream.range(0, sourceList.getModel().getSize())
                        .filter(i -> sourceModelIndex(i) >= start && sourceModelIndex(i) < start + count));
    }

    private static final class SourceView<T> extends AbstractListModel<T> {

        private int[] indices;
        private final List<T> values;
        private final String query;

        private SourceView(DefaultListModel<T> model, int[] indices, String query) {
            this.indices = indices;
            this.values = Arrays.stream(indices).mapToObj(model::get).collect(Collectors.toList());
            this.query = query;
        }

        private boolean sameItems(SourceView<?> other) {
            return query.equals(other.query)
                    && values.size() == other.values.size()
                    && IntStream.range(0, values.size()).allMatch(i -> values.get(i) == other.values.get(i));
        }

        @Override
        public int getSize() {
            return values.size();
        }

        @Override
        public T getElementAt(int index) {
            return values.get(index);
        }
    }

    private void onTargetModelChange() {
        targetList.setModel(targetModel);
    }

    private void onCellRendererChange() {
        sourceList.setCellRenderer(cellRenderer);
        targetList.setCellRenderer(cellRenderer);
    }

    private void onOrientationChange() {
        boolean horizontal = isHorizontal();

        toolBar.setOrientation(horizontal ? SwingConstants.VERTICAL : SwingConstants.HORIZONTAL);

        listsPanel.setLayout(new BoxLayout(listsPanel, horizontal ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS));
        searchPanel.setVisible(searchVisible);
        revalidate();
        repaint();
    }

    private void onComponentPopupMenuChange() {
        JPopupMenu popupMenu = getComponentPopupMenu();
        Stream.of(sourcePanel, sourceList, targetPanel, targetList).forEach(o -> o.setComponentPopupMenu(popupMenu));
    }

    private void onSourcePanelChange() {
        updatePanel(sourcePanel, sourceHeader, sourceFooter);
    }

    private void onTargetPanelChange() {
        updatePanel(targetPanel, targetHeader, targetFooter);
    }

    private static void updatePanel(JPanel panel, Component header, Component footer) {
        BorderLayout layout = (BorderLayout) panel.getLayout();
        for (String position : new String[] {BorderLayout.NORTH, BorderLayout.SOUTH}) {
            Component previous = layout.getLayoutComponent(position);
            if (previous != null) {
                panel.remove(previous);
            }
        }
        if (header != null) {
            panel.add(header, BorderLayout.NORTH);
        }
        if (footer != null) {
            panel.add(footer, BorderLayout.SOUTH);
        }
        panel.revalidate();
        panel.repaint();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    @NonNull public DefaultListModel<E> getSourceModel() {
        return sourceModel;
    }

    public void setSourceModel(@Nullable DefaultListModel<E> sourceModel) {
        DefaultListModel<E> old = this.sourceModel;
        this.sourceModel = sourceModel != null ? sourceModel : new DefaultListModel<>();
        firePropertyChange(SOURCE_MODEL_PROPERTY, old, this.sourceModel);
    }

    @NonNull public DefaultListModel<E> getTargetModel() {
        return targetModel;
    }

    public void setTargetModel(@Nullable DefaultListModel<E> targetModel) {
        DefaultListModel<E> old = this.targetModel;
        this.targetModel = targetModel != null ? targetModel : new DefaultListModel<>();
        firePropertyChange(TARGET_MODEL_PROPERTY, old, this.targetModel);
    }

    /**
     * Whether the source search bar is visible. Defaults to false.
     */
    public boolean isSearchVisible() {
        return searchVisible;
    }

    /**
     * Shows or hides source search. Hiding it suspends filtering without clearing
     * the query. The search bar spans both lists in a single row above them;
     * only available items are filtered. Existing headers and footers are retained.
     * FlatLaf shows an inline prompt, search icon, match count and clear button;
     * other look-and-feels use a text prompt with an external count and clear button.
     * The count is displayed as matches/available, with an explanatory tooltip.
     * Ctrl+F focuses search, Escape clears it, and Down focuses the results.
     */
    public void setSearchVisible(boolean searchVisible) {
        boolean old = this.searchVisible;
        this.searchVisible = searchVisible;
        firePropertyChange(SEARCH_VISIBLE_PROPERTY, old, searchVisible);
    }

    @NonNull public String getSearchText() {
        return searchText;
    }

    /**
     * Sets the source query. Matching is case-insensitive substring matching,
     * ignoring leading and trailing query whitespace. An empty query shows all
     * available items. Changing the filtered view clears its highlighted rows,
     * but never changes the chosen items or the underlying source model.
     */
    public void setSearchText(@NonNull String searchText) {
        if (!this.searchText.equals(searchText)) {
            searchPanel.searchField.setText(searchText);
        }
    }

    @NonNull public Function<? super E, String> getSearchTextProvider() {
        return searchTextProvider;
    }

    /**
     * Supplies non-null searchable text for each item, independently of its cell
     * renderer. Defaults to {@link String#valueOf(Object)}.
     * Also supplies item tooltips when the cell renderer does not provide one.
     * While filtering, "Select all" adds only matches and inversion is disabled.
     */
    public void setSearchTextProvider(@NonNull Function<? super E, String> searchTextProvider) {
        Function<? super E, String> old = this.searchTextProvider;
        this.searchTextProvider = searchTextProvider;
        firePropertyChange(SEARCH_TEXT_PROVIDER_PROPERTY, old, searchTextProvider);
    }

    @NonNull public ListCellRenderer<? super E> getCellRenderer() {
        return cellRenderer;
    }

    public void setCellRenderer(@Nullable ListCellRenderer<? super E> cellRenderer) {
        ListCellRenderer<? super E> old = this.cellRenderer;
        this.cellRenderer = cellRenderer != null ? cellRenderer : new DefaultListCellRenderer();
        firePropertyChange(CELL_RENDERER_PROPERTY, old, this.cellRenderer);
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        if (!(orientation == SwingConstants.HORIZONTAL || orientation == SwingConstants.VERTICAL)) {
            throw new IllegalArgumentException();
        }
        int old = this.orientation;
        this.orientation = orientation;
        firePropertyChange(ORIENTATION_PROPERTY, old, this.orientation);
    }

    @Nullable public Component getSourceFooter() {
        return sourceFooter;
    }

    public void setSourceFooter(@Nullable Component sourceFooter) {
        Component old = this.sourceFooter;
        this.sourceFooter = sourceFooter;
        firePropertyChange(SOURCE_FOOTER_PROPERTY, old, this.sourceFooter);
    }

    @Nullable public Component getSourceHeader() {
        return sourceHeader;
    }

    public void setSourceHeader(@Nullable Component sourceHeader) {
        Component old = this.sourceHeader;
        this.sourceHeader = sourceHeader;
        firePropertyChange(SOURCE_HEADER_PROPERTY, old, this.sourceHeader);
    }

    @Nullable public Component getTargetFooter() {
        return targetFooter;
    }

    public void setTargetFooter(@Nullable Component targetFooter) {
        Component old = this.targetFooter;
        this.targetFooter = targetFooter;
        firePropertyChange(TARGET_FOOTER_PROPERTY, old, this.targetFooter);
    }

    @Nullable public Component getTargetHeader() {
        return targetHeader;
    }

    public void setTargetHeader(@Nullable Component targetHeader) {
        Component old = this.targetHeader;
        this.targetHeader = targetHeader;
        firePropertyChange(TARGET_HEADER_PROPERTY, old, this.targetHeader);
    }
    // </editor-fold>

    @NonNull public List<E> getSelectedValues() {
        return JLists.stream(targetModel).collect(Collectors.toList());
    }

    public JToolBar createToolBar() {
        ActionMap am = getActionMap();
        JToolBar result = new JToolBar();
        result.add(am.get(SELECT_ACTION)).setIcon(iconOf(ToolBarIcon.MOVE_RIGHT, ToolBarIcon.MOVE_DOWN));
        result.add(am.get(UNSELECT_ACTION)).setIcon(iconOf(ToolBarIcon.MOVE_LEFT, ToolBarIcon.MOVE_UP));
        JButton selectAll = result.add(am.get(SELECT_ALL_ACTION));
        selectAll.setHideActionText(true);
        selectAll.setIcon(iconOf(ToolBarIcon.MOVE_ALL_RIGHT, ToolBarIcon.MOVE_ALL_DOWN));
        result.add(am.get(UNSELECT_ALL_ACTION)).setIcon(iconOf(ToolBarIcon.MOVE_ALL_LEFT, ToolBarIcon.MOVE_ALL_UP));
        result.add(am.get(INVERT_ACTION)).setIcon(iconOf(ToolBarIcon.MOVE_HORIZONTALLY, ToolBarIcon.MOVE_VERTICALLY));
        for (Component component : result.getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                button.setHideActionText(true);
                button.getAccessibleContext()
                        .setAccessibleName((String) button.getAction().getValue(Action.NAME));
                button.getAction().addPropertyChangeListener(event -> {
                    if (Action.NAME.equals(event.getPropertyName())) {
                        button.getAccessibleContext().setAccessibleName((String) event.getNewValue());
                    }
                });
            }
        }
        return result;
    }

    private void nameAction(String id, String name) {
        Action action = getActionMap().get(id);
        action.putValue(Action.NAME, name);
        action.putValue(Action.SHORT_DESCRIPTION, name);
    }

    public JPopupMenu createPopupMenu() {
        ActionMap am = getActionMap();
        JMenu result = new JMenu();
        result.add(am.get(SELECT_ACTION));
        result.add(am.get(UNSELECT_ACTION));
        result.add(am.get(SELECT_ALL_ACTION));
        result.add(am.get(UNSELECT_ALL_ACTION));
        result.add(am.get(INVERT_ACTION));
        return result.getPopupMenu();
    }

    private Icon iconOf(ToolBarIcon hIcon, ToolBarIcon vIcon) {
        return ForwardingIcon.of(
                this::isHorizontal,
                hIcon.lookup().orElseGet(InternalUtil.MISSING_ICON),
                vIcon.lookup().orElseGet(InternalUtil.MISSING_ICON));
    }

    private boolean isHorizontal() {
        return orientation == SwingConstants.HORIZONTAL;
    }

    // <editor-fold defaultstate="collapsed" desc="DataTransfer">
    private final class CustomTransferHandler extends TransferHandler {

        private final LocalDataTransfer<JList> listTransfer = LocalDataTransfer.of(JList.class);

        private boolean isValidComponent(Component c) {
            return c == sourceList || c == targetList;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDrop()
                    && listTransfer.canImport(support)
                    && isValidComponent(support.getComponent())
                    && listTransfer
                            .getData(support)
                            .map(list -> isValidComponent(list) && !list.isSelectionEmpty())
                            .orElse(false);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            listTransfer
                    .getData(support)
                    .ifPresent(source -> importData(
                            source, (JList<?>) support.getComponent(), (JList.DropLocation) support.getDropLocation()));
            return true;
        }

        private void importData(JList<?> from, JList<?> to, JList.DropLocation dl) {
            moveItems(from, to, dl.getIndex());
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return listTransfer.createTransferable((JList<?>) c);
        }
    }

    // Drop indices refer to the visible list; insert before the next match or
    // append to the underlying model when dropping after the last match.
    void moveItems(JList<?> from, JList<?> to, int viewDropIndex) {
        int[] selection = from == sourceList
                ? sourceSelection()
                : JLists.getSelectionIndexStream(from.getSelectionModel()).toArray();
        if (selection.length == 0) {
            return;
        }
        int modelDropIndex = to == sourceList
                ? (viewDropIndex < sourceList.getModel().getSize()
                        ? sourceModelIndex(viewDropIndex)
                        : sourceModel.size())
                : viewDropIndex;
        int dropIndex = from == to
                ? modelDropIndex
                        - (int) Arrays.stream(selection)
                                .filter(i -> i < modelDropIndex)
                                .count()
                : modelDropIndex;
        transferItems(from, to, selection, dropIndex);
        if (to == sourceList) {
            selectSourceIndices(dropIndex, selection.length);
        } else {
            targetList.setSelectionInterval(dropIndex, dropIndex + selection.length - 1);
        }
        revealSelection(to == sourceList ? sourceList : targetList);
    }

    private void transferItems(JList<?> from, JList<?> to, int[] selection, int dropIndex) {
        sourceList.clearSelection();
        targetList.clearSelection();
        DefaultListModel<E> fromModel = from == sourceList ? sourceModel : targetModel;
        DefaultListModel<E> toModel = to == sourceList ? sourceModel : targetModel;
        List<E> moved = Arrays.stream(selection).mapToObj(fromModel::get).collect(Collectors.toList());
        boolean[] selected = new boolean[fromModel.size()];
        Arrays.stream(selection).forEach(i -> selected[i] = true);
        List<E> retained = IntStream.range(0, selected.length)
                .filter(i -> !selected[i])
                .mapToObj(fromModel::get)
                .collect(Collectors.toList());
        List<E> destination = from == to ? retained : JLists.stream(toModel).collect(Collectors.toList());
        destination.addAll(dropIndex, moved);
        updateBulkModels(() -> {
            fromModel.clear();
            if (from != to) {
                appendItems(fromModel, retained);
                toModel.clear();
            }
            appendItems(toModel, destination);
        });
    }

    private static void revealSelection(JList<?> destinationList) {
        if (!destinationList.isSelectionEmpty()) {
            int first = destinationList.getMinSelectionIndex();
            destinationList.ensureIndexIsVisible(first);
            ListModel<?> model = destinationList.getModel();
            SwingUtilities.invokeLater(() -> {
                if (destinationList.getModel() == model && destinationList.isSelectedIndex(first)) {
                    destinationList.ensureIndexIsVisible(first);
                }
            });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Commands">
    @lombok.RequiredArgsConstructor
    private static final class TransferCommand<T> extends JCommand<JListSelection<T>> {
        private final boolean adding;
        private final boolean all;

        private JList<T> from(JListSelection<T> c) {
            return adding ? c.sourceList : c.targetList;
        }

        @Override
        public void execute(@NonNull JListSelection<T> c) {
            JList<T> from = from(c);
            JList<T> to = adding ? c.targetList : c.sourceList;
            if (all) {
                int[] indices = IntStream.range(0, from.getModel().getSize())
                        .map(i -> adding ? c.sourceModelIndex(i) : i)
                        .toArray();
                c.transferItems(from, to, indices, adding ? c.targetModel.size() : c.sourceModel.size());
            } else {
                c.moveItems(from, to, to.getModel().getSize());
            }
        }

        @Override
        public boolean isEnabled(@NonNull JListSelection<T> c) {
            return all ? from(c).getModel().getSize() > 0 : !from(c).isSelectionEmpty();
        }

        @Override
        public @NonNull ActionAdapter toAction(@NonNull JListSelection<T> c) {
            ActionAdapter result = super.toAction(c);
            if (all) {
                addListDataListener(result, from(c));
            } else {
                result.withWeakListSelectionListener(from(c).getSelectionModel());
            }
            return result;
        }
    }

    private static void addListDataListener(JCommand<?>.ActionAdapter action, JList<?> list) {
        ListDataListener listener = JLists.dataListenerOf(o -> action.refreshActionState());
        list.getModel().addListDataListener(listener);
        list.addPropertyChangeListener("model", evt -> {
            ((ListModel<?>) evt.getOldValue()).removeListDataListener(listener);
            ((ListModel<?>) evt.getNewValue()).addListDataListener(listener);
            action.refreshActionState();
        });
    }

    private static final class InvertCommand<T> extends JCommand<JListSelection<T>> {

        @Override
        public void execute(@NonNull JListSelection<T> c) throws Exception {
            List<T> items = JLists.stream(c.sourceModel).collect(Collectors.toList());
            List<T> chosen = JLists.stream(c.targetModel).collect(Collectors.toList());
            int[] sourceSelection = JLists.getSelectionIndexStream(c.sourceList.getSelectionModel())
                    .toArray();
            int[] targetSelection = JLists.getSelectionIndexStream(c.targetList.getSelectionModel())
                    .toArray();

            c.sourceList.getSelectionModel().clearSelection();
            c.targetList.getSelectionModel().clearSelection();
            c.updateBulkModels(() -> {
                c.sourceModel.clear();
                c.targetModel.clear();
                appendItems(c.sourceModel, chosen);
                appendItems(c.targetModel, items);
            });

            JLists.setSelectionIndexStream(c.targetList.getSelectionModel(), IntStream.of(sourceSelection));
            JLists.setSelectionIndexStream(c.sourceList.getSelectionModel(), IntStream.of(targetSelection));
        }

        @Override
        public boolean isEnabled(@NonNull JListSelection<T> c) {
            return !c.isFiltering() && (!c.sourceModel.isEmpty() || !c.targetModel.isEmpty());
        }

        @Override
        public @NonNull ActionAdapter toAction(@NonNull JListSelection<T> c) {
            ActionAdapter result = super.toAction(c);
            addListDataListener(result, c.sourceList);
            addListDataListener(result, c.targetList);
            return result;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class OrientationCommand extends JCommand<JListSelection<?>> {
        private final int orientation;

        @Override
        public void execute(@NonNull JListSelection<?> c) throws Exception {
            c.setOrientation(
                    c.getOrientation() != orientation
                            ? orientation
                            : orientation == SwingConstants.HORIZONTAL
                                    ? SwingConstants.VERTICAL
                                    : SwingConstants.HORIZONTAL);
        }

        @Override
        public boolean isSelected(@NonNull JListSelection<?> c) {
            return c.getOrientation() == orientation;
        }

        @Override
        public @NonNull ActionAdapter toAction(@NonNull JListSelection<?> c) {
            return super.toAction(c).withWeakPropertyChangeListener(c, ORIENTATION_PROPERTY);
        }
    }

    // </editor-fold>
}
