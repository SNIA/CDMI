/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.cdmiserver.dao.redis;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CdmiObjectDao;
import org.snia.cdmiserver.model.Capability;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.Container;
import org.snia.cdmiserver.model.DataObject;
import org.snia.cdmiserver.model.Domain;
import org.snia.cdmiserver.util.MediaTypes;
import org.springframework.data.redis.core.StringRedisTemplate;

public class CdmiObjectDaoImpl implements CdmiObjectDao {

  private static final Logger log = LoggerFactory.getLogger(CdmiObjectDaoImpl.class);

  private StringRedisTemplate redisTemplate;

  public StringRedisTemplate getRedisTemplate() {
    return redisTemplate;
  }

  public void setRedisTemplate(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public CdmiObject createCdmiObject(CdmiObject cdmiObject) {
    String objectId = cdmiObject.getObjectId();
    String objectJson = cdmiObject.toJson().toString();

    if (redisTemplate.opsForValue().setIfAbsent("objectid:" + objectId, objectJson)) {
      log.debug("set {} {}", "objectid:" + objectId, objectJson);
      return cdmiObject;
    }

    log.debug("object id {} already exists", objectId);
    return getCdmiObject(objectId);
  }

  @Override
  public CdmiObject createCdmiObject(CdmiObject cdmiObject, String path) {
    if (redisTemplate.opsForValue().setIfAbsent(path, cdmiObject.toJson().toString())) {
      CdmiObject object = createCdmiObject(cdmiObject);
      log.debug("set {} {}", path, object.toJson().toString());
      return cdmiObject;
    }

    log.debug("path {} already exists", path);
    return getCdmiObjectByPath(path);
  }

  @Override
  public CdmiObject getCdmiObject(String objectId) {
    String object = redisTemplate.opsForValue().get("objectid:" + objectId);

    try {
      JSONObject objectJson = new JSONObject(object);
      String objectType = objectJson.optString("objectType");

      if (objectType != null) {
        if (objectType.equals(MediaTypes.CONTAINER)) {
          return Container.fromJson(objectJson);
        } else if (objectType.equals(MediaTypes.DATA_OBJECT)) {
          return DataObject.fromJson(objectJson);
        } else if (objectType.equals(MediaTypes.ACCOUNT)) {
          return new Domain(objectJson);
        } else if (objectType.equals(MediaTypes.CAPABILITY)) {
          return Capability.fromJson(objectJson);
        } else {
          return CdmiObject.fromJson(objectJson);
        }
      }
    } catch (Exception ex) {
      //ex.printStackTrace();
      log.error("{} {}", ex.getClass().getName(), ex.getMessage());
    }
    return null;
  }

  @Override
  public CdmiObject updateCdmiObject(CdmiObject cdmiObject) {
    String objectId = cdmiObject.getObjectId();
    String objectJson = cdmiObject.toJson().toString();

    redisTemplate.opsForValue().set("objectid:" + objectId, objectJson);
    log.debug("set {} {}", "objectid:" + objectId, objectJson);
    return cdmiObject;
  }

  @Override
  public CdmiObject updateCdmiObject(CdmiObject cdmiObject, String path) {
    redisTemplate.opsForValue().set(path, cdmiObject.toJson().toString());
    log.debug("set {} {}", path, cdmiObject.toJson().toString());
    return cdmiObject;
  }

  @Override
  public CdmiObject deleteCdmiObject(String objectId) {
    CdmiObject cdmiObject = getCdmiObject(objectId);

    redisTemplate.delete("objectid:" + objectId);

    return cdmiObject;
  }

  @Override
  public CdmiObject getCdmiObjectByPath(String path) {
    String object = redisTemplate.opsForValue().get(path);

    try {
      JSONObject objectJson = new JSONObject(object);
      String objectType = objectJson.optString("objectType");

      if (objectType != null) {
        if (objectType.equals(MediaTypes.CONTAINER)) {
          return Container.fromJson(objectJson);
        } else if (objectType.equals(MediaTypes.DATA_OBJECT)) {
          return DataObject.fromJson(objectJson);
        } else if (objectType.equals(MediaTypes.ACCOUNT)) {
          return new Domain(objectJson);
        } else if (objectType.equals(MediaTypes.CAPABILITY)) {
          return Capability.fromJson(objectJson);
        } else {
          return CdmiObject.fromJson(objectJson);
        }
      }
    } catch (Exception ex) {
      //ex.printStackTrace();
      log.error("{} {}", ex.getClass().getName(), ex.getMessage());
    }
    return null;
  }

  @Override
  public CdmiObject deleteCdmiObjectByPath(String path) {
    CdmiObject cdmiObject = getCdmiObjectByPath(path);

    deleteCdmiObject(cdmiObject.getObjectId());

    redisTemplate.delete(path);

    return cdmiObject;
  }

}
