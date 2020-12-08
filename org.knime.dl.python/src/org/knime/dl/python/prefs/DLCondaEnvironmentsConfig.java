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
 * History
 *   Jun 12, 2020 (benjamin): created
 */
package org.knime.dl.python.prefs;

import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.python2.conda.CondaEnvironmentIdentifier;
import org.knime.python2.config.CondaEnvironmentsConfig;
import org.knime.python2.config.PythonConfigStorage;
import org.knime.python2.prefs.PythonPreferences;

/**
 * @author Benjain Wilhelm, KNIME GmbH, Konstanz, Germany
 */
final class DLCondaEnvironmentsConfig implements DLPythonEnvironmentsConfig {

    private static final String CFG_KEY_CONDA_DIRECTORY_PATH = "condaDirectoryPath";

    private static final String CFG_KEY_KERAS_CONDA_ENV_NAME_DIR = "kerasCondaEnvironmentDirectoryPath";

    private static final String CFG_KEY_TF2_CONDA_ENV_NAME_DIR = "tf2CondaEnvironmentDirectoryPath";

    static final String PLACEHOLDER_CONDA_ENV_NAME = "no environment available";

    static final String PLACEHOLDER_CONDA_ENV_DIR = "no_conda_environment_selected";

    private static final String CFG_KEY_DUMMY = "dummy";

    private final SettingsModelString m_condaDirectory;

    private final SettingsModelString m_condaInstallationInfo;

    private final SettingsModelString m_condaInstallationError;

    private final DLCondaEnvironmentConfig m_kerasEnvironmentConfig;

    private final DLCondaEnvironmentConfig m_tf2EnvironmentConfig;

    DLCondaEnvironmentsConfig() {
        // Conda config
        m_condaDirectory =
            new SettingsModelString(CFG_KEY_CONDA_DIRECTORY_PATH, PythonPreferences.getCondaInstallationPath());
        m_condaInstallationInfo = new SettingsModelString(CFG_KEY_DUMMY, "");
        m_condaInstallationError = new SettingsModelString(CFG_KEY_DUMMY, "");

        // Environments
        final String condaDirectoryPath = m_condaDirectory.getStringValue();
        m_kerasEnvironmentConfig = new DLCondaEnvironmentConfig(CFG_KEY_KERAS_CONDA_ENV_NAME_DIR,
            getDefaultCondaEnvironment(condaDirectoryPath), m_condaDirectory);
        m_tf2EnvironmentConfig = new DLCondaEnvironmentConfig(CFG_KEY_TF2_CONDA_ENV_NAME_DIR,
            getDefaultCondaEnvironment(condaDirectoryPath), m_condaDirectory);
    }

    private static CondaEnvironmentIdentifier getDefaultCondaEnvironment(final String condaDirectoryPath) {
        // TODO: change to sensible default
        return new CondaEnvironmentIdentifier(PLACEHOLDER_CONDA_ENV_NAME, PLACEHOLDER_CONDA_ENV_DIR);
    }

    @Override
    public DLCondaEnvironmentConfig getKerasConfig() {
        return m_kerasEnvironmentConfig;
    }

    @Override
    public DLCondaEnvironmentConfig getTF2Config() {
        return m_tf2EnvironmentConfig;
    }

    /**
     * @return The path to the conda directory
     */
    public SettingsModelString getCondaDirectoryPath() {
        return m_condaDirectory;
    }

    /**
     * @return The installation status message of the local Conda installation. Not meant for saving/loading.
     */
    public SettingsModelString getCondaInstallationInfo() {
        return m_condaInstallationInfo;
    }

    /**
     * @return The installation error message of the local Conda installation. Not meant for saving/loading.
     */
    public SettingsModelString getCondaInstallationError() {
        return m_condaInstallationError;
    }

    @Override
    public void saveConfigTo(final PythonConfigStorage storage) {
        storage.saveStringModel(m_condaDirectory);
        m_kerasEnvironmentConfig.saveConfigTo(storage);
        m_tf2EnvironmentConfig.saveConfigTo(storage);
    }

    @Override
    public void loadConfigFrom(final PythonConfigStorage storage) {
        storage.loadStringModel(m_condaDirectory);
        m_kerasEnvironmentConfig.loadConfigFrom(storage);
        m_tf2EnvironmentConfig.loadConfigFrom(storage);
    }

    /**
     * Load the default configuration
     */
    void loadDefaults() {
        m_condaDirectory.setStringValue(CondaEnvironmentsConfig.getDefaultCondaInstallationDirectory());
        m_condaInstallationInfo.setStringValue("");
        m_condaInstallationError.setStringValue("");
        m_kerasEnvironmentConfig.loadDefaults();
        m_tf2EnvironmentConfig.loadDefaults();
    }
}
