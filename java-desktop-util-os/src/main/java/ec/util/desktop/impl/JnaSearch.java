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
package ec.util.desktop.impl;

import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.util.Factory;
import com.sun.jna.platform.win32.COM.util.annotation.ComInterface;
import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;
import com.sun.jna.platform.win32.COM.util.annotation.ComObject;
import com.sun.jna.platform.win32.COM.util.annotation.ComProperty;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Philippe Charles
 */
final class JnaSearch extends WinSearch {

    private final Factory factory = new Factory();

    @Override
    public List<File> getFilesByName(String query) throws IOException {
        try (Connection conn = factory.createObject(Connection.class)) {
            conn.Open("Provider=Search.CollatorDSO;Extended Properties='Application=Windows';");
            try (final Recordset rs = conn.Execute("SELECT System.ItemUrl FROM SYSTEMINDEX WHERE SCOPE='file:' AND System.FileName like '%" + escapeQuery(query) + "%'")) {
                List<File> result = new ArrayList<>();
                if (!(rs.getBOF() && rs.getEOF())) {
                    rs.MoveFirst();
                    while (!rs.getEOF()) {
                        result.add(toFile(rs.getFields().getItem(0)));
                        rs.MoveNext();
                    }
                }
                return result;
            }
        } catch (COMException ex) {
            throw new IOException(ex);
        }
    }

    private static File toFile(Field field) {
        return new File(field.getValue().toString().replace("file:", ""));
    }

    private static String escapeQuery(String query) {
        return query.replace("'", "");
    }

    @ComObject(progId = "ADODB.Connection")
    public interface Connection extends Closeable {

        @ComMethod
        void Open(String connectionString);

        @ComMethod
        void Close();

        @ComMethod
        Recordset Execute(String commandText);

        @Override
        default void close() throws IOException {
            Close();
        }
    }

    @ComInterface
    public interface Recordset extends Closeable {

        @ComMethod
        void Close();

        @ComProperty
        boolean getBOF();

        @ComProperty
        boolean getEOF();

        @ComMethod
        void MoveFirst();

        @ComMethod
        void MoveNext();

        @ComProperty
        Fields getFields();

        @Override
        default void close() throws IOException {
            Close();
        }
    }

    @ComInterface
    public interface Fields {

        @ComProperty
        Field getItem(int index);
    }

    @ComInterface
    public interface Field {

        @ComProperty
        Object getValue();
    }
}
