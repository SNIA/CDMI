/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.cdmiserver.dao.redis;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CapabilityDao;
import org.snia.cdmiserver.dao.CdmiObjectDao;
import org.snia.cdmiserver.model.Capability;
import org.snia.cdmiserver.model.CdmiObject;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CapabilityDaoImpl implements CapabilityDao {

  private static final Logger log = LoggerFactory.getLogger(CapabilityDaoImpl.class);

  private CdmiObjectDao cdmiObjectDao;

  public CdmiObjectDao getCdmiObjectDao() {
    return cdmiObjectDao;
  }

  public void setCdmiObjectDao(CdmiObjectDao cdmiObjectDao) {
    this.cdmiObjectDao = cdmiObjectDao;
  }

  @Override
  public Capability findByObjectId(String objectId) {
    return (Capability) cdmiObjectDao.getCdmiObject(objectId);
  }

  @Override
  public Capability findByPath(String path) {
    return (Capability) cdmiObjectDao.getCdmiObjectByPath(path.trim());
  }

  @Override
  public Capability createByPath(String path, Capability capabilityRequest) {
    Path urlPath = Paths.get(path.trim());
    Path parentPath = urlPath.getParent();
    if (parentPath == null) {
      return null;
    }

    log.debug("create the capability meta-data files");
    log.debug("get parent object {}", parentPath.toString());
    Capability parentCapability =
        (Capability) cdmiObjectDao.getCdmiObjectByPath(parentPath.toString());
    log.debug("parent object {}", parentCapability.toString());
    
    Capability capability = new Capability(urlPath.getFileName().toString(), parentPath.toString(),
        parentCapability.getObjectId());
    log.debug("create capability {}", capability.toJson());
    
    if (parentCapability.getChildren() == null) {
      parentCapability.setChildren(new JSONArray());
    }

    JSONArray children = parentCapability.getChildren();
    JSONArray filteredChildren = new JSONArray();

    for (int i = 0; i < children.length(); i++) {
      if (!children.get(i).equals(capability.getObjectName())) {
        filteredChildren.put(children.get(i));
      }
    }

    filteredChildren.put(capability.getObjectName());
    parentCapability.setChildren(filteredChildren);

    String childrenRange = CdmiObject.getChildrenRange(parentCapability.getChildren());
    parentCapability.setChildrenrange(childrenRange);

    capability.setMetadata(capabilityRequest.getMetadata());
    capability.setCapabilities(capabilityRequest.getCapabilities());

    // if (cdmiObjectDao.createCdmiObject(capability, urlPath.toString()) == null) {
    // return (Capability) cdmiObjectDao.deleteCdmiObjectByPath(urlPath.toString());
    // }

    capability = (Capability) cdmiObjectDao.createCdmiObject(capability, urlPath.toString());
    cdmiObjectDao.updateCdmiObject(parentCapability);
    cdmiObjectDao.updateCdmiObject(parentCapability, parentPath.toString());
    // capability = (Capability) cdmiObjectDao.getCdmiObjectByPath(urlPath.toString());
    // log.debug("return capability {}", capability.toJson());
    return capability;
  }

}
