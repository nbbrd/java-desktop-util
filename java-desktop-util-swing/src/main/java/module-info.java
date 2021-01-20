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

module nbbrd.desktop.swing {
    requires static lombok;
    requires static org.checkerframework.checker.qual;
    requires static nbbrd.design;

    requires java.desktop;
    requires java.logging;
    
    exports ec.util.completion;
    exports ec.util.completion.swing;
    exports ec.util.datatransfer;
    exports ec.util.grid;
    exports ec.util.grid.swing;
    exports ec.util.list.swing;
    exports ec.util.table.swing;
    exports ec.util.various.swing;
}
