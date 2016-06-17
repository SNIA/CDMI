/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package org.snia.cdmiserver.dao.filesystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CdmiObjectDao;
import org.snia.cdmiserver.dao.DomainDao;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.Domain;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DomainDaoImpl implements DomainDao {

  private static final Logger LOG = LoggerFactory.getLogger(DomainDaoImpl.class);

  private String baseDirectoryName;

  private CdmiObjectDao cdmiObjectDao;

  public String getBaseDirectoryName() {
    return baseDirectoryName;
  }

  public void setBaseDirectoryName(String baseDirectoryName) {
    this.baseDirectoryName = baseDirectoryName;
  }

  public CdmiObjectDao getCdmiObjectDao() {
    return cdmiObjectDao;
  }

  public void setCdmiObjectDao(CdmiObjectDao cdmiObjectDao) {
    this.cdmiObjectDao = cdmiObjectDao;
  }

  @Override
  public CdmiObject createByPath(String path, Domain domainRequest) {
    LOG.debug("create domain {} {}", path.trim(), domainRequest.toString());
    // TODO
    return null;
  }

  @Override
  public void deleteByPath(String path) {
    LOG.debug("delete domain {}", path.trim());
    // TODO
  }

  @Override
  public CdmiObject findByObjectId(String objectId) {
    return cdmiObjectDao.getCdmiObject(objectId);
  }

  @Override
  public CdmiObject findByPath(String path) {
    Path domainPath = Paths.get(baseDirectoryName.trim(), path.trim());
    LOG.debug("path is {}", path);
    return (Domain) cdmiObjectDao.getCdmiObjectByPath(domainPath.toString());
  }

  @Override
  public CdmiObject updateByPath(String path, Domain domain, String[] requestedFields) {
    // TODO Auto-generated method stub
    return null;
  }
}
