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
package org.knime.dl.core.data.convert;

import java.util.List;
import java.util.OptionalLong;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.dl.core.DLTensor;
import org.knime.dl.core.DLTensorSpec;
import org.knime.dl.core.data.DLWritableBuffer;

/**
 * @param <IE> the {@link DataCell element type} of the input collection
 * @param <O> the output {@link DLWritableBuffer buffer type}
 * @author Marcel Wiedenmann, KNIME GmbH, Konstanz, Germany
 * @author Christian Dietz, KNIME GmbH, Konstanz, Germany
 */
public final class DLCollectionDataValueToTensorConverterFactory<IE extends DataValue, O extends DLWritableBuffer>
	extends DLAbstractTensorDataValueToTensorConverterFactory<CollectionDataValue, O> {

	private final DLDataValueToTensorConverterFactory<IE, O> m_elementConverterFactory;

	/**
	 * @param elementConverterFactory the converter factory that is responsible for converting the elements of the input
	 *            collection
	 */
	public DLCollectionDataValueToTensorConverterFactory(
			final DLDataValueToTensorConverterFactory<IE, O> elementConverterFactory) {
		m_elementConverterFactory = elementConverterFactory;
	}

	@Override
	public String getIdentifier() {
		return getClass().getName() + "(" + m_elementConverterFactory.getIdentifier() + ")";
	}

	@Override
	public String getName() {
		return "Collection of " + m_elementConverterFactory.getName();
	}

	@Override
	public Class<CollectionDataValue> getSourceType() {
		return CollectionDataValue.class;
	}

	public Class<IE> getSourceElementType() {
		return m_elementConverterFactory.getSourceType();
	}

	@Override
	public Class<O> getBufferType() {
		return m_elementConverterFactory.getBufferType();
	}

	@Override
	public OptionalLong getDestCount(final List<DataColumnSpec> spec) {
		return OptionalLong.empty();
	}

	@Override
	public DLDataValueToTensorConverter<CollectionDataValue, O> createConverter() {
		final DLDataValueToTensorConverter<IE, O> elementConverter = m_elementConverterFactory.createConverter();
		return new DLAbstractTensorDataValueToTensorConverter<CollectionDataValue, O>() {

			@Override
			public void convertInternal(final CollectionDataValue input, final DLTensor<O> output) {
				final Iterable<? extends IE> casted = ((Iterable<? extends IE>) input);
				elementConverter.convert(casted, output);
			}
		};
	}

	@Override
	public int hashCode() {
		return m_elementConverterFactory.hashCode() * 37;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		final DLCollectionDataValueToTensorConverterFactory other = (DLCollectionDataValueToTensorConverterFactory) obj;
		return other.m_elementConverterFactory.equals(m_elementConverterFactory);
	}

	@Override
	protected long[] getDataShapeInternal(final CollectionDataValue input, final DLTensorSpec tensorSpec) {
		return m_elementConverterFactory.getDataShape(input.stream().map(e -> (IE) e).collect(Collectors.toList()), tensorSpec);
	}
}
