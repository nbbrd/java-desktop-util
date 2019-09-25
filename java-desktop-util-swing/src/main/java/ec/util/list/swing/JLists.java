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

import java.awt.Component;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
public final class JLists {

    private JLists() {
        // static class
    }

    @NonNull
    public static <E> Stream<E> stream(@NonNull ListModel<E> model) {
        return IntStream.range(0, model.getSize()).mapToObj(model::getElementAt);
    }

    @NonNull
    public static <E> List<E> asList(@NonNull ListModel<E> model) {
        return new ListAdapter(model);
    }

    @NonNull
    public static <E> ListModel<E> emptyModel() {
        return new EmptyListModel<>();
    }

    @NonNull
    public static <E> ListModel<E> modelOf(@NonNull E... elements) {
        return modelOf(Arrays.asList(elements));
    }

    @NonNull
    public static <E> ListModel<E> modelOf(@NonNull List<E> elements) {
        return !elements.isEmpty() ? new ListModelImpl(elements) : emptyModel();
    }

    @NonNull
    public static <E> ListCellRenderer<E> cellRendererOf(@NonNull BiConsumer<JLabel, E> consumer) {
        return new LabelListCellRenderer<>((renderer, list, value, index, isSelected, cellHasFocus) -> consumer.accept(renderer, value));
    }

    @NonNull
    public static ListDataListener dataListenerOf(@NonNull Consumer<ListDataEvent> listener) {
        return new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                listener.accept(e);
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                listener.accept(e);
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                listener.accept(e);
            }
        };
    }

    @NonNull
    public static IntStream getSelectionIndexStream(@NonNull ListSelectionModel model) {
        return model.isSelectionEmpty()
                ? IntStream.empty()
                : IntStream.rangeClosed(model.getMinSelectionIndex(), model.getMaxSelectionIndex())
                .filter(model::isSelectedIndex);
    }

    public static void setSelectionIndexStream(@NonNull ListSelectionModel model, @NonNull IntStream selection) {
        model.clearSelection();
        addSelectionIndexStream(model, selection);
    }
    
    public static void addSelectionIndexStream(@NonNull ListSelectionModel model, @NonNull IntStream selection) {
        selection.forEach(x -> model.addSelectionInterval(x, x));
    }

    public static void removeSelectionIndexStream(@NonNull ListSelectionModel model, @NonNull IntStream selection) {
        selection.forEach(x -> model.removeSelectionInterval(x, x));
    }
    
    public static int getSelectionIndexSize(@NonNull ListSelectionModel model) {
        return (int) getSelectionIndexStream(model).count();
    }

    public static boolean isSingleSelectionIndex(@NonNull ListSelectionModel model) {
        return !model.isSelectionEmpty() && model.getMinSelectionIndex() == model.getMaxSelectionIndex();
    }
    
    // NOT public
    static void move(DefaultListModel from, DefaultListModel to, int[] selection, int dropIndex) {
        List reversedItems = new ArrayList(selection.length);
        for (int i = selection.length - 1; i >= 0; i--) {
            reversedItems.add(from.remove(selection[i]));
        }
        reversedItems.forEach(o -> to.insertElementAt(o, dropIndex));
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static final class ListAdapter<E> extends AbstractList<E> {

        private final ListModel<E> model;

        public ListAdapter(ListModel<E> model) {
            this.model = model;
        }

        @Override
        public E get(int index) {
            return model.getElementAt(index);
        }

        @Override
        public int size() {
            return model.getSize();
        }
    }

    private static final class EmptyListModel<E> extends AbstractListModel<E> {

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public E getElementAt(int index) {
            throw new IndexOutOfBoundsException();
        }
    }

    private static final class ListModelImpl<E> extends AbstractListModel<E> {

        private final List<E> data;

        public ListModelImpl(List<E> data) {
            this.data = data;
        }

        @Override
        public int getSize() {
            return data.size();
        }

        @Override
        public E getElementAt(int index) {
            return data.get(index);
        }
    }

    private static final class LabelListCellRenderer<T> implements ListCellRenderer<T> {

        private final LabelListCellConsumer<T> consumer;
        private final DefaultListCellRenderer delegate;

        public LabelListCellRenderer(@NonNull LabelListCellConsumer<T> consumer) {
            this.consumer = consumer;
            this.delegate = new DefaultListCellRenderer();
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus) {
            delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            consumer.accept(delegate, list, value, index, isSelected, cellHasFocus);
            return delegate;
        }

        @FunctionalInterface
        interface LabelListCellConsumer<T> {

            void accept(JLabel renderer, JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus);
        }
    }
    //</editor-fold>
}
