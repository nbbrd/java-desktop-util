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

module nbbrd.desktop.os {
    requires static lombok;
    requires static org.checkerframework.checker.qual;
    requires static nbbrd.service;

    requires static com.sun.jna;
    requires static com.sun.jna.platform;

    requires java.desktop;
    requires java.logging;

    exports ec.util.desktop;

    provides ec.util.desktop.Desktop.Factory with
            ec.util.desktop.impl.WinDesktop.Factory,
            ec.util.desktop.impl.AwtDesktop.Factory,
            ec.util.desktop.impl.MacDesktop.Factory,
            ec.util.desktop.impl.XdgDesktop.Factory;

    uses ec.util.desktop.Desktop.Factory;
}
