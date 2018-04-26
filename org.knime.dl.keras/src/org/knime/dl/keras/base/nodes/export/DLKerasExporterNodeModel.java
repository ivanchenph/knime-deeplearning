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
package org.knime.dl.keras.base.nodes.export;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.dl.core.export.DLNetworkExporterRegistry;
import org.knime.dl.keras.base.portobjects.DLKerasNetworkPortObjectBase;

/**
 * @author Benjamin Wilhelm, KNIME GmbH, Konstanz, Germany
 */
final class DLKerasExporterNodeModel extends NodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(DLKerasExporterNodeModel.class);

    private static final DLNetworkExporterRegistry EXPORTER_REGISTRY = DLNetworkExporterRegistry.getInstance();

    private static final String CFG_KEY_EXPORTER_ID = "exporter";

    private static final String CFG_KEY_FILE_PATH = "filepath";

    private static final String CFG_KEY_OVERWRITE = "overwrite";

    private final SettingsModelStringArray m_exporterId = createExporterIdSettingsModel();

    private final SettingsModelString m_filePath = createFilePathSettingsModel();

    private final SettingsModelBoolean m_overwrite = createOverwriteSettingsModel();

    static SettingsModelStringArray createExporterIdSettingsModel() {
        return new SettingsModelStringArray(CFG_KEY_EXPORTER_ID, new String[]{"", ""});
    }

    static SettingsModelString createFilePathSettingsModel() {
        return new SettingsModelString(CFG_KEY_FILE_PATH, "");
    }

    static SettingsModelBoolean createOverwriteSettingsModel() {
        return new SettingsModelBoolean(CFG_KEY_OVERWRITE, false);
    }

    protected DLKerasExporterNodeModel() {
        super(new PortType[]{DLKerasNetworkPortObjectBase.TYPE}, null);
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        // TODO implement
        return new PortObjectSpec[]{};
    }

    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        // TODO implement
        return new PortObject[]{};
    }

    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to do
    }

    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to do
    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_exporterId.saveSettingsTo(settings);
        m_filePath.saveSettingsTo(settings);
        m_overwrite.saveSettingsTo(settings);
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_exporterId.validateSettings(settings);
        m_filePath.validateSettings(settings);
        m_overwrite.validateSettings(settings);
    }

    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_exporterId.loadSettingsFrom(settings);
        m_filePath.loadSettingsFrom(settings);
        m_overwrite.loadSettingsFrom(settings);
    }

    @Override
    protected void reset() {
        // nothing to do
    }
}