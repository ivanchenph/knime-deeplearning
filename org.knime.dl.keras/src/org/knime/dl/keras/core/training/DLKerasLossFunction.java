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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
package org.knime.dl.keras.core.training;

import static org.knime.dl.util.DLUtils.Preconditions.checkNotNullOrEmpty;

import org.knime.dl.core.training.DLLossFunction;

/**
 * @author Marcel Wiedenmann, KNIME GmbH, Konstanz, Germany
 * @author Christian Dietz, KNIME GmbH, Konstanz, Germany
 */
public interface DLKerasLossFunction extends DLLossFunction {

	String getKerasIdentifier();

	@Override
	default String getBackendRepresentation() {
		return getKerasIdentifier();
	}

	public abstract static class DLKerasAbstractLossFunction implements DLKerasLossFunction {

		private final String m_name;

		private final String m_kerasIdentifier;

		protected DLKerasAbstractLossFunction(final String name, final String kerasIdentifier) {
			m_name = checkNotNullOrEmpty(name);
			m_kerasIdentifier = checkNotNullOrEmpty(kerasIdentifier);
		}

		@Override
		public String getName() {
			return m_name;
		}

		@Override
		public String getKerasIdentifier() {
			return m_kerasIdentifier;
		}
	}

	// TODO: we should add a "since" attribute to these losses to enable checking if they're available for the local
	// Keras installation. This implies changes in the installation testers on Python side as they have to extract the
	// libs' versions.

	public static class DLKerasMeanSquaredError extends DLKerasAbstractLossFunction {

		public DLKerasMeanSquaredError() {
			super("Mean squared error", "keras.losses.mse");
		}
	}

	public static class DLKerasMeanAbsoluteError extends DLKerasAbstractLossFunction {

		public DLKerasMeanAbsoluteError() {
			super("Mean absolute error", "keras.losses.mae");
		}
	}

	public static class DLKerasMeanAbsolutePercentageError extends DLKerasAbstractLossFunction {

		public DLKerasMeanAbsolutePercentageError() {
			super("Mean absolute percentage error", "keras.losses.mape");
		}
	}

	public static class DLKerasMeanSquaredLogarithmicError extends DLKerasAbstractLossFunction {

		public DLKerasMeanSquaredLogarithmicError() {
			super("Mean squared logarithmic error", "keras.losses.msle");
		}
	}

	public static class DLKerasSquaredHinge extends DLKerasAbstractLossFunction {

		public DLKerasSquaredHinge() {
			super("Squared hinge", "keras.losses.squared_hinge");
		}
	}

	public static class DLKerasHinge extends DLKerasAbstractLossFunction {

		public DLKerasHinge() {
			super("Hinge", "keras.losses.hinge");
		}
	}

	public static class DLKerasCategoricalHinge extends DLKerasAbstractLossFunction {

		public DLKerasCategoricalHinge() {
			super("Categorical hinge", "keras.losses.categorical_hinge");
		}
	}

	public static class DLKerasLogCosh extends DLKerasAbstractLossFunction {

		public DLKerasLogCosh() {
			super("Logcosh", "keras.losses.logcosh");
		}
	}

	public static class DLKerasCategoricalCrossEntropy extends DLKerasAbstractLossFunction {

		public DLKerasCategoricalCrossEntropy() {
			super("Categorical cross entropy", "keras.losses.categorical_crossentropy");
		}
	}

	public static class DLKerasSparseCategoricalCrossEntropy extends DLKerasAbstractLossFunction {

		public DLKerasSparseCategoricalCrossEntropy() {
			super("Sparse categorical cross entropy", "keras.losses.sparse_categorical_crossentropy");
		}
	}

	public static class DLKerasBinaryCrossEntropy extends DLKerasAbstractLossFunction {

		public DLKerasBinaryCrossEntropy() {
			super("Binary cross entropy", "keras.losses.binary_crossentropy");
		}
	}

	public static class DLKerasKullbackLeiblerDivergence extends DLKerasAbstractLossFunction {

		public DLKerasKullbackLeiblerDivergence() {
			super("Kullback-Leibler divergence", "keras.losses.kld");
		}
	}

	public static class DLKerasPoisson extends DLKerasAbstractLossFunction {

		public DLKerasPoisson() {
			super("Poisson", "keras.losses.poisson");
		}
	}

	public static class DLKerasCosineProximity extends DLKerasAbstractLossFunction {

		public DLKerasCosineProximity() {
			super("Cosine proximity", "keras.losses.cosine");
		}
	}
}