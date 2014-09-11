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

/**
 * <p>
 * Concrete implementation of {@link CapabilityObjectDao} using the local filesystem as the backing
 * store.
 * </p>
 */
public class CapabilityDaoImpl implements CapabilityDao {

    // -------------------------------------------------------------- Properties
    /**
     * <p>
     * Injected {@link CapabilityDao} instance.
     * </p>
     */
    private CapabilityDao capabilityDao;
    private String ROOTobjectID = ObjectID.getObjectID(8);
    private String CONTAINERobjectID = ObjectID.getObjectID(8);
    private String DEFAULTobjectID = ObjectID.getObjectID(8);
    private String OBJECTobjectID = ObjectID.getObjectID(8);

    public void setCapabilityDao(CapabilityDao capabilityDao) {
        this.capabilityDao = capabilityDao;
    }

    // ---------------------------------------------------- ContainerDao Methods
    @Override
    public Capability findByObjectId(String objectId) {
        throw new UnsupportedOperationException("CapabilityDaoImpl.findByObjectId()");
    }

    @Override
    public Capability findByPath(String path) {
        Capability capability = new Capability();

        System.out.print("In Capability.findByPath, path is: ");
        System.out.println(path);
        if (path.equals("container/")) {
            System.out.println("Container Capabilities");
            // Container Capabilities
            // cdmi_list_children = true
            // cdmi_list_children_range = unset until implemented
            // cdmi_read_metadata = true
            // cdmi_modify_metadata = true
            // cdmi_snapshot = unset, stretch goal (filesystem support?)
            // cdmi_serialize_container = unset until implemented
            // cdmi_create_dataobject = true
            // cdmi_post_dataobject = true
            // cdmi_create_container = true
            capability.getMetadata().put("cdmi_list_children", "true");
            capability.getMetadata().put("cdmi_read_metadata", "true");
            capability.getMetadata().put("cdmi_modify_metadata", "true");
            capability.getMetadata().put("cdmi_create_dataobject", "true");
            // capability.getMetadata().put("cdmi_post_dataobject", "true");
            capability.getMetadata().put("cdmi_create_container", "true");
            capability.getChildren().add("default");
            capability.setObjectID(CONTAINERobjectID);
            capability.setObjectType("application/cdmi-capability");
            capability.setParentURI("cdmi_capabilities/");
            capability.setParentID(ROOTobjectID);
        } else if (path.equals("container/default/")) {
            System.out.println("Default Container Capabilities");
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

        } else if (path.equals("dataobject/")) {
            // Data Object Capabilities
            System.out.println("Data Object Capabilities");
            // cdmi_read_value = true
            // cdmi_read_value_range = unset initially, then true when implemented
            // cdmi_read_metadata = true
            // cdmi_modify_value = true
            // cdmi_modify_value_range = unset until implemented
            // cdmi_modify_metadata = true
            // cdmi_serialize_dataobject, cdmi_deserialize_dataobject = unset until implemented
            // cdmi_delete_dataobject = true
            capability.getMetadata().put("cdmi_read_value", "true");
            capability.getMetadata().put("cdmi_read_metadata", "true");
            capability.getMetadata().put("cdmi_modify_metadata", "true");
            capability.getMetadata().put("cdmi_modify_value", "true");
            capability.getMetadata().put("cdmi_delete_dataobject", "true");
            capability.setObjectID(OBJECTobjectID);
            capability.setObjectType("application/cdmi-capability");
            capability.setParentURI("cdmi_capabilities/");
            capability.setParentID(ROOTobjectID);
        } else {
            // System Capabilities
            System.out.println("System Capabilities");
            // cdmi_domains = later version true
            // cdmi_export_occi_iscsi = true for demo?
            // cdmi_metadata_maxitems, cdmi_metadata_maxsize = TBD based on our limits
            // cdmi_notification, cdmi_query, cdmi_queues, cdmi_security_audit = exposed as
            // implementations become available
            // cdmi_security_data_integrity, cdmi_security_encryption, ditto
            // cdmi_security_https_transport = present and true
            // cdmi_security_immutability = as XAM SDK code is integrated
            // cdmi_security_sanitization = should we implement?
            // cdmi_serialization_json = propose using this form for RI
            capability.getMetadata().put("domains", "false");
            capability.getMetadata().put("cdmi_export_occi_iscsi", "true");
            capability.getMetadata().put("cdmi_metadata_maxitems", "1024");
            capability.getMetadata().put("cdmi_metadata_maxsize", "4096");
            // capability.getMetadata().put("cdmi_security_https_transport", "true");
            // capability.getMetadata().put("cdmi_serialization_json", "true");
            capability.getChildren().add("container");
            capability.getChildren().add("dataobject");
            capability.setObjectID(ROOTobjectID);
            capability.setObjectType("application/cdmi-capability");
            capability.setParentURI("/");
            capability.setParentID(ROOTobjectID);
        }
        return (capability);
        // throw new UnsupportedOperationException("CapabilityDaoImpl.findByPath()");
    }
}
