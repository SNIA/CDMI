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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CapabilityDao;
import org.snia.cdmiserver.model.Capability;
import org.snia.cdmiserver.util.MediaTypes;
import org.snia.cdmiserver.util.ObjectID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Concrete implementation of {@link CapabilityObjectDao} using the local
 * filesystem as the backing store.
 * </p>
 */
@Component
public class CapabilityDaoImpl implements CapabilityDao {

	private static final Logger LOG = LoggerFactory.getLogger(CapabilityDaoImpl.class);

	// -------------------------------------------------------------- Properties

	private String ROOTobjectID = ObjectID.getObjectID(8);
	private String CONTAINERobjectID = ObjectID.getObjectID(8);
	private String DEFAULTobjectID = ObjectID.getObjectID(8);
	private String OBJECTobjectID = ObjectID.getObjectID(8);

	@Value("${capability.system.cdmi_domains}") private String system_cdmi_domains;
	@Value("${capability.system.cdmi_export_occi_iscsi}") private String system_cdmi_export_occi_iscsi;
	@Value("${capability.system.cdmi_metadata_maxitems}") private String system_cdmi_metadata_maxitems;
	@Value("${capability.system.cdmi_metadata_maxsize}") private String system_cdmi_metadata_maxsize;
	@Value("${capability.container.cdmi_list_children}") private String container_cdmi_list_children;
	@Value("${capability.container.cdmi_read_metadata}") private String container_cdmi_read_metadata;
	@Value("${capability.container.cdmi_modify_metadata}") private String container_cdmi_modify_metadata;
	@Value("${capability.container.cdmi_create_dataobject}") private String container_cdmi_create_dataobject;
	@Value("${capability.container.cdmi_post_dataobject}") private String container_cdmi_post_dataobject;
	@Value("${capability.container.cdmi_create_container}") private String container_cdmi_create_container;
	@Value("${capability.dataobject.cdmi_read_value}") private String dataobject_cdmi_read_value;
	@Value("${capability.dataobject.cdmi_read_value_range}") private String dataobject_cdmi_read_value_range;
	@Value("${capability.dataobject.cdmi_modify_value}") private String dataobject_cdmi_modify_value;
	// @Value("${capability.dataobject.cdmi_modify_value_range}") private String dataobject_cdmi_modify_value_range;
	@Value("${capability.dataobject.cdmi_read_metadata}") private String dataobject_cdmi_read_metadata;
	@Value("${capability.dataobject.cdmi_modify_metadata}") private String dataobject_cdmi_modify_metadata;
	@Value("${capability.dataobject.cdmi_delete_dataobject}") private String dataobject_cdmi_delete_dataobject;
	
	// ---------------------------------------------------- ContainerDao Methods

	@Override
	public Capability findByObjectId(String objectId) {
		throw new UnsupportedOperationException("CapabilityDaoImpl.findByObjectId()");
	}

	@Override
	public Capability findByPath(String path) {
		Capability capability = new Capability();

		LOG.trace("In Capability.findByPath, path is: {}", path);
		if (path.equals("/cdmi_capabilities/container/")) {	//XXX changed the Path
			LOG.trace("Container Capabilities");
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
			capability.getCapabilities().put("cdmi_list_children", container_cdmi_list_children);
			capability.getCapabilities().put("cdmi_read_metadata", container_cdmi_read_metadata);
			capability.getCapabilities().put("cdmi_modify_metadata", container_cdmi_modify_metadata);
			capability.getCapabilities().put("cdmi_create_dataobject", container_cdmi_create_dataobject);
			// capability.getMetadata().put("cdmi_post_dataobject", container_cdmi_post_dataobject);
			capability.getCapabilities().put("cdmi_create_container", container_cdmi_create_container);
			capability.getChildren().add("default");
			capability.setChildrenrange("0-" + String.valueOf(capability.getChildren().size() - 1));	//XXX added childrenrange
			capability.setObjectID(CONTAINERobjectID);
			capability.setObjectType(MediaTypes.CAPABILITY);
			capability.setParentURI("cdmi_capabilities/");
			capability.setParentID(ROOTobjectID);
		} else if (path.equals("/cdmi_capabilities/container/default/")) {	//XXX changed the Path
			LOG.trace("Default Container Capabilities");
			capability.getCapabilities().put("cdmi_list_children", container_cdmi_list_children);
			capability.getCapabilities().put("cdmi_read_metadata", container_cdmi_read_metadata);
			capability.getCapabilities().put("cdmi_modify_metadata", container_cdmi_modify_metadata);
			capability.getCapabilities().put("cdmi_create_dataobject", container_cdmi_create_dataobject);
			capability.getCapabilities().put("cdmi_post_dataobject", container_cdmi_post_dataobject);
			capability.getCapabilities().put("cdmi_create_container", container_cdmi_create_container);
			capability.setObjectID(DEFAULTobjectID);
			capability.setObjectType(MediaTypes.CAPABILITY);
			capability.setParentURI("cdmi_capabilities/container");
			capability.setParentID(CONTAINERobjectID);

		} else if (path.equals("/cdmi_capabilities/dataobject/")) {//XXX changed the Path
			// Data Object Capabilities
			LOG.trace("Data Object Capabilities");
			// cdmi_read_value = true
			// cdmi_read_value_range = unset initially, then true when
			// implemented
			// cdmi_read_metadata = true
			// cdmi_modify_value = true
			// cdmi_modify_value_range = unset until implemented
			// cdmi_modify_metadata = true
			// cdmi_serialize_dataobject, cdmi_deserialize_dataobject = unset
			// until implemented
			// cdmi_delete_dataobject = true
			capability.getCapabilities().put("cdmi_read_value", dataobject_cdmi_read_value);
			capability.getCapabilities().put("cdmi_read_metadata", dataobject_cdmi_read_metadata);
			capability.getCapabilities().put("cdmi_modify_metadata", dataobject_cdmi_modify_metadata);
			capability.getCapabilities().put("cdmi_modify_value", dataobject_cdmi_modify_value);
			capability.getCapabilities().put("cdmi_delete_dataobject", dataobject_cdmi_delete_dataobject);
			// capability.getCapabilities().put("cdmi_modify_value_range", dataobject_cdmi_modify_value_range);
			capability.getCapabilities().put("cdmi_read_value_range", dataobject_cdmi_read_value_range);
			capability.setObjectID(OBJECTobjectID);
			capability.setObjectType(MediaTypes.CAPABILITY);
			capability.setParentURI("cdmi_capabilities/");
			capability.setParentID(ROOTobjectID);
		} else {
			// System Capabilities
			LOG.trace("System Capabilities");
			// cdmi_domains = later version true
			// cdmi_export_occi_iscsi = true for demo?
			// cdmi_metadata_maxitems, cdmi_metadata_maxsize = TBD based on our
			// limits
			// cdmi_notification, cdmi_query, cdmi_queues, cdmi_security_audit =
			// exposed as
			// implementations become available
			// cdmi_security_data_integrity, cdmi_security_encryption, ditto
			// cdmi_security_https_transport = present and true
			// cdmi_security_immutability = as XAM SDK code is integrated
			// cdmi_security_sanitization = should we implement?
			// cdmi_serialization_json = propose using this form for RI
			capability.getCapabilities().put("cdmi_domains", system_cdmi_domains);
			capability.getCapabilities().put("cdmi_export_occi_iscsi", system_cdmi_export_occi_iscsi);
			capability.getCapabilities().put("cdmi_metadata_maxitems", system_cdmi_metadata_maxitems);
			capability.getCapabilities().put("cdmi_metadata_maxsize", system_cdmi_metadata_maxsize);
			// capability.getMetadata().put("cdmi_security_https_transport",
			// "true");
			// capability.getMetadata().put("cdmi_serialization_json", "true");

			capability.getChildren().add("container");
			capability.getChildren().add("dataobject");
			capability.setChildrenrange("0-" + String.valueOf(capability.getChildren().size() - 1));

			capability.setObjectID(ROOTobjectID);
			capability.setObjectType(MediaTypes.CAPABILITY);
			capability.setObjectName("cdmi_capabilities");
			capability.setParentURI("/");
			capability.setParentID(ROOTobjectID);
		}
		return (capability);
		// throw new
		// UnsupportedOperationException("CapabilityDaoImpl.findByPath()");
	}
}
