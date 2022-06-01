/*
 * Copyright 2019 National Bank of Belgium
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
package internal;

import ec.util.various.swing.OnEDT;
import java.lang.ref.WeakReference;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Timer;

/**
 *
 * @author Philippe Charles
 */
enum Animator {
    INSTANCE;

    private static final int DEFAULT_FPS = 60;

    private final Clock clock;
    private final Timer timer;
    private final List<WeakReference<Animation>> animations;

    Animator() {
        this(DEFAULT_FPS, Clock.systemDefaultZone());
    }

    Animator(int fps, Clock clock) {
        this.clock = clock;
        this.timer = new Timer(1000 / fps, event -> broadcast(clock.millis()));
        this.animations = new ArrayList<>();
        timer.start();
    }

    private void broadcast(long currentTimeInMillis) {
        Iterator<WeakReference<Animation>> iterator = animations.iterator();
        while (iterator.hasNext()) {
            Animation o = iterator.next().get();
            if (o == null || !o.refresh(currentTimeInMillis)) {
                iterator.remove();
            }
        }
    }

    @OnEDT
    public void register(Animation animation) {
        if (animation.refresh(clock.millis())) {
            animations.add(new WeakReference<>(animation));
        }
    }
}
