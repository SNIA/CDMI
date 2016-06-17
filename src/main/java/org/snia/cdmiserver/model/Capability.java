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

import org.json.JSONArray;
import org.json.JSONObject;
import org.snia.cdmiserver.util.MediaTypes;

/**
 * <p>
 * Representation of a CDMI <em>Capability</em>.
 * </p>
 */
public class Capability extends CdmiObject {

  private String objectType;
  private String objectName;
  private String parentUri;
  private String parentId;

  private JSONObject metadata;
  private JSONObject capabilities;

  private String childrenrange;
  private JSONArray children;

  /**
   * Creates a new capability object with the mandatory fields.
   * 
   * @param objectName the object's name
   * @param parentUri the object's parent URI
   * @param parentId the object's parent objectId
   */
  public Capability(String objectName, String parentUri, String parentId) {
    super();
    this.objectName = objectName;
    this.parentId = parentId;
    this.parentUri = parentUri;
    // default values
    this.objectType = MediaTypes.CAPABILITY;
    this.capabilities = new JSONObject();
    this.metadata = new JSONObject();
  }

  private Capability() {}

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

  public JSONObject getMetadata() {
    return metadata;
  }

  public void setMetadata(JSONObject metadata) {
    this.metadata = metadata;
  }

  public JSONObject getCapabilities() {
    return capabilities;
  }

  public void setCapabilities(JSONObject capabilities) {
    this.capabilities = capabilities;
  }

  public String getChildrenrange() {
    return childrenrange;
  }

  public void setChildrenrange(String childrenrange) {
    this.childrenrange = childrenrange;
  }

  public JSONArray getChildren() {
    return children;
  }

  public void setChildren(JSONArray children) {
    this.children = children;
  }

  /**
   * Deserializes a capability object from the given JSON object.
   * 
   * @param json a {@link JSONObject}
   */
  public static Capability fromJson(JSONObject json) {
    Capability capability = new Capability();

    if (json.has("objectID")) {
      capability.setObjectId(json.optString("objectID"));
    }
    if (json.has("objectName")) {
      capability.objectName = json.getString("objectName");
    }
    if (json.has("parentURI")) {
      capability.parentUri = json.getString("parentURI");
    }
    if (json.has("parentID")) {
      capability.parentId = json.getString("parentID");
    }
    // default values
    capability.objectType = MediaTypes.CAPABILITY;

    capability.metadata = json.optJSONObject("metadata");
    if (capability.metadata == null) {
      capability.metadata = new JSONObject();
    }
    capability.capabilities = json.optJSONObject("capabilities");
    if (capability.capabilities == null) {
      capability.capabilities = new JSONObject();
    }

    if (json.has("childrenrange")) {
      capability.childrenrange = json.optString("childrenrange");
    }
    if (json.has("children")) {
      capability.children = json.optJSONArray("children");
    }

    return capability;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();

    json.putOpt("objectType", objectType);
    json.putOpt("objectName", objectName);
    json.putOpt("parentURI", parentUri);
    json.putOpt("parentID", parentId);
    json.putOpt("capabilities", capabilities);
    json.putOpt("metadata", metadata);
    json.putOpt("childrenrange", childrenrange);
    json.putOpt("children", children);

    return json;
  }

  @Override
  public String toString() {
    return "Capability [objectId=" + getObjectId() + ", "
        + (objectType != null ? "objectType=" + objectType + ", " : "")
        + (objectName != null ? "objectName=" + objectName + ", " : "")
        + (parentUri != null ? "parentUri=" + parentUri + ", " : "")
        + (parentId != null ? "parentId=" + parentId + ", " : "")
        + (metadata != null ? "metadata=" + metadata + ", " : "")
        + (capabilities != null ? "capabilities=" + capabilities + ", " : "")
        + (childrenrange != null ? "childrenrange=" + childrenrange + ", " : "")
        + (children != null ? "children=" + children : "") + "]";
  }
}
