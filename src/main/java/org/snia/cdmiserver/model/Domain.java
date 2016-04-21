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

import java.util.HashMap;
import java.util.Map;

public class Domain extends CdmiObject {

  // DataObject creation fields
  private static final String metadata = "metadata";
  private static final String deserialize = "deserialize";
  private static final String deserializevalue = "deserializevalue";
  private static final String copy = "copy";
  private static final String move = "move";


  // DataObject representation fields
  private static final String objectType = "objectType";
  private static final String objectName = "objectName";
  private static final String parentURI = "parentURI";
  private static final String parentID = "parentID";
  private static final String domainURI = "domainURI";
  private static final String capabilitiesURI = "capabilitiesURI";
  private static final String children = "children";
  private static final String childrenrange = "childrenrange";

  private String ObjectURI;

  public Domain() {
    super();
  }

  public Domain(JSONObject json) {
    super(json);
  }

  public Domain(Map<String, Object> attributeMap) {
    super(attributeMap);
  }

  public Map<String, Object> getMetadata() {
    if (getAttributeMap().get(metadata) != null) {
      try {
        JSONObject json = (JSONObject) getAttributeMap().get(metadata);
        HashMap<String, Object> map = new HashMap<>();
        for (Object key : json.keySet()) {
          map.put((String) key, json.get((String) key));
        }
        return map;
      } catch (ClassCastException e) {
        return (Map<String, Object>) getAttributeMap().get(metadata);
      }
    }
    return null;
  }

  public void setMetadata(Map<String, Object> metadata) {
    getAttributeMap().put(Domain.metadata, metadata);
  }

  public String getCopy() {
    return (String) getAttributeMap().get(copy);
  }

  public void setCopy(String copy) {
    getAttributeMap().put(Domain.copy, copy);
  }

  public String getMove() {
    return (String) getAttributeMap().get(move);
  }

  public void setMove(String move) {
    getAttributeMap().put(Domain.move, move);
  }

  public String getDeserialize() {
    return (String) getAttributeMap().get(deserialize);
  }

  public void setDeserialize(String deserialize) {
    getAttributeMap().put(Domain.deserialize, deserialize);
  }

  public String getDeserializevalue() {
    return (String) getAttributeMap().get(deserializevalue);
  }

  public void setDeserializevalue(String deserializevalue) {
    getAttributeMap().put(Domain.deserializevalue, deserializevalue);
  }

  public String getObjectType() {
    return (String) getAttributeMap().get(objectType);
  }

  public void setObjectType(String objectType) {
    getAttributeMap().put(Domain.objectType, objectType);
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
    getAttributeMap().put(Domain.objectName, objectName);
  }

  public String getParentURI() {
    return (String) getAttributeMap().get(parentURI);
  }

  public void setParentURI(String parentURI) {
    getAttributeMap().put(Domain.parentURI, parentURI);
  }

  public String getParentID() {
    return (String) getAttributeMap().get(Domain.parentID);
  }

  public void setParentID(String parentID) {
    getAttributeMap().put(Domain.parentID, parentID);
  }

  public String getDomainURI() {
    return (String) getAttributeMap().get(domainURI);
  }

  public void setDomainURI(String domainURI) {
    getAttributeMap().put(Domain.domainURI, domainURI);
  }

  public String getCapabilitiesURI() {
    return (String) getAttributeMap().get(capabilitiesURI);
  }

  public void setCapabilitiesURI(String capabilitiesURI) {
    getAttributeMap().put(Domain.capabilitiesURI, capabilitiesURI);
  }

  public String getChildrenrange() {
    return (String) getAttributeMap().get(childrenrange);
  }

  public void setChildrenrange(String childrenrange) {
    getAttributeMap().put(Domain.childrenrange, childrenrange);
  }

  public JSONArray getChildren() {
    if (getAttributeMap().get(children) != null) {
      JSONArray json = (JSONArray) getAttributeMap().get(children);
      return json;
    }
    return null;
  }

  public void setChildren(JSONArray children2) {
    getAttributeMap().put(Domain.children, children2);
  }

  public String getObjectURI() {
    return ObjectURI;
  }

  public void setObjectURI(String objectURI) {
    ObjectURI = objectURI;
  }



}
