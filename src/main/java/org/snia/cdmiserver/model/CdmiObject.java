/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package org.snia.cdmiserver.model;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.util.ObjectID;

public class CdmiObject {

	private static final Logger log = LoggerFactory.getLogger(CdmiObject.class);

	private static final int eNum = 8;

	static final String objectId = "objectID";
	private Map<String, Object> attributeMap;

	public CdmiObject() {
		this.attributeMap = new HashMap<>();
		this.attributeMap.put(CdmiObject.objectId, ObjectID.getObjectID(eNum));
		log.debug("create new object {}", toString());
	}

	public CdmiObject(Map<String, Object> attributeMap) {
		if (attributeMap == null)
			throw new IllegalArgumentException("attributeMap can't be null");

		this.attributeMap = attributeMap;

		// ensure objectID
		if (attributeMap.get(CdmiObject.objectId) == null)
			this.attributeMap.put(CdmiObject.objectId, ObjectID.getObjectID(eNum));
		log.debug("create new object {}", toString());
	}

	public CdmiObject(String objectId) {
		if (objectId == null)
			throw new IllegalArgumentException("objectId can't be null");

		this.attributeMap = new HashMap<>();
		this.attributeMap.put(CdmiObject.objectId, objectId);
		log.debug("create new object {}", toString());
	}

	public CdmiObject(String objectId, Map<String, Object> attributeMap) {
		if (objectId == null)
			throw new IllegalArgumentException("objectId can't be null");

		if (attributeMap == null)
			throw new IllegalArgumentException("attributeMap can't be null");

		this.attributeMap = attributeMap;

		// ensure objectID
		if (attributeMap.get(CdmiObject.objectId) == null)
			this.attributeMap.put(CdmiObject.objectId, ObjectID.getObjectID(eNum));
		log.debug("create new object {}", toString());
	}

	public CdmiObject(JSONObject json) {
		this.attributeMap = new HashMap<>();
		for (Object key : json.keySet())
			this.attributeMap.put((String) key, json.get((String) key));

		// ensure objectID
		if (attributeMap.get(CdmiObject.objectId) == null)
			this.attributeMap.put(CdmiObject.objectId, ObjectID.getObjectID(eNum));
		log.debug("create new object {}", toString());
	}

	public String getObjectId() {
		return (String) attributeMap.get(objectId);
	}

	public void setObjectId(String objectId) {
		attributeMap.put(CdmiObject.objectId, objectId);
	}

	public Map<String, Object> getAttributeMap() {
		return attributeMap;
	}

	public void setAttributeMap(Map<String, Object> attributeMap) {
		this.attributeMap = attributeMap;
	}

	public JSONObject toJson() {
		return new JSONObject(this.attributeMap);
	}

	@Override
	public String toString() {
		return "CdmiObject [" + (attributeMap != null ? "attributeMap=" + attributeMap : "") + "]";
	}
}
