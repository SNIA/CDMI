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
import org.snia.cdmiserver.dao.CdmiObjectDao;
import org.snia.cdmiserver.dao.DataObjectDao;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.Container;
import org.snia.cdmiserver.model.DataObject;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DataObjectDaoImpl implements DataObjectDao {

  private static final Logger log = LoggerFactory.getLogger(DataObjectDaoImpl.class);

  private CdmiObjectDao cdmiObjectDao;

  public CdmiObjectDao getCdmiObjectDao() {
    return cdmiObjectDao;
  }

  public void setCdmiObjectDao(CdmiObjectDao cdmiObjectDao) {
    this.cdmiObjectDao = cdmiObjectDao;
  }

  @Override
  public DataObject createByPath(String path, DataObject dataObjectRequest) {
    Path urlPath = Paths.get(path.trim());
    Path parentPath = urlPath.getParent();
    if (parentPath == null) {
      // root container
      return null;
    }

    // create the data object meta-data files
    Container parentContainer =
        (Container) cdmiObjectDao.getCdmiObjectByPath(parentPath.toString());

    DataObject dataObject = new DataObject(urlPath.getFileName().toString(), parentPath.toString(),
        parentContainer.getObjectId());

    if (parentContainer.getChildren() == null) {
      parentContainer.setChildren(new JSONArray());
    }

    JSONArray children = parentContainer.getChildren();
    JSONArray filteredChildren = new JSONArray();

    for (int i = 0; i < children.length(); i++) {
      if (!children.get(i).equals(dataObject.getObjectName())) {
        filteredChildren.put(children.get(i));
      }
    }

    filteredChildren.put(dataObject.getObjectName());
    parentContainer.setChildren(filteredChildren);

    String childrenRange = CdmiObject.getChildrenRange(parentContainer.getChildren());
    parentContainer.setChildrenrange(childrenRange);

    dataObject.setCompletionStatus("Complete");
    dataObject.setMetadata(dataObjectRequest.getMetadata());
    // dataObject.setCapabilitiesUri(dataObjectRequest.getCapabilitiesUri());

    dataObject = (DataObject) cdmiObjectDao.createCdmiObject(dataObject, urlPath.toString());
    cdmiObjectDao.updateCdmiObject(parentContainer);
    cdmiObjectDao.updateCdmiObject(parentContainer, parentPath.toString());

    return dataObject;
  }

  @Override
  public DataObject createNonCdmiByPath(String path, String contentType,
      DataObject dataObjectRequest) {
    throw new UnsupportedOperationException("DataObjectDaoImpl.createNonCDMIByPath()");
  }

  @Override
  public DataObject createById(String objectId, DataObject dataObj) {
    throw new UnsupportedOperationException("DataObjectDaoImpl.createById()");
  }

  @Override
  public DataObject updateContent(String path, byte[] content) {
    throw new UnsupportedOperationException("DataObjectDaoImpl.updateContent()");
  }

  @Override
  public DataObject deleteByPath(String path) {
    DataObject dataObject = null;
    try {
      log.debug("delete data object {}", path.trim());

      dataObject = (DataObject) cdmiObjectDao.getCdmiObjectByPath(path.trim());

      if (dataObject != null) {
        cdmiObjectDao.deleteCdmiObject(dataObject.getObjectId());
        cdmiObjectDao.deleteCdmiObjectByPath(path.trim());
      }
    } catch (Exception e) {
      log.error("ERROR: {}", e.getMessage());
    }
    return dataObject;
  }

  @Override
  public DataObject findByObjectId(String objectId) {
    DataObject dataObject = (DataObject) cdmiObjectDao.getCdmiObject(objectId);
    return dataObject;
  }

  @Override
  public DataObject findByPath(String path) {
    DataObject dataObject = (DataObject) cdmiObjectDao.getCdmiObjectByPath(path.trim());
    return dataObject;
  }

}
