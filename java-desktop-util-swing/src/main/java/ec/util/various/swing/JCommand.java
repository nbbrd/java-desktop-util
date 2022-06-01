/*
 * Copyright 2013 National Bank of Belgium
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
package ec.util.various.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.EventListener;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A command pattern that targets Swing components. It is similar to the
 * {@link  Action} class but with more features.
 *
 * @author Philippe Charles
 * @param <C> the type of component on which the command is executed
 */
public abstract class JCommand<C> {

    @NonNull
    public static <X> JCommand<X> of(@NonNull Consumer<X> consumer) {
        return new JCommand<X>() {
            @Override
            public void execute(@NonNull X component) throws Exception {
                consumer.accept(component);
            }
        };
    }

    private static final Logger LOGGER = Logger.getLogger(JCommand.class.getName());

    /**
     * Executes the command on the specified component.
     *
     * @param component a non-null component
     * @throws Exception if something went wrong during the execution
     */
    public abstract void execute(@NonNull C component) throws Exception;

    /**
     * Executes the command on the specified component but catches and returns
     * exceptions.
     *
     * @param component a non-null component
     * @return an exception if one were thrown; null otherwise
     */
    @Nullable
    public Exception executeSafely(@NonNull C component) {
        try {
            execute(component);
            return null;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "While executing command", ex);
            return ex;
        }
    }

    /**
     * Checks if this command should be enabled on the specified component.
     *
     * @param component a non-null component
     * @return true if enabled; false otherwise
     */
    public boolean isEnabled(@NonNull C component) {
        return true;
    }

    /**
     * Checks if this command should be selected on the specified component.
     *
     * @param component a non-null component
     * @return true if selected; false otherwise
     */
    public boolean isSelected(@NonNull C component) {
        return false;
    }

    /**
     * Converts this command into an Action to be used in the Swing API.
     *
     * @param component a non-null component
     * @return a non-null action
     */
    @NonNull
    public ActionAdapter toAction(@NonNull C component) {
        return new ActionAdapter(component);
    }

    /**
     * An improved action that takes care of tedious code such as listeners.
     */
    public class ActionAdapter extends AbstractAction {

        @NonNull
        private final C component;
        private boolean listening;

        public ActionAdapter(@NonNull C component) {
            this.component = component;
            this.listening = true;
            refreshActionState();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            listening = false;
            if (isEnabled()) {
                Exception ex = JCommand.this.executeSafely(component);
                if (ex != null) {
                    handleException(e, ex);
                }
            }
            listening = true;
            refreshActionState();
        }

        public void handleException(ActionEvent event, Exception ex) {
        }

        public final void refreshActionState() {
            if (listening) {
                setEnabled(JCommand.this.isEnabled(component));
                putValue(Action.SELECTED_KEY, JCommand.this.isSelected(component));
            }
        }

        /**
         * Register a property change listener on a component. This listener
         * uses a weak reference to avoid memory leaks.
         *
         * @param source a non-null component
         * @param properties a list of properties to listen to
         * @return itself
         */
        @NonNull
        public ActionAdapter withWeakPropertyChangeListener(@NonNull Component source, @NonNull String... properties) {
            PropertyChangeListener realListener = evt -> refreshActionState();
            putValue("PropertyChangeListener", realListener);
            if (properties.length > 0) {
                for (final String property : properties) {
                    source.addPropertyChangeListener(property, new WeakPropertyChangeListener(realListener) {
                        @Override
                        protected void unregister(@NonNull Object source) {
                            ((Component) source).removePropertyChangeListener(property, this);
                        }
                    });
                }
            } else {
                source.addPropertyChangeListener(new WeakPropertyChangeListener(realListener) {
                    @Override
                    protected void unregister(@NonNull Object source) {
                        ((Component) source).removePropertyChangeListener(this);
                    }
                });
            }
            return this;
        }

        /**
         * Register a list selection listener on a list selection model. This
         * listener uses a weak reference to avoid memory leaks.
         *
         * @param source a non-null list selection model
         * @return itself
         */
        @NonNull
        public ActionAdapter withWeakListSelectionListener(@NonNull ListSelectionModel source) {
            ListSelectionListener realListener = evt -> refreshActionState();
            putValue("ListSelectionListener", realListener);
            source.addListSelectionListener(new WeakListSelectionListener(realListener) {
                @Override
                protected void unregister(@NonNull Object source) {
                    ((ListSelectionModel) source).removeListSelectionListener(this);
                }
            });
            return this;
        }

        @Deprecated
        public void registerPropertyChangeListener(@NonNull Container source) {
            withWeakPropertyChangeListener(source);
        }

        @Deprecated
        public void registerListSelectionListener(@NonNull ListSelectionModel source) {
            withWeakListSelectionListener(source);
        }
    }

    public static abstract class WeakEventListener<T extends EventListener> implements EventListener {

        protected final WeakReference<T> delegate;

        public WeakEventListener(@NonNull T delegate) {
            this.delegate = new WeakReference<>(delegate);
        }

        abstract protected void unregister(@NonNull Object source);
    }

    private abstract static class WeakPropertyChangeListener extends WeakEventListener<PropertyChangeListener> implements PropertyChangeListener {

        public WeakPropertyChangeListener(@NonNull PropertyChangeListener delegate) {
            super(delegate);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            PropertyChangeListener listener = delegate.get();
            if (listener != null) {
                listener.propertyChange(evt);
            } else {
                unregister(evt.getSource());
            }
        }
    }

    private abstract static class WeakListSelectionListener extends WeakEventListener<ListSelectionListener> implements ListSelectionListener {

        public WeakListSelectionListener(@NonNull ListSelectionListener delegate) {
            super(delegate);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionListener listener = delegate.get();
            if (listener != null) {
                listener.valueChanged(e);
            } else {
                unregister(e.getSource());
            }
        }
    }
}
