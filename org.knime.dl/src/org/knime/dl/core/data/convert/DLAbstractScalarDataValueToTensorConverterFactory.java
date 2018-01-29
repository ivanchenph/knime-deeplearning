package org.knime.dl.core.data.convert;

import java.util.List;

import org.knime.core.data.DataValue;
import org.knime.dl.core.data.DLWritableBuffer;

public abstract class DLAbstractScalarDataValueToTensorConverterFactory <I extends DataValue, O extends DLWritableBuffer>
		implements DLDataValueToTensorConverterFactory<I, O> {

	@Override
	public long[] getDataShape(List<? extends DataValue> input) {
		if (input.isEmpty()) {
			throw new IllegalArgumentException("Can't infer shape from empty list of data values.");
		}
		DataValue val = input.get(0);
		// runtime check if type matches
		try {
			I casted = (I) val;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("The provided values are not compatible with the converter.");
		}
		return new long[] {input.size()};
	}
}