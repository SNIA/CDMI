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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CapabilityDao;
import org.snia.cdmiserver.model.Capability;
import org.snia.cdmiserver.util.MediaTypes;
import org.snia.cdmiserver.util.ObjectID;
import org.springframework.stereotype.Component;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

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
	private String DEFAULTCONTAINERobjectID = ObjectID.getObjectID(8);
	private String DISKCONTAINERobjectID = ObjectID.getObjectID(8);
	private String TAPECONTAINERobjectID = ObjectID.getObjectID(8);
	private String DEFAULTOBJECTobjectID = ObjectID.getObjectID(8);
	private String DISKOBJECTobjectID = ObjectID.getObjectID(8);
	private String TAPEOBJECTobjectID = ObjectID.getObjectID(8);
	private String OBJECTobjectID = ObjectID.getObjectID(8);
	
	private final ImmutableMap<String, String> OBJECTIDs_BY_NAME = ImmutableMap.of(
		    "default", DEFAULTOBJECTobjectID,
		    "tape", TAPEOBJECTobjectID,
		    "disk", DISKOBJECTobjectID
		);
	private final ImmutableMap<String, String> CONTAINERIDs_BY_NAME = ImmutableMap.of(
		    "default", DEFAULTCONTAINERobjectID,
		    "tape", TAPECONTAINERobjectID,
		    "disk", DISKCONTAINERobjectID
		);

	// ---------------------------------------------------- ContainerDao Methods

	@Override
	public Capability findByObjectId(String objectId) {
		throw new UnsupportedOperationException("CapabilityDaoImpl.findByObjectId()");
	}

	private JSONObject json;
	private JSONObject system;
	private JSONObject dataobject;
	private JSONObject container;

	private void readProperties() {
		Path path = Paths.get("src/main/resources/capabilities.properties.json");
		String file;
		try {
			file = new String(Files.readAllBytes(path));
			json = new JSONObject(file);
			system = json.getJSONObject("system-capabilities");
			dataobject = json.getJSONObject("data-object-capabilities");
			container = json.getJSONObject("container-capabilities");
		} catch (Exception e) {
			LOG.error("ERROR: {}", e.getMessage());
			//e.printStackTrace();
		}
	}

	@Override
	public Capability findByPath(String path) {
		Capability capability = new Capability();
		readProperties();

		LOG.trace("In Capability.findByPath, path is: {}", path);
		//container capabilities
		Iterator<?> containerkeys = container.keys();
		while (containerkeys.hasNext()) {
			String key = (String) containerkeys.next();
			if (container.get(key) instanceof JSONObject) {
				if (path.equals("/cdmi_capabilities/container/" + key)) {
					LOG.trace("Container "+key+" Capabilities");
					JSONObject defaultC = container.getJSONObject(key);
					Iterator<?> keys = defaultC.keys();
					while (keys.hasNext()) {
						String cap = (String) keys.next();
						capability.getCapabilities().put(cap, String.valueOf(defaultC.get(cap)));
					}
					capability.setParentURI("cdmi_capabilities/container");
					capability.setParentID(CONTAINERobjectID);
					capability.setObjectName("cdmi_container_"+key+"_capabilities");
					capability.setObjectID(CONTAINERIDs_BY_NAME.get(key));
				}
			}
		}
		//dataobject capabilities
		Iterator<?> objectkeys = dataobject.keys();
		while (objectkeys.hasNext()) {
			String key = (String) objectkeys.next();
			if (dataobject.get(key) instanceof JSONObject) {
				if (path.equals("/cdmi_capabilities/dataobject/" + key)) {
					LOG.trace("Dataobject "+key+" Capabilities");
					JSONObject object = dataobject.getJSONObject(key);
					Iterator<?> keys = object.keys();
					while (keys.hasNext()) {
						String cap = (String) keys.next();
						capability.getCapabilities().put(cap, String.valueOf(object.get(cap)));
					}
					capability.setParentURI("cdmi_capabilities/dataobject");
					capability.setParentID(OBJECTobjectID);
					capability.setObjectName("cdmi_object_"+key+"_capabilities");
					capability.setObjectID(OBJECTIDs_BY_NAME.get(key));
				}
			}
		}
		if (path.equals("/cdmi_capabilities/")) {
			// System Capabilities
			LOG.trace("System Capabilities");

			Iterator<?> keys = system.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				capability.getCapabilities().put(key, String.valueOf(system.get(key)));
			}

			capability.getChildren().add("container");
			capability.getChildren().add("dataobject");
			capability.setChildrenrange("0-" + String.valueOf(capability.getChildren().size() - 1));
			capability.setObjectID(ROOTobjectID);
			capability.setObjectName("cdmi_capabilities");
			capability.setParentURI("/");
			capability.setParentID(ROOTobjectID);
		}

		capability.setObjectType(MediaTypes.CAPABILITY);
		return (capability);

	}

}
