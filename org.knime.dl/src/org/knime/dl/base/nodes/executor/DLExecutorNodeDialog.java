/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 * History
 *   Jun 2, 2017 (marcel): created
 */
package org.knime.dl.base.nodes.executor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.dl.base.portobjects.DLNetworkPortObject;
import org.knime.dl.base.portobjects.DLNetworkPortObjectSpec;
import org.knime.dl.core.DLLayerDataSpec;
import org.knime.dl.core.DLNetworkSpec;
import org.knime.dl.core.backend.DLBackendRegistry;
import org.knime.dl.core.backend.DLProfile;
import org.knime.dl.util.DLUtils;

/**
 * @author Marcel Wiedenmann, KNIME, Konstanz, Germany
 * @author Christian Dietz, KNIME, Konstanz, Germany
 */
final class DLExecutorNodeDialog extends NodeDialogPane {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(DLExecutorNodeModel.class);

    private DLGeneralModelConfig m_generalCfg;

    private ArrayList<DLInputLayerDataPanel> m_inputPanels;

    private LinkedHashMap<String, DLOutputLayerDataPanel> m_outputPanels;

    private JPanel m_root;

    private GridBagConstraints m_rootConstr;

    private JScrollPane m_rootScrollableView;

    private JButton m_outputsAddBtn;

    private DLNetworkSpec m_lastIncomingNetworkSpec;

    private DLNetworkSpec m_lastConfiguredNetworkSpec;

    /**
     * Creates a new dialog.
     */
    public DLExecutorNodeDialog() {
        resetSettings();
        addTab("Options", m_rootScrollableView);
    }

    private void resetSettings() {
        m_generalCfg = DLExecutorNodeModel.createGeneralModelConfig();
        m_inputPanels = new ArrayList<>();
        m_outputPanels = new LinkedHashMap<>();
        // root panel; content will be generated based on input network specs
        m_root = new JPanel(new GridBagLayout());
        m_rootConstr = new GridBagConstraints();
        resetDialog();
        m_rootScrollableView = new JScrollPane();
        final JPanel rootWrapper = new JPanel(new BorderLayout());
        rootWrapper.add(m_root, BorderLayout.NORTH);
        m_rootScrollableView.setViewportView(rootWrapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        if (specs[DLExecutorNodeModel.IN_NETWORK_PORT_IDX] == null) {
            throw new NotConfigurableException("Input deep learning network port object is missing.");
        }
        if (specs[DLExecutorNodeModel.IN_DATA_PORT_IDX] == null) {
            throw new NotConfigurableException("Input data table is missing.");
        }
        if (!DLNetworkPortObject.TYPE.acceptsPortObjectSpec(specs[DLExecutorNodeModel.IN_NETWORK_PORT_IDX])) {
            throw new NotConfigurableException("Input port object is not a valid deep learning network port object.");
        }
        if (((DataTableSpec)specs[DLExecutorNodeModel.IN_DATA_PORT_IDX]).getNumColumns() == 0) {
            throw new NotConfigurableException("Input table has no columns.");
        }

        final DLNetworkPortObjectSpec currNetworkSpec =
            (DLNetworkPortObjectSpec)specs[DLExecutorNodeModel.IN_NETWORK_PORT_IDX];
        final DataTableSpec currTableSpec = (DataTableSpec)specs[DLExecutorNodeModel.IN_DATA_PORT_IDX];

        if (currNetworkSpec.getNetworkSpec() == null) {
            throw new NotConfigurableException("Input port object's deep learning network specs are missing.");
        }
        if (currNetworkSpec.getProfile() == null) {
            throw new NotConfigurableException("Input port object's deep learning profile is missing.");
        }

        final DLNetworkSpec networkSpec = currNetworkSpec.getNetworkSpec();
        m_lastIncomingNetworkSpec = networkSpec;
        final DLProfile profile = currNetworkSpec.getProfile();

        if (networkSpec.getInputSpecs().length == 0) {
            LOGGER.warn("Input deep learning network has no input specs.");
        }
        if (networkSpec.getOutputSpecs().length == 0 && networkSpec.getIntermediateOutputSpecs().length == 0) {
            LOGGER.warn("Input deep learning network has no output specs.");
        }
        if (profile.size() == 0) {
            throw new NotConfigurableException("Input deep learning network has no associated back end.");
        }

        final boolean networkChanged = m_lastConfiguredNetworkSpec == null || !m_lastConfiguredNetworkSpec.equals(m_lastIncomingNetworkSpec);

        if (m_lastConfiguredNetworkSpec == null || networkChanged) {
            resetDialog();
            createDialogContent(currNetworkSpec, currTableSpec);
        }

        if (m_lastConfiguredNetworkSpec == null || !networkChanged) {
            try {
                m_generalCfg.loadFromSettings(settings);
            } catch (final InvalidSettingsException e) {
                // default settings
            }
            try {
                if (settings.containsKey(DLExecutorNodeModel.CFG_KEY_INPUTS)) {
                    final NodeSettingsRO inputSettings = settings.getNodeSettings(DLExecutorNodeModel.CFG_KEY_INPUTS);
                    for (final DLInputLayerDataPanel inputPanel : m_inputPanels) {
                        if (inputSettings.containsKey(inputPanel.getConfig().getInputLayerDataName())) {
                            inputPanel.loadFromSettings(inputSettings, specs);
                        } else {
                            inputPanel.getInputColumns()
                                .loadConfiguration(inputPanel.getConfig().getInputColumnsModel(), currTableSpec);
                            inputPanel.getInputColumns()
                                .updateWithNewConfiguration(inputPanel.getConfig().getInputColumnsModel());
                        }
                    }
                }
                if (settings.containsKey(DLExecutorNodeModel.CFG_KEY_OUTPUTS)) {
                    final NodeSettingsRO outputSettings = settings.getNodeSettings(DLExecutorNodeModel.CFG_KEY_OUTPUTS);
                    for (final String layerName : outputSettings) {
                        if (!m_outputPanels.containsKey(layerName)) {
                            // add output to the dialog (when loading the dialog for the first time)
                            final Optional<DLLayerDataSpec> spec = DLUtils.Networks.findSpec(layerName,
                                networkSpec.getOutputSpecs(), networkSpec.getIntermediateOutputSpecs());
                            if (spec.isPresent()) {
                                m_outputPanels.put(layerName,
                                    createOutputPanel(spec.get(), m_generalCfg.getBackendModel()));
                            }
                        }
                    }
                    for (final DLOutputLayerDataPanel outputPanel : m_outputPanels.values()) {
                        m_outputPanels.get(outputPanel.getConfig().getOutputLayerDataName())
                            .loadFromSettings(outputSettings, specs);
                    }
                }
            } catch (final Exception e) {
                m_outputPanels.clear();
                // default input settings
            }
        } else {
            for (final DLInputLayerDataPanel inputPanel : m_inputPanels) {
                inputPanel.getInputColumns().loadConfiguration(inputPanel.getConfig().getInputColumnsModel(),
                    currTableSpec);
                inputPanel.getInputColumns().updateWithNewConfiguration(inputPanel.getConfig().getInputColumnsModel());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        m_generalCfg.saveToSettings(settings);

        final NodeSettingsWO inputSettings = settings.addNodeSettings(DLExecutorNodeModel.CFG_KEY_INPUTS);
        for (final DLInputLayerDataPanel inputPanel : m_inputPanels) {
            inputPanel.saveToSettings(inputSettings);
        }

        final NodeSettingsWO outputSettings = settings.addNodeSettings(DLExecutorNodeModel.CFG_KEY_OUTPUTS);
        for (final DLOutputLayerDataPanel outputPanel : m_outputPanels.values()) {
            outputPanel.saveToSettings(outputSettings);
        }

        final SettingsModelStringArray outputOrder =
            DLExecutorNodeModel.createOutputOrderSettingsModel(m_outputPanels.size());
        final String[] outputs = new String[m_outputPanels.size()];

        int i = 0;
        for (final String output : m_outputPanels.keySet()) {
            outputs[i++] = output;
        }
        outputOrder.setStringArrayValue(outputs);
        outputOrder.saveSettingsTo(settings);

        m_lastConfiguredNetworkSpec = m_lastIncomingNetworkSpec;
    }

    private void resetDialog() {
        m_inputPanels.clear();
        m_outputPanels.clear();
        m_root.removeAll();
        m_rootConstr.gridx = 0;
        m_rootConstr.gridy = 0;
        m_rootConstr.gridwidth = 1;
        m_rootConstr.gridheight = 1;
        m_rootConstr.weightx = 1;
        m_rootConstr.weighty = 0;
        m_rootConstr.anchor = GridBagConstraints.WEST;
        m_rootConstr.fill = GridBagConstraints.BOTH;
        m_rootConstr.insets = new Insets(5, 5, 5, 5);
        m_rootConstr.ipadx = 0;
        m_rootConstr.ipady = 0;
    }

    private void createDialogContent(final DLNetworkPortObjectSpec portObjectSpec, final DataTableSpec tableSpec)
        throws NotConfigurableException {
        final DLNetworkSpec networkSpec = portObjectSpec.getNetworkSpec();
        final DLProfile profile = portObjectSpec.getProfile();

        // general settings:
        final JPanel generalPanel = new JPanel(new GridBagLayout());
        generalPanel.setBorder(BorderFactory.createTitledBorder("General Settings"));
        final GridBagConstraints generalPanelConstr = new GridBagConstraints();
        generalPanelConstr.gridx = 0;
        generalPanelConstr.gridy = 0;
        generalPanelConstr.weightx = 1;
        generalPanelConstr.anchor = GridBagConstraints.WEST;
        generalPanelConstr.fill = GridBagConstraints.VERTICAL;
        m_generalCfg.getBackendModel().setStringValue(DLBackendRegistry.getPreferredBackend(profile).getIdentifier());
        // back end selection
        final DialogComponentStringSelection dcBackend = new DialogComponentStringSelection(
            m_generalCfg.getBackendModel(), "Back end", getAvailableBackends(profile));
        generalPanel.add(dcBackend.getComponentPanel(), generalPanelConstr);
        generalPanelConstr.gridy++;
        // batch size input
        final DialogComponentNumber cdBatchSize =
            new DialogComponentNumber(m_generalCfg.getBatchSizeModel(), "Input batch size", 100);
        generalPanel.add(cdBatchSize.getComponentPanel(), generalPanelConstr);
        generalPanelConstr.gridy++;
        final DialogComponentBoolean appendColumnComponent =
            new DialogComponentBoolean(m_generalCfg.getKeepInputColumns(), "Keep input columns");
        generalPanel.add(appendColumnComponent.getComponentPanel(), generalPanelConstr);
        m_root.add(generalPanel, m_rootConstr);
        m_rootConstr.gridy++;

        // input settings:
        final JPanel inputsSeparator = new JPanel(new GridBagLayout());
        final GridBagConstraints inputsSeparatorLabelConstr = new GridBagConstraints();
        inputsSeparatorLabelConstr.gridwidth = 1;
        inputsSeparatorLabelConstr.weightx = 0;
        inputsSeparatorLabelConstr.anchor = GridBagConstraints.WEST;
        inputsSeparatorLabelConstr.fill = GridBagConstraints.NONE;
        inputsSeparatorLabelConstr.insets = new Insets(7, 7, 7, 7);
        final GridBagConstraints inputsSeparatorSeparatorConstr = new GridBagConstraints();
        inputsSeparatorSeparatorConstr.gridwidth = GridBagConstraints.REMAINDER;
        inputsSeparatorSeparatorConstr.weightx = 1;
        inputsSeparatorSeparatorConstr.fill = GridBagConstraints.HORIZONTAL;
        inputsSeparator.add(new JLabel("Inputs"), inputsSeparatorLabelConstr);
        inputsSeparator.add(new JSeparator(), inputsSeparatorSeparatorConstr);
        m_root.add(inputsSeparator, m_rootConstr);
        m_rootConstr.gridy++;
        // inputs
        for (final DLLayerDataSpec inputDataSpec : networkSpec.getInputSpecs()) {
            if (!DLUtils.Shapes.isFixed(inputDataSpec.getShape())) {
                throw new NotConfigurableException("Input '" + inputDataSpec.getName()
                    + "' has an (at least partially) unknown shape. This is not supported.");
            }
            createInputPanel(inputDataSpec, tableSpec, m_generalCfg.getBackendModel());
        }

        // output settings:
        final JPanel outputsSeparator = new JPanel(new GridBagLayout());
        final GridBagConstraints outputsSeparatorLabelConstr = new GridBagConstraints();
        outputsSeparatorLabelConstr.gridwidth = 1;
        outputsSeparatorLabelConstr.weightx = 0;
        outputsSeparatorLabelConstr.anchor = GridBagConstraints.WEST;
        outputsSeparatorLabelConstr.fill = GridBagConstraints.NONE;
        outputsSeparatorLabelConstr.insets = new Insets(7, 7, 7, 7);
        final GridBagConstraints outputsSeparatorSeparatorConstr = new GridBagConstraints();
        outputsSeparatorSeparatorConstr.gridwidth = GridBagConstraints.REMAINDER;
        outputsSeparatorSeparatorConstr.weightx = 1;
        outputsSeparatorSeparatorConstr.fill = GridBagConstraints.HORIZONTAL;
        outputsSeparator.add(new JLabel("Outputs"), outputsSeparatorLabelConstr);
        outputsSeparator.add(new JSeparator(), outputsSeparatorSeparatorConstr);
        m_root.add(outputsSeparator, m_rootConstr);
        m_rootConstr.gridy++;
        // 'add output' button
        m_outputsAddBtn = new JButton("add output");
        // 'add output' button click event: open dialog
        m_outputsAddBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                // 'add output' dialog
                final JPanel outputsAddDlg = new JPanel(new GridBagLayout());
                final GridBagConstraints addOutputDialogConstr = new GridBagConstraints();
                addOutputDialogConstr.gridx = 0;
                addOutputDialogConstr.gridy = 0;
                addOutputDialogConstr.weightx = 1;
                addOutputDialogConstr.anchor = GridBagConstraints.WEST;
                addOutputDialogConstr.fill = GridBagConstraints.VERTICAL;
                // available outputs
                final ArrayList<String> availableOutputs = new ArrayList<>(networkSpec.getOutputSpecs().length
                    + networkSpec.getIntermediateOutputSpecs().length - m_outputPanels.size());
                final HashMap<String, DLLayerDataSpec> availableOutputsMap = new HashMap<>(availableOutputs.size());
                for (final DLLayerDataSpec outputSpec : networkSpec.getOutputSpecs()) {
                    final String outputName = outputSpec.getName();
                    if (!m_outputPanels.containsKey(outputName)) {
                        availableOutputs.add(outputName);
                        availableOutputsMap.put(outputName, outputSpec);
                    }
                }
                for (int i = networkSpec.getIntermediateOutputSpecs().length - 1; i >= 0; i--) {
                    final DLLayerDataSpec intermediateSpec = networkSpec.getIntermediateOutputSpecs()[i];
                    final String intermediateName = intermediateSpec.getName();
                    if (!m_outputPanels.containsKey(intermediateName)) {
                        final String intermediateDisplayName = intermediateName + " (hidden)";
                        availableOutputs.add(intermediateDisplayName);
                        availableOutputsMap.put(intermediateDisplayName, intermediateSpec);
                    }
                }
                // output selection
                final SettingsModelString smOutput = new SettingsModelString("output", availableOutputs.get(0));
                final DialogComponentStringSelection dcOutput =
                    new DialogComponentStringSelection(smOutput, "Output", availableOutputs);
                outputsAddDlg.add(dcOutput.getComponentPanel(), addOutputDialogConstr);
                final int selectedOption = JOptionPane.showConfirmDialog(DLExecutorNodeDialog.this.getPanel(),
                    outputsAddDlg, "Add output...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (selectedOption == JOptionPane.OK_OPTION) {
                    final DLLayerDataSpec outputDataSpec = availableOutputsMap.get(smOutput.getStringValue());
                    if (!DLUtils.Shapes.isFixed(outputDataSpec.getShape())) {
                        throw new RuntimeException("Output '" + outputDataSpec.getName()
                            + "' has an (at least partially) unknown shape. This is not supported.");
                    }
                    createOutputPanel(outputDataSpec, m_generalCfg.getBackendModel());
                }
            }
        });
        final GridBagConstraints outputsAddBtnConstr = (GridBagConstraints)m_rootConstr.clone();
        outputsAddBtnConstr.weightx = 1;
        outputsAddBtnConstr.anchor = GridBagConstraints.EAST;
        outputsAddBtnConstr.fill = GridBagConstraints.NONE;
        outputsAddBtnConstr.insets = new Insets(0, 5, 10, 5);
        m_root.add(m_outputsAddBtn, outputsAddBtnConstr);
        m_rootConstr.gridy++;
    }

    private DLInputLayerDataPanel createInputPanel(final DLLayerDataSpec inputDataSpec, final DataTableSpec tableSpec,
        final SettingsModelString smBackend) {
        final DLInputLayerDataModelConfig inputCfg =
            DLExecutorNodeModel.createInputLayerDataModelConfig(inputDataSpec.getName(), smBackend);
        final DLInputLayerDataPanel inputPanel = new DLInputLayerDataPanel(inputDataSpec, inputCfg, tableSpec);
        addInput(inputPanel);
        return inputPanel;
    }

    private DLOutputLayerDataPanel createOutputPanel(final DLLayerDataSpec outputDataSpec,
        final SettingsModelString smBackend) {
        final DLOutputLayerDataModelConfig outputCfg =
            DLExecutorNodeModel.createOutputLayerDataModelConfig(outputDataSpec.getName(), smBackend);
        final DLOutputLayerDataPanel outputPanel = new DLOutputLayerDataPanel(outputDataSpec, outputCfg);
        outputPanel.addRemoveListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent e) {
                removeOutput(outputDataSpec.getName(), outputPanel, outputCfg);
            }
        });
        addOutput(outputDataSpec.getName(), outputPanel);
        return outputPanel;
    }

    private void addInput(final DLInputLayerDataPanel inputPanel) {
        m_inputPanels.add(inputPanel);
        m_root.add(inputPanel, m_rootConstr);
        m_rootConstr.gridy++;
    }

    private void addOutput(final String outputName, final DLOutputLayerDataPanel outputPanel) {
        if (!m_outputPanels.containsKey(outputName)) {
            m_outputPanels.put(outputName, outputPanel);
            m_root.add(outputPanel, m_rootConstr);
            m_rootConstr.gridy++;
            if (m_outputPanels.size() == m_lastIncomingNetworkSpec.getIntermediateOutputSpecs().length
                + m_lastIncomingNetworkSpec.getOutputSpecs().length) {
                m_outputsAddBtn.setEnabled(false);
            }
            m_rootScrollableView.validate();
            final JScrollBar scrollBar = m_rootScrollableView.getVerticalScrollBar();
            scrollBar.setValue(scrollBar.getMaximum());
            m_rootScrollableView.repaint();
        }
    }

    private void removeOutput(final String outputName, final JPanel outputPanel,
        final DLOutputLayerDataModelConfig m_cfg) {
        if (m_outputPanels.remove(outputName) != null) {
            m_root.remove(outputPanel);
            m_outputsAddBtn.setEnabled(true);
            m_rootScrollableView.validate();
            m_rootScrollableView.repaint();
        }
    }

    private List<String> getAvailableBackends(final DLProfile profile) {
        final ArrayList<String> backends = new ArrayList<>(profile.size());
        profile.forEach(b -> backends.add(b.getIdentifier()));
        backends.sort(Comparator.naturalOrder());
        return backends;
    }
}
