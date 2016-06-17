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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.util.ObjectId;

public class CdmiObject {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(CdmiObject.class);

  private static int eNum = 99999;

  private String objectId;

  /**
   * Creates a new CDMI object.
   */
  public CdmiObject() {
    this.objectId = ObjectId.getObjectId(eNum);
  }

  /**
   * Creates a new CDMI object with the given object id.
   * 
   * @param objectId the object's id
   */
  public CdmiObject(String objectId) {
    if (objectId != null && !objectId.equals("")) {
      this.objectId = objectId;
    } else {
      this.objectId = ObjectId.getObjectId(eNum);
    }
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  /**
   * Calculates the childrenrange parameter.
   * 
   * @param children the {@link JSONArray} of the object's children
   * @return the children range as {@link String}
   */
  public static String getChildrenRange(JSONArray children) {
    String childrenRange = "";
    if (children != null && children.length() == 1) {
      childrenRange = "0";
    }
    if (children != null && children.length() > 1) {
      childrenRange = "0-" + String.valueOf(children.length() - 1);
    }
    return childrenRange;
  }

  /**
   * Deserializes a CDMI object from the given JSON.
   * 
   * @param json a {@link JSONObject}
   */
  public static CdmiObject fromJson(JSONObject json) {
    return new CdmiObject(json.optString("objectID"));
  }

  /**
   * Serializes this object as a JSON object.
   * 
   * @return the serialized {@link JSONObject}
   */
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("objectID", objectId);
    return json;
  }

  @Override
  public String toString() {
    return "CdmiObject [" + (objectId != null ? "objectId=" + objectId : "") + "]";
  }
}
