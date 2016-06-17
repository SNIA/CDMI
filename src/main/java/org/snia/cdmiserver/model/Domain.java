/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package org.snia.cdmiserver.model;

import org.json.JSONArray;
import org.json.JSONObject;
import org.snia.cdmiserver.util.MediaTypes;

public class Domain extends CdmiObject {

  private String objectType;
  private String objectName;
  private String parentUri;
  private String parentId;
  private String domainUri;
  private String capabilitiesUri;

  private JSONObject metadata;
  private String childrenrange;
  private JSONArray children;

  private String deserializevalue;
  private String move;
  private String copy;
  private String deserialize;

  /**
   * Creates a new domain with the mandatory fields.
   * 
   * @param objectName the domain's name
   * @param parentUri the domain's parent URI
   * @param parentId the domain's parent objectId
   */
  public Domain(String objectName, String parentUri, String parentId) {
    super();
    this.objectName = objectName;
    this.parentUri = parentUri;
    this.parentId = parentId;
    // default values
    this.objectType = MediaTypes.ACCOUNT;
    this.domainUri = "/cdmi_domains";
    this.capabilitiesUri = "/cdmi_capabilities/domain";
    this.metadata = new JSONObject();
  }

  /**
   * Creates a new Domain from the given JSON object.
   * 
   * @param json a {@link JSONObject}
   */
  public Domain(JSONObject json) {
    super(json.getString("objectID"));
    this.objectName = json.getString("objectName");
    this.parentUri = json.getString("parentURI");
    this.parentId = json.getString("parentID");
    // default values
    this.objectType = MediaTypes.ACCOUNT;
    this.domainUri = json.optString("domainURI", "/cdmi_domains");
    this.capabilitiesUri = json.optString("capabilitiesURI", "/cdmi_capabilities/domain");
    this.metadata = json.optJSONObject("metadata");
    if (this.metadata == null) {
      this.metadata = new JSONObject();
    }
    // optional values
    this.childrenrange = json.optString("childrenrange");
    this.children = json.optJSONArray("children");
    this.deserializevalue = json.optString("deserializevalue");
    this.move = json.optString("move");
    this.copy = json.optString("copy");
    this.deserialize = json.optString("deserialize");
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

  public JSONObject getMetadata() {
    return metadata;
  }

  public void setMetadata(JSONObject metadata) {
    this.metadata = metadata;
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

  public String getDeserializedvalue() {
    return deserializevalue;
  }

  public void setDeserializedvalue(String deserializevalue) {
    this.deserializevalue = deserializevalue;
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

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();

    json.putOpt("objectType", objectType);
    json.putOpt("objectName", objectName);
    json.putOpt("parentURI", parentUri);
    json.putOpt("parentID", parentId);
    json.putOpt("domainURI", domainUri);
    json.putOpt("capabilitiesURI", capabilitiesUri);
    json.putOpt("metadata", metadata);
    json.putOpt("childrenrange", childrenrange);
    json.putOpt("children", children);
    json.putOpt("deserializevalue", deserializevalue);
    json.putOpt("deserialize", deserialize);
    json.putOpt("move", move);
    json.putOpt("copy", copy);
    json.putOpt("deserialize", deserialize);

    return json;
  }

  @Override
  public String toString() {
    return "Domain [objectId=" + getObjectId() + ", "
        + (objectType != null ? "objectType=" + objectType + ", " : "")
        + (objectName != null ? "objectName=" + objectName + ", " : "")
        + (parentUri != null ? "parentUri=" + parentUri + ", " : "")
        + (parentId != null ? "parentId=" + parentId + ", " : "")
        + (domainUri != null ? "domainUri=" + domainUri + ", " : "")
        + (capabilitiesUri != null ? "capabilitiesUri=" + capabilitiesUri + ", " : "")
        + (metadata != null ? "metadata=" + metadata + ", " : "")
        + (childrenrange != null ? "childrenrange=" + childrenrange + ", " : "")
        + (children != null ? "children=" + children + ", " : "")
        + (deserializevalue != null ? "deserializevalue=" + deserializevalue + ", " : "")
        + (move != null ? "move=" + move + ", " : "") + (copy != null ? "copy=" + copy + ", " : "")
        + (deserialize != null ? "deserialize=" + deserialize : "") + "]";
  }
}
