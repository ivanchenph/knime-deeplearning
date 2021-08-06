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
package org.knime.dl.base.portobjects;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStorePortObject;
import org.knime.core.node.NodeLogger;
import org.knime.dl.core.DLInvalidSourceException;
import org.knime.dl.core.DLNetwork;

/**
 * Abstract base class for deep learning {@link DLNetworkPortObject network port objects} that contain a
 * <em>materialized</em> network. This is the case with all port objects of the KNIME Deep Learning, KNIME TensorFlow
 * (1|2), and KNIME ONNX integrations except for the ones created by the layer nodes of the KNIME Deep Learning - Keras
 * integration ({@code DLKerasUnmaterializedNetworkPortObject}), which only materialize their network on demand.
 *
 * @param <N> the network type
 * @param <S> the port object spec type
 * @author Marcel Wiedenmann, KNIME GmbH, Konstanz, Germany
 * @author Christian Dietz, KNIME GmbH, Konstanz, Germany
 */
public abstract class DLAbstractNetworkPortObject<N extends DLNetwork, S extends DLNetworkPortObjectSpec>
    extends FileStorePortObject implements DLNetworkPortObject {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(DLNetworkPortObject.class);

    /**
     * The stored network. Is <code>null</code> if the port object was created via
     * {@link #DLAbstractNetworkPortObject()} or {@link #DLAbstractNetworkPortObject(List)} and
     * {@link #getNetworkInternal(DLNetworkPortObjectSpec)} was not yet called (except a deriving class did set it
     * manually).
     */
    protected N m_network;

    /**
     * The spec of the port object. Must be populated by the port object's serializer.
     */
    protected S m_spec;

    /**
     * Creates a new instance of this port object. The given network is stored in (i.e. copied to if not already there)
     * the given file store if the file store is non-<code>null</code>.
     *
     * @param network the network to store
     * @param spec the spec of this port object
     * @param fileStore the file store in which to store the network, may be <code>null</code> in which case the network
     *            will not be copied to a file store
     * @throws IOException if failed to store the network
     */
    protected DLAbstractNetworkPortObject(final N network, final S spec, final FileStore fileStore) throws IOException {
        super(fileStore != null ? Collections.singletonList(fileStore) : Collections.emptyList());
        m_network = checkNotNull(network);
        m_spec = checkNotNull(spec);
        if (fileStore != null) {
            // Copy network to file store.
            flushToFileStoreInternal(network, getFileStore(0));
        }
    }

    /**
     * Creates a new instance of this port object that references a number of file stores (optional). Usually, a single
     * file store is referenced and used to store a network. In this case, deriving classes must manually flush the
     * network to file store via {@link #flushToFileStoreInternal(DLNetwork, FileStore)}. Also see
     * {@link #DLAbstractNetworkPortObject()} for further conditions that also apply when using this constructor.
     *
     *
     * @param fileStores the file stores which are references by this port object, may be empty
     */
    protected DLAbstractNetworkPortObject(final List<FileStore> fileStores) {
        super(fileStores);
    }

    /**
     * Deserialization constructor.<br>
     * Deriving classes should make sure to populate {@link #m_spec} immediately after the call to this constructor
     * returns. {@link #m_network} can be populated later, e.g. via
     * {@link #getNetworkInternal(DLNetworkPortObjectSpec)}.
     */
    protected DLAbstractNetworkPortObject() {
    }

    /**
     * Stores the given network in file store. Note that the network's source and the file store may reference the same
     * file.<br>
     * Is called from {@link #DLAbstractNetworkPortObject(DLNetwork, DLNetworkPortObjectSpec, FileStore)}.
     *
     * @param network the network to store in file store
     * @param fileStore the file store in which to store the network
     * @throws IOException if storing the network to file store failed
     */
    protected abstract void flushToFileStoreInternal(N network, FileStore fileStore) throws IOException;

    protected abstract void hashCodeInternal(HashCodeBuilder b);

    protected abstract boolean equalsInternal(DLNetworkPortObject other);

    /**
     * Loads and returns the stored network. Use {@link FileStorePortObject#getFileStore(int)} with argument
     * <code>0</code> as source for loading the network if it was stored in a file store.<br>
     * Is called from {@link #getNetwork()} if {@link #m_network} is null.
     *
     * @param spec the port object spec
     * @return the loaded network
     * @throws DLInvalidSourceException if network source has become unavailable or invalid
     * @throws IOException if loading the network implied I/O which failed (optional)
     */
    protected abstract N getNetworkInternal(S spec) throws DLInvalidSourceException, IOException;

    /**
     * Returns the contained {@link DLNetwork}.
     * <P>
     * Note that this method override "un-deprecates" {@link DLNetworkPortObject#getNetwork()} as the contained network
     * is guaranteed to be materialized.
     *
     * @return the network
     * @throws DLInvalidSourceException if network source has become unavailable or invalid
     * @throws IOException if retrieving the network implied I/O which failed (optional)
     */
    @SuppressWarnings("deprecation") // HACK
    @Override
    public final N getNetwork() throws DLInvalidSourceException, IOException {
        if (m_network == null) {
            try {
                m_network = getNetworkInternal(m_spec);
            } catch (final DLInvalidSourceException e) {
                LOGGER.debug(e.getMessage(), e);
                throw e;
            } catch (final IOException e) {
                LOGGER.debug("Failed to load deep learning network in port object.", e);
                throw e;
            }
        }
        return m_network;
    }

    @Override
    public S getSpec() {
        return m_spec;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder b = new HashCodeBuilder(17, 37);
        b.append(m_network);
        b.append(m_spec);
        hashCodeInternal(b);
        return b.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        final DLAbstractNetworkPortObject<?, ?> other = (DLAbstractNetworkPortObject<?, ?>)obj;
        return Objects.equals(other.m_network, m_network) //
            && Objects.equals(other.m_spec, m_spec) //
            && equalsInternal(other);
    }
}
