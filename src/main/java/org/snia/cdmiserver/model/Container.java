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

package org.snia.cdmiserver.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Representation of a CDMI <em>Container</em>.
 * </p>
 */
public class Container extends CdmiObject {
	private static final Logger LOG = LoggerFactory.getLogger(Container.class);

	// Container creation fields
	private static final String metadata = "metadata";
	private static final String exports = "exports";
	private static final String copy = "copy";
	private static final String move = "move";
	private static final String reference = "reference";
	private static final String snapshot = "snapshot";
	private static final String deserialize = "deserialize";
	private static final String deserializevalue = "deserializevalue";

	// Container representation fields
	private static final String objectType = "objectType";
	private static final String objectName = "objectName";
	private static final String parentURI = "parentURI";
	private static final String parentID = "parentID";
	private static final String domainURI = "domainURI";
	private static final String capabilitiesURI = "capabilitiesURI";
	private static final String completionStatus = "completionStatus";
	private static final String percentComplete = "percentComplete";
	private static final String snapshots = "snapshots";
	private static final String childrenrange = "childrenrange";
	private static final String children = "children";

	private String ObjectURI;

	public Container() {
		super();
	}

	public Container(JSONObject json) {
		super(json);
	}

	public Container(Map<String, Object> attributeMap) {
		super(attributeMap);
	}

	public Map<String, Object> getMetadata() {
		if (getAttributeMap().get(metadata) != null) {
			JSONObject json = (JSONObject) getAttributeMap().get(metadata);
			HashMap<String, Object> map = new HashMap<>();
			for (Object key : json.keySet()) {
				map.put((String) key, json.get((String) key));
			}
			return map;
		}
		return null;
	}

	public void setMetadata(Map<String, Object> metadata) {
		getAttributeMap().put(Container.metadata, metadata);
	}

	public Map<String, Object> getExports() {
		if (getAttributeMap().get(exports) != null) {
			JSONObject json = (JSONObject) getAttributeMap().get(exports);
			HashMap<String, Object> map = new HashMap<>();
			for (Object key : json.keySet()) {
				map.put((String) key, json.get((String) key));
			}
			return map;
		}
		return null;
	}

	public void setExports(Map<String, Object> exports) {
		getAttributeMap().put(Container.exports, exports);
	}

	public String getCopy() {
		return (String) getAttributeMap().get(copy);
	}

	public void setCopy(String copy) {
		getAttributeMap().put(Container.copy, copy);
	}

	public String getMove() {
		return (String) getAttributeMap().get(move);
	}

	public void setMove(String move) {
		getAttributeMap().put(Container.move, move);
	}

	public String getReference() {
		return (String) getAttributeMap().get(reference);
	}

	public void setReference(String reference) {
		getAttributeMap().put(Container.reference, reference);
	}

	public String getSnapshot() {
		return (String) getAttributeMap().get(snapshot);
	}

	public void setSnapshot(String snapshot) {
		getAttributeMap().put(Container.snapshot, snapshot);
	}

	public String getDeserialize() {
		return (String) getAttributeMap().get(deserialize);
	}

	public void setDeserialize(String deserialize) {
		getAttributeMap().put(Container.deserialize, deserialize);
	}

	public String getDeserializevalue() {
		return (String) getAttributeMap().get(deserializevalue);
	}

	public void setDeserializevalue(String deserializevalue) {
		getAttributeMap().put(Container.deserializevalue, deserializevalue);
	}

	public String getObjectType() {
		return (String) getAttributeMap().get(objectType);
	}

	public void setObjectType(String objectType) {
		getAttributeMap().put(Container.objectType, objectType);
	}

	public String getObjectID() {
		return super.getObjectId();
	}

	public void setObjectID(String objectID) {
		super.setObjectId(objectID);
	}

	public String getObjectName() {
		return (String) getAttributeMap().get(objectName);
	}

	public void setObjectName(String objectName) {
		getAttributeMap().put(Container.objectName, objectName);
	}

	public String getParentURI() {
		return (String) getAttributeMap().get(parentURI);
	}

	public void setParentURI(String parentURI) {
		getAttributeMap().put(Container.parentURI, parentURI);
	}

	public String getParentID() {
		return (String) getAttributeMap().get(Container.parentID);
	}

	public void setParentID(String parentID) {
		getAttributeMap().put(Container.parentID, parentID);
	}

	public String getDomainURI() {
		return (String) getAttributeMap().get(domainURI);
	}

	public void setDomainURI(String domainURI) {
		getAttributeMap().put(Container.domainURI, domainURI);
	}

	public String getCapabilitiesURI() {
		return (String) getAttributeMap().get(capabilitiesURI);
	}

	public void setCapabilitiesURI(String capabilitiesURI) {
		getAttributeMap().put(Container.capabilitiesURI, capabilitiesURI);
	}

	public String getCompletionStatus() {
		return (String) getAttributeMap().get(completionStatus);
	}

	public void setCompletionStatus(String completionStatus) {
		getAttributeMap().put(Container.completionStatus, completionStatus);
	}

	public String getPercentComplete() {
		return (String) getAttributeMap().get(percentComplete);
	}

	public void setPercentComplete(String percentComplete) {
		getAttributeMap().put(Container.percentComplete, percentComplete);
	}

	public List<Object> getSnapshots() {
		if (getAttributeMap().get(snapshots) != null) {
			JSONObject json = (JSONObject) getAttributeMap().get(snapshots);
			ArrayList<Object> list = new ArrayList<>();
			for (Object key : json.keySet()) {
				list.add(json.get((String) key));
			}
			return list;
		}
		return null;
	}

	public void setSnapshots(List<Object> snapshots) {
		getAttributeMap().put(Container.snapshots, snapshots);
	}

	public String getChildrenrange() {
		return (String) getAttributeMap().get(childrenrange);
	}

	public void setChildrenrange(String childrenrange) {
		getAttributeMap().put(Container.childrenrange, childrenrange);
	}

	public List<Object> getChildren() {
		if (getAttributeMap().get(children) != null) {
			JSONObject json = (JSONObject) getAttributeMap().get(children);
			ArrayList<Object> list = new ArrayList<>();
			for (Object key : json.keySet()) {
				list.add(json.get((String) key));
			}
			return list;
		}
		return null;
	}

	public void setChildren(List<Object> children) {
		getAttributeMap().put(Container.children, children);
	}

	public String getObjectURI() {
		return ObjectURI;
	}

	public void setObjectURI(String objectURI) {
		ObjectURI = objectURI;
	}

	@Deprecated
	public void fromJson(byte[] bytes, boolean bool) {

	}

	@Deprecated
	public String toJson(boolean bool) {
		return null;
	}
}
