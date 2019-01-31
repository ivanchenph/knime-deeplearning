/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 */
package org.knime.dl.python.core.data.serde;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.knime.dl.core.data.DLBuffer;
import org.knime.dl.python.core.data.DLPythonStringBuffer;
import org.knime.python.typeextension.Serializer;
import org.knime.python.typeextension.SerializerFactory;

import com.google.common.base.Charsets;

/**
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public class DLPythonStringBufferSerializerFactory extends SerializerFactory<DLPythonStringBuffer>
    implements DLSerializerFactory {

    /**
     * The unique identifier of this serializer factory.
     */
    public static final String IDENTIFIER = "org.knime.dl.python.core.data.serde.DLPythonStringBufferSerializerFactory";

    /**
     */
    public DLPythonStringBufferSerializerFactory() {
        super(DLPythonStringBuffer.class);
    }

    @Override
    public Class<? extends DLBuffer> getBufferType() {
        return DLPythonStringBuffer.class;
    }

    @Override
    public Serializer<? extends DLPythonStringBuffer> createSerializer() {
        return DLPythonStringBufferSerializerFactory::createBytes;
    }

    private static byte[] createBytes(DLPythonStringBuffer value) {
        // Note that casting to int should be fine because the data is stored in a array which is indexed by int
        final int nextRead = (int)value.getNextReadPosition();
        final int size = (int)(value.size() - nextRead);
        String[] storage = value.getStorageForReading(nextRead, size);
        final byte[][] values = new byte[size][];
        final int[] lengths = new int[size];
        for (int i = nextRead; i < nextRead + size; i++) {
            values[i] = storage[i].getBytes(Charsets.UTF_8);
            lengths[i] = values[i].length;
        }
        int totalLength = (size + 1) * Integer.BYTES + Arrays.stream(lengths).sum();
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(size);
        Arrays.stream(lengths).forEachOrdered(buffer::putInt);
        Arrays.stream(values).forEachOrdered(buffer::put);
        return buffer.array();
    }

}
