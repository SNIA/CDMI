    /*
 * Copyright (c) 2010, Sun Microsystems, Inc.
 * Copyright (c) 2010, The Storage Networking Industry Association.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of The Storage Networking Industry Association (SNIA) nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 *  THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.snia.cdmiserver.dao.filesystem;

import org.snia.cdmiserver.dao.CapabilityDao;
import org.snia.cdmiserver.model.Capability;
import org.snia.cdmiserver.util.ObjectID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Concrete implementation of {@link CapabilityObjectDao} using the local filesystem as the backing
 * store.
 * </p>
 */
public class CapabilityDaoImpl implements CapabilityDao {

    private static final Logger LOG = LoggerFactory.getLogger(CapabilityDaoImpl.class);

    // -------------------------------------------------------------- Properties
    /**
     * <p>
     * Injected {@link CapabilityDao} instance.
     * </p>
     */
    private final String ROOTobjectID = ObjectID.getObjectID(8);
    private final String CONTAINERobjectID = ObjectID.getObjectID(8);
    private final String DEFAULTobjectID = ObjectID.getObjectID(8);
    private final String OBJECTobjectID = ObjectID.getObjectID(8);


    // ---------------------------------------------------- ContainerDao Methods
    @Override
    public Capability findByObjectId(String objectId) {
        throw new UnsupportedOperationException("CapabilityDaoImpl.findByObjectId()");
    }

    @Override
    public Capability findByPath(String path) {
        Capability capability = new Capability();

        LOG.trace("In Capability.findByPath, path is: {}", path);
        switch (path) {
        case "container/":
            LOG.trace("Container Capabilities");
            // Container Capabilities
            capability.getMetadata().put("cdmi_list_children", "true");
            capability.getMetadata().put("cdmi_read_metadata", "true");
            capability.getMetadata().put("cdmi_modify_metadata", "true");
            capability.getMetadata().put("cdmi_create_dataobject", "true");
            capability.getMetadata().put("cdmi_create_container", "true");
            capability.getChildren().add("default");
            capability.setObjectID(CONTAINERobjectID);
            capability.setObjectType("application/cdmi-capability");
            capability.setParentURI("cdmi_capabilities/");
            capability.setParentID(ROOTobjectID);
            break;
        case "container/default/":
            LOG.trace("Default Container Capabilities");
            capability.getMetadata().put("cdmi_list_children", "true");
            capability.getMetadata().put("cdmi_read_metadata", "true");
            capability.getMetadata().put("cdmi_modify_metadata", "true");
            capability.getMetadata().put("cdmi_create_dataobject", "true");
            capability.getMetadata().put("cdmi_post_dataobject", "true");
            capability.getMetadata().put("cdmi_create_container", "true");
            capability.setObjectID(DEFAULTobjectID);
            capability.setObjectType("application/cdmi-capability");
            capability.setParentURI("cdmi_capabilities/container");
            capability.setParentID(CONTAINERobjectID);
            break;
        case "dataobject/":
            // Data Object Capabilities
            LOG.trace("Data Object Capabilities");
            capability.getMetadata().put("cdmi_read_value", "true");
            capability.getMetadata().put("cdmi_read_metadata", "true");
            capability.getMetadata().put("cdmi_modify_metadata", "true");
            capability.getMetadata().put("cdmi_modify_value", "true");
            capability.getMetadata().put("cdmi_delete_dataobject", "true");
            capability.setObjectID(OBJECTobjectID);
            capability.setObjectType("application/cdmi-capability");
            capability.setParentURI("cdmi_capabilities/");
            capability.setParentID(ROOTobjectID);
            break;
        default:
            // System Capabilities
            LOG.trace("System Capabilities");
            capability.getMetadata().put("domains", "false");
            capability.getMetadata().put("cdmi_export_occi_iscsi", "true");
            capability.getMetadata().put("cdmi_metadata_maxitems", "1024");
            capability.getMetadata().put("cdmi_metadata_maxsize", "4096");
            capability.getChildren().add("container");
            capability.getChildren().add("dataobject");
            capability.setObjectID(ROOTobjectID);
            capability.setObjectType("application/cdmi-capability");
            capability.setParentURI("/");
            capability.setParentID(ROOTobjectID);
            break;
        }
        return (capability);
    }
}
