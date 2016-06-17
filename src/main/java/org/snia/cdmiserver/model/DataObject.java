/*
 * Copyright (c) 2010, Sun Microsystems, Inc. Copyright (c) 2010, The Storage Networking Industry
 * Association.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of The Storage Networking Industry Association (SNIA) nor the names of its
 * contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.snia.cdmiserver.model;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.util.MediaTypes;

/**
 * <p>
 * Representation of a CDMI <em>DataObject</em>.
 * </p>
 */
public class DataObject extends CdmiObject {

  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(DataObject.class);

  private String objectType;
  private String objectName;
  private String parentUri;
  private String parentId;
  private String domainUri;
  private String capabilitiesUri;
  private String completionStatus;

  private String percentComplete;
  private JSONObject metadata;

  private String mimetype;
  private String deserializevalue;
  private String reference;
  private String move;
  private String copy;
  private String deserialize;
  private String serialize;
  private String valuetransferencoding;
  private String value;

  private DataObject() {}

  /**
   * Creates a new data object with the mandatory fields.
   * 
   * @param objectName the container's name
   * @param parentUri the container's parent URI
   * @param parentId the container's parent objectId
   */
  public DataObject(String objectName, String parentUri, String parentId) {
    super();
    this.objectName = objectName;
    this.parentUri = parentUri;
    this.parentId = parentId;
    // default values
    this.objectType = MediaTypes.DATA_OBJECT;
    this.domainUri = "/cdmi_domains";
    this.capabilitiesUri = "/cdmi_capabilities/dataobject";
    this.completionStatus = "Processing";
    this.mimetype = "application/octet-stream";
    this.metadata = new JSONObject();
  }

  public String getObjectType() {
    return objectType;
  }

  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  public String getObjectName() {
    return objectName;
  }

  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }

  public String getParentUri() {
    return parentUri;
  }

  public void setParentUri(String parentUri) {
    this.parentUri = parentUri;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getDomainUri() {
    return domainUri;
  }

  public void setDomainUri(String domainUri) {
    this.domainUri = domainUri;
  }

  public String getCapabilitiesUri() {
    return capabilitiesUri;
  }

  public void setCapabilitiesUri(String capabilitiesUri) {
    this.capabilitiesUri = capabilitiesUri;
  }

  public String getCompletionStatus() {
    return completionStatus;
  }

  public void setCompletionStatus(String completionStatus) {
    this.completionStatus = completionStatus;
  }

  public String getPercentComplete() {
    return percentComplete;
  }

  public void setPercentComplete(String percentComplete) {
    this.percentComplete = percentComplete;
  }

  public JSONObject getMetadata() {
    return metadata;
  }

  public void setMetadata(JSONObject metadata) {
    this.metadata = metadata;
  }

  public String getDeserializedvalue() {
    return deserializevalue;
  }

  public void setDeserializedvalue(String deserializevalue) {
    this.deserializevalue = deserializevalue;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getMove() {
    return move;
  }

  public void setMove(String move) {
    this.move = move;
  }

  public String getCopy() {
    return copy;
  }

  public void setCopy(String copy) {
    this.copy = copy;
  }

  public String getDeserialize() {
    return deserialize;
  }

  public void setDeserialize(String deserialize) {
    this.deserialize = deserialize;
  }

  public String getMimetype() {
    return mimetype;
  }

  public void setMimetype(String mimetype) {
    this.mimetype = mimetype;
  }

  public String getSerialize() {
    return serialize;
  }

  public void setSerialize(String serialize) {
    this.serialize = serialize;
  }

  public String getValuetransferencoding() {
    return valuetransferencoding;
  }

  public void setValuetransferencoding(String valuetransferencoding) {
    this.valuetransferencoding = valuetransferencoding;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Deserializes a data object from the given JSON object.
   * 
   * @param json a {@link JSONObject}
   */
  public static DataObject fromJson(JSONObject json) {
    DataObject dataObject = new DataObject();

    if (json.has("objectID")) {
      dataObject.setObjectId(json.optString("objectID"));
    }
    if (json.has("objectName")) {
      dataObject.objectName = json.optString("objectName");
    }
    if (json.has("parentURI")) {
      dataObject.parentUri = json.optString("parentURI");
    }
    if (json.has("parentID")) {
      dataObject.parentId = json.optString("parentID");
    }
    // default values
    dataObject.objectType = MediaTypes.DATA_OBJECT;

    dataObject.domainUri = json.optString("domainURI", "/cdmi_domains");
    dataObject.capabilitiesUri = json.optString("capabilitiesURI", "/cdmi_capabilities/container");
    dataObject.completionStatus = json.optString("completionStatus", "Processing");
    dataObject.mimetype = json.optString("mimetype", "application/octet-stream");
    dataObject.metadata = json.optJSONObject("metadata");
    if (dataObject.metadata == null) {
      dataObject.metadata = new JSONObject();
    }
    // optional values
    if (json.has("percentComplete")) {
      dataObject.percentComplete = json.optString("percentComplete");
    }
    if (json.has("serialize")) {
      dataObject.serialize = json.optString("serialize");
    }
    if (json.has("valuetransferencoding")) {
      dataObject.valuetransferencoding = json.optString("valuetransferencoding");
    }
    if (json.has("value")) {
      dataObject.value = json.optString("value");
    }
    if (json.has("deserializevalue")) {
      dataObject.deserializevalue = json.optString("deserializevalue");
    }
    if (json.has("reference")) {
      dataObject.reference = json.optString("reference");
    }
    if (json.has("move")) {
      dataObject.move = json.optString("move");
    }
    if (json.has("copy")) {
      dataObject.copy = json.optString("copy");
    }
    if (json.has("deserialize")) {
      dataObject.deserialize = json.optString("deserialize");
    }

    return dataObject;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();

    json.putOpt("objectType", objectType);
    json.putOpt("objectName", objectName);
    json.putOpt("parentURI", parentUri);
    json.putOpt("parentID", parentId);
    json.putOpt("domainURI", domainUri);
    json.putOpt("capabilitiesURI", capabilitiesUri);
    json.putOpt("completionStatus", completionStatus);
    json.putOpt("percentComplete", percentComplete);
    json.putOpt("metadata", metadata);
    json.putOpt("mimetype", mimetype);
    json.putOpt("serialize", serialize);
    json.putOpt("valuetransferencoding", valuetransferencoding);
    json.putOpt("value", value);
    json.putOpt("deserializevalue", deserializevalue);
    json.putOpt("reference", reference);
    json.putOpt("move", move);
    json.putOpt("copy", copy);
    json.putOpt("deserialize", deserialize);

    return json;
  }

  @Override
  public String toString() {
    return "DataObject [objectId=" + getObjectId() + ", "
        + (objectType != null ? "objectType=" + objectType + ", " : "")
        + (objectName != null ? "objectName=" + objectName + ", " : "")
        + (parentUri != null ? "parentUri=" + parentUri + ", " : "")
        + (parentId != null ? "parentId=" + parentId + ", " : "")
        + (domainUri != null ? "domainUri=" + domainUri + ", " : "")
        + (capabilitiesUri != null ? "capabilitiesUri=" + capabilitiesUri + ", " : "")
        + (completionStatus != null ? "completionStatus=" + completionStatus + ", " : "")
        + (percentComplete != null ? "percentComplete=" + percentComplete + ", " : "")
        + (metadata != null ? "metadata=" + metadata + ", " : "")
        + (mimetype != null ? "mimetype=" + mimetype + ", " : "")
        + (deserializevalue != null ? "deserializevalue=" + deserializevalue + ", " : "")
        + (reference != null ? "reference=" + reference + ", " : "")
        + (move != null ? "move=" + move + ", " : "") + (copy != null ? "copy=" + copy + ", " : "")
        + (deserialize != null ? "deserialize=" + deserialize + ", " : "")
        + (serialize != null ? "serialize=" + serialize + ", " : "")
        + (valuetransferencoding != null ? "valuetransferencoding=" + valuetransferencoding + ", "
            : "")
        + (value != null ? "value=" + value : "") + "]";
  }
}
