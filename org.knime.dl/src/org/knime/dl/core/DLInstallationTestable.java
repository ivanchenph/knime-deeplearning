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
package org.knime.dl.core;

/**
 * @param <T> The type of the external context.
 * @author Marcel Wiedenmann, KNIME GmbH, Konstanz, Germany
 * @author Christian Dietz, KNIME GmbH, Konstanz, Germany
 */
public interface DLInstallationTestable<T> {

    /**
     * Checks if the external dependencies of this instance are available (if any). Throws an exception if they are not
     * or if testing their availability timed out or was interrupted.
     * <P>
     * Executing installation tests for external dependencies might be costly. Thus, implementations of this method can
     * cache the results of their first invocation to improve the response time of subsequent calls.
     *
     * @param context The external context.
     * @param forceRefresh if true, possibly cached test results from a previous check will be discarded and the check
     *            will be redone. Otherwise, previous test results will be used if available.
     * @param timeout timeout in milliseconds after which the installation test will be interrupted
     * @param cancelable to check if the operation has been canceled
     * @throws DLMissingDependencyException if the external dependencies of this network type are unavailable
     * @throws DLInstallationTestTimeoutException if the installation test timed out or was interrupted in terms of
     *             threading
     * @throws DLCanceledExecutionException if the operation has been canceled
     */
    void checkAvailability(final T context, boolean forceRefresh, int timeout, DLCancelable cancelable)
        throws DLMissingDependencyException, DLInstallationTestTimeoutException, DLCanceledExecutionException;
}
