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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CdmiObjectDao;
import org.snia.cdmiserver.dao.ContainerDao;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.Container;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ContainerDaoImpl implements ContainerDao {

  private static final Logger log = LoggerFactory.getLogger(ContainerDaoImpl.class);

  private CdmiObjectDao cdmiObjectDao;

  public CdmiObjectDao getCdmiObjectDao() {
    return cdmiObjectDao;
  }

  public void setCdmiObjectDao(CdmiObjectDao cdmiObjectDao) {
    this.cdmiObjectDao = cdmiObjectDao;
  }

  @Override
  public Container createByPath(String path, Container containerRequest) {
    if (path == null || containerRequest == null) {
      return null;
    }

    Path urlPath = Paths.get(path.trim());
    Path parentPath = urlPath.getParent();
    if (parentPath == null) {
      // return root container
      return (Container) cdmiObjectDao.getCdmiObjectByPath("/");
    }

    // create the container meta-data files
    Container parentContainer =
        (Container) cdmiObjectDao.getCdmiObjectByPath(parentPath.toString());

    if (parentContainer == null) {
      parentContainer =
          createByPath(parentPath.toString(), Container.fromJson(new JSONObject("{}")));
    }

    Container container = new Container(urlPath.getFileName().toString(), parentPath.toString(),
        parentContainer.getObjectId());

    if (parentContainer.getChildren() == null) {
      parentContainer.setChildren(new JSONArray());
    }

    JSONArray children = parentContainer.getChildren();
    JSONArray filteredChildren = new JSONArray();

    for (int i = 0; i < children.length(); i++) {
      if (!children.get(i).equals(container.getObjectName())) {
        filteredChildren.put(children.get(i));
      }
    }

    filteredChildren.put(container.getObjectName());
    parentContainer.setChildren(filteredChildren);

    String childrenRange = CdmiObject.getChildrenRange(parentContainer.getChildren());
    parentContainer.setChildrenrange(childrenRange);

    container.setCompletionStatus("Complete");
    container.setMetadata(containerRequest.getMetadata());
    // container.setCapabilitiesUri(containerRequest.getCapabilitiesUri());

    container = (Container) cdmiObjectDao.createCdmiObject(container, urlPath.toString());
    cdmiObjectDao.updateCdmiObject(parentContainer);
    cdmiObjectDao.updateCdmiObject(parentContainer, parentPath.toString());

    return container;
  }

  @Override
  public Container deleteByPath(String path) {
    Container container = null;
    try {
      log.debug("delete container {}", path.trim());

      container = (Container) cdmiObjectDao.getCdmiObjectByPath(path.trim());

      if (container != null) {

        cdmiObjectDao.deleteCdmiObject(container.getObjectId());
        cdmiObjectDao.deleteCdmiObjectByPath(path.trim());

        // removeChild(containerPath.getFileName().toString(),
        // containerPath.getParent().toString());
      }
    } catch (Exception ex) {
      log.error("ERROR: {}", ex.getMessage());
    }
    return container;
  }

  @Override
  public Container findByObjectId(String objectId) {
    return (Container) cdmiObjectDao.getCdmiObject(objectId);
  }

  @Override
  public Container findByPath(String path) {
    return (Container) cdmiObjectDao.getCdmiObjectByPath(path.trim());
  }

  @Override
  public boolean isContainer(String path) {
    try {
      return (cdmiObjectDao.getCdmiObjectByPath(path.trim()) instanceof Container);
    } catch (Exception ex) {
      log.error("ERROR: {}", ex.getMessage());
      return false;
    }
  }

}
