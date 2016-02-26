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

import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Representation of a CDMI <em>DataObject</em>.
 * </p>
 */
public class DataObject extends CdmiObject {
	private static final Logger LOG = LoggerFactory.getLogger(DataObject.class);

	// DataObject creation fields
	private static final String mimetype = "mimetype";
	private static final String metadata = "metadata";
	private static final String deserialize = "deserialize";
	private static final String serialize = "serialize";
	private static final String copy = "copy";
	private static final String move = "move";
	private static final String reference = "reference";
	private static final String deserializevalue = "deserializevalue";
	private static final String valuetransferencoding = "valuetransferencoding";
	private static final String value = "value";

	// DataObject representation fields
	private static final String objectType = "objectType";
	private static final String parentURI = "parentURI";
	private static final String parentID = "parentID";
	private static final String domainURI = "domainURI";
	private static final String capabilitiesURI = "capabilitiesURI";
	private static final String completionStatus = "completionStatus";
	private static final String percentComplete = "percentComplete";

	public DataObject() {
		super();
	}

	public DataObject(JSONObject json) {
		super(json);
	}

	public String getDeserializevalue() {
		return (String) getAttributeMap().get(deserializevalue);
	}

	public void setDeserializevalue(String deserializevalue) {
		getAttributeMap().put(DataObject.deserializevalue, deserializevalue);
	}

	public String getValuetransferencoding() {
		return (String) getAttributeMap().get(valuetransferencoding);
	}

	public void setValuetransferencoding(String valuetransferencoding) {
		getAttributeMap().put(DataObject.valuetransferencoding, valuetransferencoding);
	}

	public String getParentID() {
		return (String) getAttributeMap().get(parentID);
	}

	public void setParentID(String parentID) {
		getAttributeMap().put(DataObject.parentID, parentID);
	}

	public String getDomainURI() {
		return (String) getAttributeMap().get(domainURI);
	}

	public void setDomainURI(String domainURI) {
		getAttributeMap().put(DataObject.domainURI, domainURI);
	}

	public String getMimetype() {
		return (String) getAttributeMap().get(mimetype);
	}

	public void setMimetype(String mimetype) {
		getAttributeMap().put(DataObject.mimetype, mimetype);
	}

	public Map<String, String> getMetadata() {
		return (Map<String, String>) getAttributeMap().get(metadata);
	}

	public void setMetadata(Map<String, String> metadata) {
		getAttributeMap().put(DataObject.metadata, metadata);
	}

	public String getDeserialize() {
		return (String) getAttributeMap().get(deserialize);
	}

	public void setDeserialize(String deserialize) {
		getAttributeMap().put(DataObject.deserialize, deserialize);
	}

	public String getSerialize() {
		return (String) getAttributeMap().get(serialize);
	}

	public void setSerialize(String serialize) {
		getAttributeMap().put(DataObject.serialize, serialize);
	}

	public String getCopy() {
		return (String) getAttributeMap().get(copy);
	}

	public void setCopy(String copy) {
		getAttributeMap().put(DataObject.copy, copy);
	}

	public String getMove() {
		return (String) getAttributeMap().get(move);
	}

	public void setMove(String move) {
		getAttributeMap().put(DataObject.move, move);
	}

	public String getReference() {
		return (String) getAttributeMap().get(reference);
	}

	public void setReference(String reference) {
		getAttributeMap().put(DataObject.reference, reference);
	}

	public String getValue() {
		return (String) getAttributeMap().get(value);
	}

	public void setValue(String value) {
		getAttributeMap().put(DataObject.value, value);
	}

	public String getObjectType() {
		return (String) getAttributeMap().get(objectType);
	}

	public void setObjectType(String objectType) {
		getAttributeMap().put(DataObject.objectType, objectType);
	}

	public String getObjectID() {
		return super.getObjectId();
	}

	public void setObjectID(String objectID) {
		super.setObjectId(objectID);
	}

	public String getParentURI() {
		return (String) getAttributeMap().get(parentURI);
	}

	public void setParentURI(String parentURI) {
		getAttributeMap().put(DataObject.parentURI, parentURI);
	}

	public String getCapabilitiesURI() {
		return (String) getAttributeMap().get(capabilitiesURI);
	}

	public void setCapabilitiesURI(String capabilitiesURI) {
		getAttributeMap().put(DataObject.capabilitiesURI, capabilitiesURI);
	}

	public String getCompletionStatus() {
		return (String) getAttributeMap().get(completionStatus);
	}

	public void setCompletionStatus(String completionStatus) {
		getAttributeMap().put(DataObject.completionStatus, completionStatus);
	}

	public String getPercentComplete() {
		return (String) getAttributeMap().get(percentComplete);
	}

	public void setPercentComplete(String percentComplete) {
		getAttributeMap().put(DataObject.percentComplete, percentComplete);
	}

	@Deprecated
	public void fromJson(byte[] bytes, boolean bool) {

	}

	@Deprecated
	public String toJson(boolean bool) {
		return null;
	}
}
