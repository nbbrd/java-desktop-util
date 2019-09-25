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
package ec.util.completion.swing;

import ec.util.completion.AutoCompletionSources;
import java.awt.Component;
import java.util.function.Function;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A specialized renderer used by JAutoCompletion that highlights terms.
 *
 * @author Philippe Charles
 * @param <T>
 */
public class CustomListCellRenderer<T> extends DefaultListCellRenderer {

    @NonNull
    public static <T> DefaultListCellRenderer of(@NonNull Function<T, String> toValueAsString, @NonNull Function<T, String> toToolTipText) {
        return new CustomListCellRenderer<T>() {
            @Override
            protected String getValueAsString(T value) {
                return toValueAsString.apply(value);
            }

            @Override
            protected String toToolTipText(String term, JList list, T value, int index, boolean isSelected, boolean cellHasFocus) {
                return toToolTipText.apply(value);
            }
        };
    }

    private final boolean highlightTerm;

    public CustomListCellRenderer() {
        this(true);
    }

    public CustomListCellRenderer(boolean highlightTerm) {
        this.highlightTerm = highlightTerm;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel result = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        String term = list.getModel() instanceof CustomListModel ? ((CustomListModel) list.getModel()).getTerm() : null;
        setText(toString(term, list, (T) value, index, isSelected, cellHasFocus));
        setIcon(toIcon(term, list, (T) value, index, isSelected, cellHasFocus));
        setToolTipText(toToolTipText(term, list, (T) value, index, isSelected, cellHasFocus));
        return result;
    }

    /**
     * Renders a value as a String.
     *
     * @param term
     * @param list
     * @param value
     * @param index
     * @param isSelected
     * @param cellHasFocus
     * @return
     */
    @Nullable
    protected String toString(String term, JList list, T value, int index, boolean isSelected, boolean cellHasFocus) {
        String valueAsString = getValueAsString(value);
        if (!highlightTerm || term == null || term.isEmpty()) {
            return valueAsString;
        }
        int beginIndex = getNormalizedString(valueAsString).indexOf(getNormalizedString(term));
        if (beginIndex == -1) {
            return valueAsString;
        }
        int endIndex = beginIndex + term.length();
        return "<html>" + valueAsString.substring(0, beginIndex) + "<b>" + valueAsString.substring(beginIndex, endIndex) + "</b>" + valueAsString.substring(endIndex);
    }

    /**
     * Renders a value as an Icon.
     *
     * @param term
     * @param list
     * @param value
     * @param index
     * @param isSelected
     * @param cellHasFocus
     * @return
     */
    @Nullable
    protected Icon toIcon(String term, JList list, T value, int index, boolean isSelected, boolean cellHasFocus) {
        return null;
    }

    /**
     * Renders a value as a tooltip text.
     *
     * @param term
     * @param list
     * @param value
     * @param index
     * @param isSelected
     * @param cellHasFocus
     * @return
     */
    @Nullable
    protected String toToolTipText(String term, JList list, T value, int index, boolean isSelected, boolean cellHasFocus) {
        return null;
    }

    /**
     * Format a value as a string.<br>Default behavior uses
     * {@link Object#toString()}.
     *
     * @param value the value to be formatted
     * @return
     */
    @NonNull
    protected String getValueAsString(@NonNull T value) {
        return value.toString();
    }

    /**
     * Returns a normalized string used by the highlighter.<br>Default behavior
     * uses {@link AutoCompletionSources#normalize(java.lang.String)}.
     *
     * @param input the string to be normalized
     * @return a normalized string
     */
    @NonNull
    protected String getNormalizedString(@NonNull String input) {
        return AutoCompletionSources.normalize(input);
    }
}
