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
package org.knime.dl.python.prefs;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.dl.python.prefs.DLPythonPreferences.InstanceScopeConfigStorage;
import org.knime.python2.PythonCommand;
import org.knime.python2.PythonVersion;
import org.knime.python2.conda.Conda;
import org.knime.python2.conda.CondaEnvironmentIdentifier;
import org.knime.python2.config.CondaEnvironmentConfig;
import org.knime.python2.config.ObservableValue;
import org.knime.python2.config.PythonConfigStorage;
import org.osgi.service.prefs.Preferences;

/**
 * @author Benjamin Wilhelm, KNIME GmbH, Konstanz, Germany
 */
final class DLCondaEnvironmentConfig extends DLPythonAbstractEnvironmentConfig {

    private final SettingsModelString m_environmentDirectory;

    /** Only used for legacy support. See {@link #loadConfigFrom(PythonConfigStorage)} below. */
    private static final String LEGACY_CFG_KEY_KERAS_CONDA_ENV_NAME = "condaEnvironmentName";

    /** Only used for legacy support. See {@link #loadConfigFrom(PythonConfigStorage)} below. */
    private static final String LEGACY_PLACEHOLDER_CONDA_ENV_NAME = "<no environment>";

    /** Not managed by this config. Only needed to create command object. */
    private final SettingsModelString m_condaDirectory;

    /** Not meant for saving/loading. We just want observable values here to communicate with the view. */
    private final ObservableValue<CondaEnvironmentIdentifier[]> m_availableEnvironments;

    /** Required in {@link #loadDefaults()}. */
    private final CondaEnvironmentIdentifier m_defaultEnvironment;

    /**
     * @param environmentDirectoryConfigKey The identifier of the Conda environment directory config. Used for
     *            saving/loading the path to the environment's directory.
     * @param defaultEnvironment The initial Conda environment.
     * @param condaDirectory The settings model that specifies the Conda installation directory. Not saved/loaded or
     *            otherwise managed by this config.
     */
    DLCondaEnvironmentConfig(final String environmentDirectoryConfigKey, //
        final CondaEnvironmentIdentifier defaultEnvironment, //
        final SettingsModelString condaDirectory) {
        m_environmentDirectory =
            new SettingsModelString(environmentDirectoryConfigKey, defaultEnvironment.getDirectoryPath());
        m_availableEnvironments = new ObservableValue<>(new CondaEnvironmentIdentifier[]{defaultEnvironment});
        m_defaultEnvironment = defaultEnvironment;
        m_condaDirectory = condaDirectory;
    }

    @Override
    public PythonCommand getPythonCommand() {
        return Conda.createPythonCommand(PythonVersion.PYTHON3, m_condaDirectory.getStringValue(),
            m_environmentDirectory.getStringValue());
    }

    /**
     * @return The path to the directory of the Python Conda environment.
     */
    public SettingsModelString getEnvironmentDirectory() {
        return m_environmentDirectory;
    }

    /**
     * @return The list of the currently available Python Conda environments. Not meant for saving/loading.
     */
    public ObservableValue<CondaEnvironmentIdentifier[]> getAvailableEnvironments() {
        return m_availableEnvironments;
    }

    @Override
    public void saveConfigTo(final PythonConfigStorage storage) {
        storage.saveStringModel(m_environmentDirectory);
    }

    @Override
    public void loadConfigFrom(final PythonConfigStorage storage) {
        // Legacy support: we used to only save the environment's name, not the path to its directory. If only the name
        // is available, we need to convert it into the correct path.
        if (storage instanceof InstanceScopeConfigStorage) {
            final Preferences preferences = InstanceScopeConfigStorage.getInstanceScopePreferences();
            final boolean isLegacy = Platform.getPreferencesService().get( //
                m_environmentDirectory.getKey(), //
                null, //
                new Preferences[]{preferences}) == null;
            if (isLegacy) {
                final SettingsModelString environmentName =
                    new SettingsModelString(LEGACY_CFG_KEY_KERAS_CONDA_ENV_NAME, LEGACY_PLACEHOLDER_CONDA_ENV_NAME);
                storage.loadStringModel(environmentName);
                try {
                    final String environmentNameValue = environmentName.getStringValue();
                    final List<CondaEnvironmentIdentifier> environments =
                        new Conda(m_condaDirectory.getStringValue()).getEnvironments();
                    for (final CondaEnvironmentIdentifier environment : environments) {
                        if (environmentNameValue.equals(environment.getName())) {
                            m_environmentDirectory.setStringValue(environment.getDirectoryPath());
                            storage.saveStringModel(m_environmentDirectory);
                            break;
                        }
                    }
                } catch (final IOException ex) {
                    NodeLogger.getLogger(CondaEnvironmentConfig.class).debug(ex);
                    // Keep directory path's default value.
                }
                return;
            }
        }
        storage.loadStringModel(m_environmentDirectory);
    }

    /**
     * Load the default configuration
     */
    void loadDefaults() {
        m_environmentDirectory.setStringValue(m_defaultEnvironment.getDirectoryPath());
        m_availableEnvironments.setValue(new CondaEnvironmentIdentifier[]{m_defaultEnvironment});
    }
}
