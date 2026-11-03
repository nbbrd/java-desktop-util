package ec.util.list.swing;

import static ec.util.list.swing.JListSelection.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JListSelectionTest {

    @BeforeEach
    void requireDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(), "JListSelection enables Swing drag-and-drop");
    }

    @Test
    void searchIsOptInAndDoesNotMutateModels() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("Alpha", "Beta", "ALPHABET", "Gamma");
            c.getTargetModel().addElement("Already chosen");
            assertThat(c.isSearchVisible()).isFalse();
            assertThat(c.getSearchText()).isEmpty();
            assertThat(source(c).getModel()).isSameAs(c.getSourceModel());

            c.setSearchText("  aLpHa  ");
            assertThat(values(source(c))).containsExactly("Alpha", "Beta", "ALPHABET", "Gamma");
            c.setSearchVisible(true);
            assertThat(values(source(c))).containsExactly("Alpha", "ALPHABET");
            assertThat(JLists.stream(c.getSourceModel())).containsExactly("Alpha", "Beta", "ALPHABET", "Gamma");
            assertThat(c.getSelectedValues()).containsExactly("Already chosen");
            assertThat(find(c, JLabel.class)).extracting(JLabel::getText).contains("2/4");

            c.setSearchVisible(false);
            assertThat(source(c).getModel()).isSameAs(c.getSourceModel());
            assertThat(c.getSearchText()).isEqualTo("  aLpHa  ");
            c.setSearchVisible(true);
            assertThat(values(source(c))).containsExactly("Alpha", "ALPHABET");
            c.setSearchText("   ");
            assertThat(source(c).getModel()).isSameAs(c.getSourceModel());
        });
    }

    @Test
    void transfersUseModelIndicesAndRetainQuery() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("Hidden", "Alpha", "Other", "Alpine", "Alpha");
            c.setSearchVisible(true);
            c.setSearchText("alp");
            source(c).setSelectedIndices(new int[] {1, 2});
            execute(c, SELECT_ACTION);

            assertThat(c.getSelectedValues()).containsExactly("Alpine", "Alpha");
            assertThat(JLists.stream(c.getSourceModel())).containsExactly("Hidden", "Alpha", "Other");
            assertThat(values(source(c))).containsExactly("Alpha");
            assertThat(c.getSearchText()).isEqualTo("alp");
            assertThat(target(c).getSelectedIndices()).containsExactly(0, 1);

            execute(c, UNSELECT_ACTION);
            assertThat(c.getSelectedValues()).isEmpty();
            assertThat(values(source(c))).containsExactly("Alpha", "Alpine", "Alpha");
            assertThat(source(c).getSelectedIndices()).containsExactly(1, 2);
        });
    }

    @Test
    void bulkActionsRespectMatchesAndPopupLabelsFollowQuery() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("Hidden", "Alpha", "Beta", "Alpine");
            JPopupMenu popup = c.createPopupMenu();
            c.setSearchVisible(true);
            c.setSearchText("alp");
            assertThat(((javax.swing.JMenuItem) popup.getComponent(2)).getText())
                    .isEqualTo("Add all matches");
            assertThat(c.getActionMap().get(INVERT_ACTION).isEnabled()).isFalse();
            execute(c, INVERT_ACTION);
            assertThat(c.getSelectedValues()).isEmpty();

            execute(c, SELECT_ALL_ACTION);
            assertThat(c.getSelectedValues()).containsExactly("Alpha", "Alpine");
            assertThat(JLists.stream(c.getSourceModel())).containsExactly("Hidden", "Beta");
            assertThat(c.getActionMap().get(SELECT_ALL_ACTION).isEnabled()).isFalse();
            assertThat(find(c, JLabel.class)).extracting(JLabel::getText).contains("0/2");

            execute(c, UNSELECT_ALL_ACTION);
            assertThat(c.getSelectedValues()).isEmpty();
            assertThat(values(source(c))).containsExactly("Alpha", "Alpine");
            c.setSearchText("");
            assertThat(((javax.swing.JMenuItem) popup.getComponent(2)).getText())
                    .isEqualTo("Add all");
            assertThat(c.getActionMap().get(INVERT_ACTION).isEnabled()).isTrue();
            execute(c, SELECT_ALL_ACTION);
            assertThat(c.getSelectedValues()).containsExactly("Hidden", "Beta", "Alpha", "Alpine");
        });
    }

    @Test
    void changingQueryClearsStaleHighlights() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("Alpha", "Beta");
            c.setSearchVisible(true);
            c.setSearchText("alpha");
            source(c).setSelectedIndex(0);
            c.setSearchText("beta");
            assertThat(source(c).isSelectionEmpty()).isTrue();
            assertThat(c.getActionMap().get(SELECT_ACTION).isEnabled()).isFalse();
            execute(c, SELECT_ACTION);
            assertThat(c.getSelectedValues()).isEmpty();
        });
    }

    @Test
    void providerIsIndependentOfRendererAndRefreshesMatches() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<Integer> c = new JListSelection<>();
            c.getSourceModel().addElement(1);
            c.getSourceModel().addElement(2);
            c.setSearchVisible(true);
            c.setSearchText("one");
            assertThat(values(source(c))).isEmpty();
            c.setSearchTextProvider(value -> value == 1 ? "One" : "Two");
            assertThat(values(source(c))).containsExactly(1);
            c.setSearchTextProvider(value -> "One");
            assertThat(values(source(c))).containsExactly(1, 2);
            assertThatNullPointerException().isThrownBy(() -> c.setSearchTextProvider(null));
            assertThatNullPointerException().isThrownBy(() -> c.setSearchText(null));
        });
    }

    @Test
    void externalSourceEditsAndModelReplacementRefreshSearch() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("Alpha", "Beta");
            c.setSearchVisible(true);
            c.setSearchText("alp");
            c.getSourceModel().addElement("Alpine");
            assertThat(values(source(c))).containsExactly("Alpha", "Alpine");
            c.getSourceModel().set(0, "Other");
            assertThat(values(source(c))).containsExactly("Alpine");
            c.getSourceModel().remove(2);
            assertThat(values(source(c))).isEmpty();

            DefaultListModel<String> old = c.getSourceModel();
            DefaultListModel<String> replacement = new DefaultListModel<>();
            replacement.addElement("Alphabet");
            c.setSourceModel(replacement);
            assertThat(values(source(c))).containsExactly("Alphabet");
            old.addElement("Alps");
            assertThat(values(source(c))).containsExactly("Alphabet");
            replacement.clear();
            assertThat(values(source(c))).isEmpty();
            c.setSourceModel(null);
            c.getSourceModel().addElement("Alps");
            assertThat(values(source(c))).containsExactly("Alps");
        });
    }

    @Test
    void targetReplacementRemainsVisibleAndTransferable() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("Alpha");
            c.setSearchVisible(true);
            c.setSearchText("alp");
            DefaultListModel<String> replacement = new DefaultListModel<>();
            replacement.addElement("Beta");
            replacement.addElement("Alpine");
            c.setTargetModel(replacement);
            assertThat(target(c).getModel()).isSameAs(replacement);
            target(c).setSelectedIndices(new int[] {0, 1});
            execute(c, UNSELECT_ACTION);
            assertThat(values(source(c))).containsExactly("Alpha", "Alpine");
            assertThat(source(c).getSelectedIndices()).containsExactly(1);
            assertThat(JLists.stream(c.getSourceModel())).containsExactly("Alpha", "Beta", "Alpine");
        });
    }

    @Test
    void filteredDropsMapBothSelectionAndInsertionIndices() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("Hidden", "Alpha", "Other", "Alpine", "Last");
            c.setSearchVisible(true);
            c.setSearchText("alp");
            source(c).setSelectedIndex(1);
            c.moveItems(source(c), target(c), 0);
            assertThat(c.getSelectedValues()).containsExactly("Alpine");
            assertThat(JLists.stream(c.getSourceModel())).containsExactly("Hidden", "Alpha", "Other", "Last");

            c.moveItems(target(c), source(c), 0);
            assertThat(JLists.stream(c.getSourceModel())).containsExactly("Hidden", "Alpine", "Alpha", "Other", "Last");
            assertThat(source(c).getSelectedIndices()).containsExactly(0);

            c.getTargetModel().addElement("Not a match");
            target(c).setSelectedIndex(0);
            c.moveItems(target(c), source(c), source(c).getModel().getSize());
            assertThat(JLists.stream(c.getSourceModel()))
                    .containsExactly("Hidden", "Alpine", "Alpha", "Other", "Last", "Not a match");
            assertThat(source(c).isSelectionEmpty()).isTrue();
        });
    }

    @Test
    void reorderingFilteredItemsKeepsHiddenItemsAndRestoresHighlights() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("Hidden", "Alpha", "Other", "Alpine", "Last", "Alphabet");
            c.setSearchVisible(true);
            c.setSearchText("alp");
            source(c).setSelectedIndices(new int[] {0, 2});
            c.moveItems(source(c), source(c), 2);
            assertThat(JLists.stream(c.getSourceModel()))
                    .containsExactly("Hidden", "Other", "Alpine", "Last", "Alpha", "Alphabet");
            assertThat(values(source(c))).containsExactly("Alpine", "Alpha", "Alphabet");
            assertThat(source(c).getSelectedIndices()).containsExactly(1, 2);
            source(c).setSelectedIndex(2);
            c.moveItems(source(c), source(c), 0);
            assertThat(values(source(c))).containsExactly("Alphabet", "Alpine", "Alpha");
            assertThat(source(c).getSelectedIndices()).containsExactly(0);
        });
    }

    @Test
    void headersFootersAndBothOrientationsRetainSearch() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("Alpha", "Beta");
            JLabel header = new JLabel("Available");
            JLabel footer = new JLabel("Footer");
            c.setSourceHeader(header);
            c.setSourceFooter(footer);
            c.setSearchVisible(true);
            c.setSearchText("alp");
            for (int orientation : new int[] {SwingConstants.VERTICAL, SwingConstants.HORIZONTAL}) {
                c.setOrientation(orientation);
                assertThat(find(c, JLabel.class)).contains(header, footer);
                assertThat(find(c, JTextField.class)).hasSize(1);
                assertThat(values(source(c))).containsExactly("Alpha");
                c.setSourceHeader(null);
                assertThat(find(c, JTextField.class)).hasSize(1);
                c.setSourceHeader(header);
            }
            c.setSearchVisible(false);
            assertThat(find(c, JLabel.class)).contains(header, footer);
            assertThat(find(c, JTextField.class).get(0).getParent().isVisible()).isFalse();
        });
    }

    @Test
    void searchSpansBothListsInOneRow() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("Alpha", "Beta");
            c.setSourceHeader(new JLabel("Available"));
            c.setTargetHeader(new JLabel("Chosen"));
            c.setSearchVisible(true);
            for (int orientation : new int[] {SwingConstants.HORIZONTAL, SwingConstants.VERTICAL}) {
                c.setOrientation(orientation);
                c.setSize(900, 600);
                layout(c);
                JTextField field = find(c, JTextField.class).get(0);
                Container search = field.getParent();
                assertThat(search.getParent()).isSameAs(c);
                assertThat(search.getX()).isZero();
                assertThat(search.getWidth()).isEqualTo(c.getWidth());
                Rectangle fieldBounds = SwingUtilities.convertRectangle(field.getParent(), field.getBounds(), c);
                for (javax.swing.JButton button : find(search, javax.swing.JButton.class)) {
                    Rectangle bounds = SwingUtilities.convertRectangle(button.getParent(), button.getBounds(), c);
                    assertThat(bounds.intersects(new Rectangle(0, fieldBounds.y, c.getWidth(), fieldBounds.height)))
                            .isTrue();
                }
                Rectangle sourceBounds = SwingUtilities.convertRectangle(
                        source(c).getParent(), source(c).getBounds(), c);
                Rectangle targetBounds = SwingUtilities.convertRectangle(
                        target(c).getParent(), target(c).getBounds(), c);
                assertThat(sourceBounds.y).isGreaterThanOrEqualTo(search.getY() + search.getHeight());
                assertThat(targetBounds.y).isGreaterThanOrEqualTo(search.getY() + search.getHeight());
                if (orientation == SwingConstants.HORIZONTAL) {
                    assertThat(sourceBounds.y).isEqualTo(targetBounds.y);
                    assertThat(sourceBounds.height).isEqualTo(targetBounds.height);
                }
            }
        });
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container) {
                layout((Container) child);
            }
        }
    }

    @Test
    void keyboardActionsFocusResultsAndClearSearch() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("Alpha", "Beta");
            c.setSearchVisible(true);
            JTextField field = find(c, JTextField.class).get(0);
            field.setText("beta");
            assertThat(c.getSearchText()).isEqualTo("beta");
            assertThat(values(source(c))).containsExactly("Beta");
            field.getActionMap()
                    .get(field.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)))
                    .actionPerformed(null);
            assertThat(source(c).getSelectedIndex()).isZero();
            source(c).getActionMap().get(SELECT_ACTION).actionPerformed(null);
            assertThat(c.getSelectedValues()).containsExactly("Beta");
            field.getActionMap()
                    .get(field.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)))
                    .actionPerformed(null);
            assertThat(c.getSearchText()).isEmpty();
            assertThat(source(c).getModel()).isSameAs(c.getSourceModel());
        });
    }

    @Test
    void unfilteredInversionAndReorderingRetainExistingBehavior() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("A", "B", "C", "D");
            source(c).setSelectedIndices(new int[] {0, 2});
            c.moveItems(source(c), source(c), 3);
            assertThat(values(source(c))).containsExactly("B", "A", "C", "D");
            assertThat(source(c).getSelectedIndices()).containsExactly(1, 2);
            execute(c, SELECT_ACTION);
            execute(c, INVERT_ACTION);
            assertThat(values(source(c))).containsExactly("A", "C");
            assertThat(c.getSelectedValues()).containsExactly("B", "D");
        });
    }

    private static JListSelection<String> create(String... values) {
        JListSelection<String> result = new JListSelection<>();
        for (String value : values) {
            result.getSourceModel().addElement(value);
        }
        return result;
    }

    @Test
    void compactCountsExplainEmptyAndFilteredResults() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create();
            c.setSearchVisible(true);
            JLabel status = find(c, JLabel.class).stream()
                    .filter(label -> "0/0".equals(label.getText()))
                    .findFirst()
                    .orElseThrow(AssertionError::new);
            assertThat(status.getToolTipText()).isEqualTo("0 of 0 available items");
            for (int i = 0; i < 2526; i++) {
                c.getSourceModel().addElement((i < 10 ? "Match " : "Other ") + i);
            }
            assertThat(status.getText()).isEqualTo("2526/2526");
            c.setSearchText("match");
            assertThat(status.getText()).isEqualTo("10/2526");
            assertThat(status.getToolTipText()).isEqualTo("10 of 2526 available items");
            assertThat(status.getAccessibleContext().getAccessibleName()).isEqualTo(status.getToolTipText());
            execute(c, SELECT_ALL_ACTION);
            assertThat(status.getText()).isEqualTo("0/2516");
            assertThat(status.getToolTipText()).isEqualTo("No matches. 0 of 2516 available items");
            execute(c, UNSELECT_ALL_ACTION);
            assertThat(status.getText()).isEqualTo("10/2526");
            c.setSearchText("");
            assertThat(status.getText()).isEqualTo("2526/2526");
        });
    }

    @Test
    void searchPropertiesNotifyAndRepeatedSettersKeepHighlights() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("Alpha", "Beta");
            List<java.beans.PropertyChangeEvent> events = new ArrayList<>();
            c.addPropertyChangeListener(events::add);
            c.setSearchVisible(true);
            c.setSearchText("alp");
            java.util.function.Function<String, String> provider = s -> s.toUpperCase(Locale.ROOT);
            c.setSearchTextProvider(provider);
            source(c).setSelectedIndex(0);
            events.clear();
            c.setSearchVisible(true);
            c.setSearchText("alp");
            c.setSearchTextProvider(provider);
            assertThat(events).isEmpty();
            assertThat(source(c).getSelectedIndices()).containsExactly(0);
            c.setSearchVisible(false);
            assertThat(events).hasSize(1);
            assertThat(events.get(0).getPropertyName()).isEqualTo(SEARCH_VISIBLE_PROPERTY);
            assertThat(events.get(0).getOldValue()).isEqualTo(true);
            assertThat(events.get(0).getNewValue()).isEqualTo(false);
            events.clear();
            c.setSearchText("");
            assertThat(events).hasSize(1);
            assertThat(events.get(0).getPropertyName()).isEqualTo(SEARCH_TEXT_PROPERTY);
            assertThat(events.get(0).getOldValue()).isEqualTo("alp");
            assertThat(events.get(0).getNewValue()).isEqualTo("");
            assertThat(c.getSearchTextProvider()).isSameAs(provider);
        });
    }

    @Test
    void equalItemsAreTransferredByOccurrenceRatherThanEquality() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            String first = new String("Alpha");
            String second = new String("Alpha");
            JListSelection<String> c = create("Hidden", first, "Other", second);
            c.setSearchVisible(true);
            c.setSearchText("alpha");
            source(c).setSelectedIndex(1);
            execute(c, SELECT_ACTION);
            assertThat(c.getTargetModel().get(0)).isSameAs(second);
            assertThat(c.getSourceModel().get(1)).isSameAs(first);
            assertThat(source(c).getModel().getElementAt(0)).isSameAs(first);
            execute(c, UNSELECT_ACTION);
            assertThat(source(c).getModel().getElementAt(1)).isSameAs(second);
        });
    }

    @Test
    void allReorderSelectionsAndDropPositionsPreserveItemsAndOrder() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (boolean filtered : new boolean[] {false, true}) {
                for (int mask = 1; mask < 8; mask++) {
                    for (int drop = 0; drop <= 3; drop++) {
                        JListSelection<String> c = filtered
                                ? create("Hidden", "Match A", "Other", "Match B", "Last", "Match C")
                                : create("Match A", "Match B", "Match C");
                        c.setSearchVisible(filtered);
                        c.setSearchText(filtered ? "match" : "");
                        List<String> expected =
                                JLists.stream(c.getSourceModel()).collect(Collectors.toList());
                        List<String> moved = new ArrayList<>();
                        List<Integer> indices = new ArrayList<>();
                        for (int i = 0; i < 3; i++) {
                            if ((mask & (1 << i)) != 0) {
                                indices.add(i);
                                moved.add((String) source(c).getModel().getElementAt(i));
                            }
                        }
                        int insertion = drop == 3 ? expected.size() : expected.indexOf("Match " + (char) ('A' + drop));
                        int before = 0;
                        for (String item : moved) {
                            if (expected.indexOf(item) < insertion) {
                                before++;
                            }
                        }
                        expected.removeAll(moved);
                        expected.addAll(insertion - before, moved);
                        source(c)
                                .setSelectedIndices(indices.stream()
                                        .mapToInt(Integer::intValue)
                                        .toArray());
                        c.moveItems(source(c), source(c), drop);
                        assertThat(JLists.stream(c.getSourceModel()))
                                .as("filtered=%s, selection=%s, drop=%s", filtered, mask, drop)
                                .containsExactlyElementsOf(expected);
                        assertThat(new ArrayList<Object>(source(c).getSelectedValuesList()))
                                .containsExactlyElementsOf(moved);
                        assertThat(c.getTargetModel().isEmpty()).isTrue();
                    }
                }
            }
        });
    }

    @Test
    void emptySelectionAndNoResultsDoNotTransferHiddenItems() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("Alpha", "Beta");
            c.setSearchVisible(true);
            c.setSearchText("missing");
            assertThat(c.getActionMap().get(SELECT_ACTION).isEnabled()).isFalse();
            assertThat(c.getActionMap().get(SELECT_ALL_ACTION).isEnabled()).isFalse();
            assertThat(c.getActionMap().get(UNSELECT_ACTION).isEnabled()).isFalse();
            assertThat(c.getActionMap().get(UNSELECT_ALL_ACTION).isEnabled()).isFalse();
            c.moveItems(source(c), target(c), 0);
            execute(c, SELECT_ACTION);
            execute(c, SELECT_ALL_ACTION);
            assertThat(JLists.stream(c.getSourceModel())).containsExactly("Alpha", "Beta");
            assertThat(c.getSelectedValues()).isEmpty();
            c.setSearchVisible(false);
            assertThat(c.getActionMap().get(SELECT_ALL_ACTION).isEnabled()).isTrue();
            assertThat(c.getActionMap().get(INVERT_ACTION).isEnabled()).isTrue();
        });
    }

    @Test
    void itemTooltipsFollowFilteredRowsTransfersAndRendererOverrides() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("Hidden", "Alpha", "Beta");
            c.setSearchTextProvider(value -> "Description: " + value);
            c.setSearchVisible(true);
            c.setSearchText("alpha");
            source(c).setFixedCellHeight(20);
            source(c).setSize(200, 100);
            assertThat(tooltip(source(c), 10, 10)).isEqualTo("Description: Alpha");
            assertThat(tooltip(source(c), 10, 50)).isNull();
            source(c).setSelectedIndex(0);
            execute(c, SELECT_ACTION);
            target(c).setFixedCellHeight(20);
            target(c).setSize(200, 100);
            assertThat(tooltip(target(c), 10, 10)).isEqualTo("Description: Alpha");
            assertThat(tooltip(source(c), 10, 10)).isNull();
            c.setCellRenderer(JLists.cellRendererOf((label, value) -> {
                label.setText(value);
                label.setToolTipText("Custom: " + value);
            }));
            assertThat(tooltip(target(c), 10, 10)).isEqualTo("Custom: Alpha");
            assertThat(tooltip(target(c), 10, 50)).isNull();
            c.setCellRenderer(null);
            assertThat(tooltip(target(c), 10, 10)).isEqualTo("Description: Alpha");
            c.setSearchText("");
            assertThat(tooltip(source(c), 10, 10)).isEqualTo("Description: Hidden");
        });
    }

    @Test
    void bulkActionsDetachViewsAndPreserveCallerModelsAndSelections() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create();
            DefaultListModel<String> available = c.getSourceModel();
            DefaultListModel<String> chosen = c.getTargetModel();
            for (int i = 0; i < 10000; i++) {
                available.addElement("Item " + i);
            }
            List<String> original = JLists.stream(available).collect(Collectors.toList());
            java.util.concurrent.atomic.AtomicInteger attachedUpdates = new java.util.concurrent.atomic.AtomicInteger();
            javax.swing.event.ListDataListener listener = JLists.dataListenerOf(event -> {
                if (source(c).getModel() == available || target(c).getModel() == chosen) {
                    attachedUpdates.incrementAndGet();
                }
            });
            available.addListDataListener(listener);
            chosen.addListDataListener(listener);
            execute(c, SELECT_ALL_ACTION);
            assertThat(c.getSelectedValues()).containsExactlyElementsOf(original);
            assertThat(available.isEmpty()).isTrue();
            execute(c, UNSELECT_ALL_ACTION);
            assertThat(JLists.stream(available)).containsExactlyElementsOf(original);
            assertThat(chosen.isEmpty()).isTrue();
            assertThat(attachedUpdates.get()).isZero();
            target(c).clearSelection();
            chosen.addElement("Chosen");
            source(c).setSelectedIndices(new int[] {10, 100, 9999});
            target(c).setSelectedIndex(0);
            attachedUpdates.set(0);
            execute(c, INVERT_ACTION);
            assertThat(JLists.stream(available)).containsExactly("Chosen");
            assertThat(c.getSelectedValues()).containsExactlyElementsOf(original);
            assertThat(source(c).getSelectedIndices()).containsExactly(0);
            assertThat(target(c).getSelectedIndices()).containsExactly(10, 100, 9999);
            assertThat(attachedUpdates.get()).isZero();
            assertThat(c.getSourceModel()).isSameAs(available);
            assertThat(c.getTargetModel()).isSameAs(chosen);
            assertThat(source(c).getModel()).isSameAs(available);
            assertThat(target(c).getModel()).isSameAs(chosen);
            assertThat(c.getActionMap().get(SELECT_ALL_ACTION).isEnabled()).isTrue();
            assertThat(c.getActionMap().get(UNSELECT_ALL_ACTION).isEnabled()).isTrue();
        });
    }

    @Test
    void doubleClickOnlyTransfersActualRowsWithLeftButton() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("Alpha", "Beta");
            JList<?> list = source(c);
            list.setFixedCellHeight(20);
            list.setSize(200, 200);
            list.setSelectedIndex(0);
            click(list, 100, java.awt.event.MouseEvent.BUTTON1);
            click(list, 10, java.awt.event.MouseEvent.BUTTON3);
            assertThat(c.getSelectedValues()).isEmpty();
            click(list, 10, java.awt.event.MouseEvent.BUTTON1);
            assertThat(c.getSelectedValues()).containsExactly("Alpha");
            target(c).setFixedCellHeight(20);
            target(c).setSize(200, 200);
            click(target(c), 100, java.awt.event.MouseEvent.BUTTON1);
            assertThat(c.getSelectedValues()).containsExactly("Alpha");
            click(target(c), 10, java.awt.event.MouseEvent.BUTTON1);
            assertThat(c.getSelectedValues()).isEmpty();
        });
    }

    private static void click(JList<?> list, int y, int button) {
        list.dispatchEvent(new java.awt.event.MouseEvent(
                list, java.awt.event.MouseEvent.MOUSE_CLICKED, 0, 0, 10, y, 2, false, button));
    }

    @Test
    void unchangedFilteredMatchesKeepHighlightAndUpdatedIndexMapping() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("Hidden", "Alpha", "Other", "Alpine");
            c.setSearchVisible(true);
            c.setSearchText("alp");
            source(c).setSelectedIndex(1);
            javax.swing.ListModel<?> view = source(c).getModel();
            c.getSourceModel().add(0, "Not a match");
            c.getSourceModel().remove(3);
            assertThat(source(c).getModel()).isSameAs(view);
            assertThat(source(c).getSelectedIndices()).containsExactly(1);
            execute(c, SELECT_ACTION);
            assertThat(c.getSelectedValues()).containsExactly("Alpine");
            assertThat(JLists.stream(c.getSourceModel())).containsExactly("Not a match", "Hidden", "Alpha");
        });
    }

    @Test
    void toolbarAndPopupHaveConsistentAccessibleActionNames() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create("Alpha", "Beta");
            javax.swing.JToolBar toolbar = c.createToolBar();
            JPopupMenu popup = c.createPopupMenu();
            String[] names = {"Add", "Remove", "Add all", "Remove all", "Invert"};
            for (int i = 0; i < names.length; i++) {
                javax.swing.JButton button = (javax.swing.JButton) toolbar.getComponent(i);
                assertThat(button.getAccessibleContext().getAccessibleName()).isEqualTo(names[i]);
                assertThat(button.getToolTipText()).isEqualTo(names[i]);
                assertThat(((javax.swing.JMenuItem) popup.getComponent(i)).getText())
                        .isEqualTo(names[i]);
                assertThat(button.getHideActionText()).isTrue();
            }
            c.setSearchVisible(true);
            c.setSearchText("alp");
            assertThat(((javax.swing.JButton) toolbar.getComponent(2))
                            .getAccessibleContext()
                            .getAccessibleName())
                    .isEqualTo("Add all matches");
        });
    }

    @Test
    void largeMultiSelectionUsesDetachedViewsAndScrollsToTransferredItems() throws Exception {
        JListSelection<?>[] component = new JListSelection<?>[1];
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create();
            component[0] = c;
            for (int i = 0; i < 2000; i++) {
                c.getSourceModel().addElement("Item " + i);
                c.getTargetModel().addElement("Chosen " + i);
            }
            c.setSize(800, 300);
            layout(c);
            source(c)
                    .setSelectedIndices(
                            java.util.stream.IntStream.range(0, 1000).toArray());
            java.util.concurrent.atomic.AtomicInteger attachedUpdates = new java.util.concurrent.atomic.AtomicInteger();
            c.getTargetModel().addListDataListener(JLists.dataListenerOf(event -> {
                if (target(c).getModel() == c.getTargetModel()) {
                    attachedUpdates.incrementAndGet();
                }
            }));
            execute(c, SELECT_ACTION);
            assertThat(attachedUpdates.get()).isZero();
            assertThat(c.getTargetModel().size()).isEqualTo(3000);
            assertThat(c.getSourceModel().size()).isEqualTo(1000);
            assertThat(target(c).getMinSelectionIndex()).isEqualTo(2000);
            layout(c);
        });
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<?> c = component[0];
            assertThat(target(c).getVisibleRect().intersects(target(c).getCellBounds(2000, 2000)))
                    .isTrue();
            execute(c, UNSELECT_ACTION);
            assertThat(c.getSourceModel().size()).isEqualTo(2000);
            assertThat(source(c).getMinSelectionIndex()).isEqualTo(1000);
            layout(c);
        });
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<?> c = component[0];
            assertThat(source(c).getVisibleRect().intersects(source(c).getCellBounds(1000, 1000)))
                    .isTrue();
        });
    }

    @Test
    void emptyStatesArePaintedWithoutChangingModelsOrTooltips() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create();
            c.setSearchVisible(true);
            for (String query : new String[] {"", "missing"}) {
                c.setSearchText(query);
                for (JList<?> list : new JList<?>[] {source(c), target(c)}) {
                    list.setSize(200, 100);
                    java.awt.image.BufferedImage image =
                            new java.awt.image.BufferedImage(200, 100, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    java.awt.Graphics2D g = image.createGraphics();
                    try {
                        list.paint(g);
                    } finally {
                        g.dispose();
                    }
                    boolean textPainted = false;
                    int background = list.getBackground().getRGB();
                    for (int x = 20; x < 180; x++) {
                        for (int y = 30; y < 70; y++) {
                            textPainted |= image.getRGB(x, y) != background;
                        }
                    }
                    assertThat(textPainted).isTrue();
                    assertThat(list.getModel().getSize()).isZero();
                    assertThat(tooltip(list, 10, 10)).isNull();
                }
            }
        });
    }

    @Test
    void layoutChangesKeepScrollPanesAndViewportPositions() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create();
            for (int i = 0; i < 100; i++) {
                c.getSourceModel().addElement("Available " + i);
                c.getTargetModel().addElement("Chosen " + i);
            }
            c.setSize(800, 600);
            layout(c);
            Container sourceViewport = source(c).getParent();
            Container targetViewport = target(c).getParent();
            Container sourceScrollPane = sourceViewport.getParent();
            Container targetScrollPane = targetViewport.getParent();
            ((javax.swing.JViewport) sourceViewport).setViewPosition(new java.awt.Point(0, 100));
            ((javax.swing.JViewport) targetViewport).setViewPosition(new java.awt.Point(0, 150));
            JTextField field = find(c, JTextField.class).get(0);
            for (int orientation : new int[] {SwingConstants.VERTICAL, SwingConstants.HORIZONTAL}) {
                c.setOrientation(orientation);
                c.setSearchVisible(true);
                c.setSourceHeader(new JLabel("Available"));
                c.setSourceFooter(new JLabel("Footer"));
                c.setTargetHeader(new JLabel("Chosen"));
                c.setTargetFooter(new JLabel("Footer"));
                c.setSearchVisible(false);
                assertThat(source(c).getParent()).isSameAs(sourceViewport);
                assertThat(target(c).getParent()).isSameAs(targetViewport);
                assertThat(sourceViewport.getParent()).isSameAs(sourceScrollPane);
                assertThat(targetViewport.getParent()).isSameAs(targetScrollPane);
                assertThat(((javax.swing.JViewport) sourceViewport).getViewPosition().y)
                        .isEqualTo(100);
                assertThat(((javax.swing.JViewport) targetViewport).getViewPosition().y)
                        .isEqualTo(150);
                assertThat(find(c, JTextField.class).get(0)).isSameAs(field);
            }
        });
    }

    @Test
    void orientationActionsRetainToggleAndSelectedStateSemantics() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JListSelection<String> c = create();
            for (String action : new String[] {APPLY_HORIZONTAL_ACTION, APPLY_VERTICAL_ACTION}) {
                for (int initial : new int[] {SwingConstants.HORIZONTAL, SwingConstants.VERTICAL}) {
                    c.setOrientation(initial);
                    execute(c, action);
                    assertThat(c.getOrientation()).isNotEqualTo(initial);
                    assertThat(c.getActionMap().get(APPLY_HORIZONTAL_ACTION).getValue(Action.SELECTED_KEY))
                            .isEqualTo(c.getOrientation() == SwingConstants.HORIZONTAL);
                    assertThat(c.getActionMap().get(APPLY_VERTICAL_ACTION).getValue(Action.SELECTED_KEY))
                            .isEqualTo(c.getOrientation() == SwingConstants.VERTICAL);
                }
            }
        });
    }

    private static String tooltip(JList<?> list, int x, int y) {
        return list.getToolTipText(
                new java.awt.event.MouseEvent(list, java.awt.event.MouseEvent.MOUSE_MOVED, 0, 0, x, y, 0, false));
    }

    private static void execute(JListSelection<?> c, String name) {
        Action action = c.getActionMap().get(name);
        action.actionPerformed(null);
    }

    private static JList<?> source(JListSelection<?> c) {
        return find(c, JList.class).get(0);
    }

    private static JList<?> target(JListSelection<?> c) {
        return find(c, JList.class).get(1);
    }

    private static List<Object> values(JList<?> list) {
        return JLists.stream(list.getModel()).collect(Collectors.toList());
    }

    private static <T> List<T> find(Component c, Class<T> type) {
        List<T> result = new ArrayList<>();
        if (type.isInstance(c)) {
            result.add(type.cast(c));
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                result.addAll(find(child, type));
            }
        }
        return result;
    }
}
