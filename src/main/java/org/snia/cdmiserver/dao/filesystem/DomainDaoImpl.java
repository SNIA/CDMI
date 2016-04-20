/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package org.snia.cdmiserver.dao.filesystem;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.DomainDao;
import org.snia.cdmiserver.exception.BadRequestException;
import org.snia.cdmiserver.exception.ConflictException;
import org.snia.cdmiserver.exception.NotFoundException;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.Domain;
import org.snia.cdmiserver.util.MediaTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class DomainDaoImpl implements DomainDao {

  private static final Logger LOG = LoggerFactory.getLogger(DomainDaoImpl.class);


  @Value("${cdmi.capabilitiesUri}")
  private String capabilitiesUri;

  @Value("${cdmi.domainUri}")
  private String domainUri;

  @Value("${cdmi.data.baseDirectory}")
  private String baseDirectoryName;

  @Autowired
  private CdmiObjectDaoImpl cdmiObjectDaoImpl;

  @Override
  public CdmiObject createByPath(String path, Domain domainRequest) {
    LOG.debug("create domain {} {}", path.trim(), domainRequest.toString());

    Domain domain = (Domain) cdmiObjectDaoImpl.createCdmiObject(new Domain());
    LOG.debug("domain is {}", domain.toJson().toString());
    if (domain != null) {

      Path relPath = Paths.get(path.trim());
      try {
        Path domainPath = Paths.get(baseDirectoryName.trim(), path.trim());
        Path directory = Files.createDirectory(domainPath);
        LOG.debug("created directory {}", directory.toString());

        CdmiObject parentObject = cdmiObjectDaoImpl.getCdmiObjectByPath(
            Paths.get(baseDirectoryName, relPath.getParent().toString()).toString());
        LOG.debug("parent object {}", parentObject.toString());

        domain.setObjectType(MediaTypes.ACCOUNT);
        domain.setObjectName(relPath.getFileName().toString());
        domain.setParentURI(relPath.getParent().toString());
        domain.setParentID(parentObject.getObjectId());
        domain.setCapabilitiesURI(capabilitiesUri);
        domain.setDomainURI(domainUri);
        domain.setMetadata(domainRequest.getMetadata());

        cdmiObjectDaoImpl.updateCdmiObject(domain);

        Path file = Paths.get(baseDirectoryName, relPath.toString().trim());
        if (cdmiObjectDaoImpl.createCdmiObject(domain, file.toString()) == null)
          cdmiObjectDaoImpl.updateCdmiObject(domain, file.toString());

        // Add child-entry for parent
        Domain parentDomain = (Domain) parentObject;
        JSONArray children = parentDomain.getChildren();
        if (children == null)
          children = new JSONArray();
        children.put(domain.getObjectName());
        parentDomain.setChildren(children);
        parentDomain.setChildrenrange("0-" + String.valueOf(children.length() - 1));
        cdmiObjectDaoImpl.updateCdmiObject(parentDomain);
        String parentPath = file.getParent().toString();
        cdmiObjectDaoImpl.updateCdmiObject(parentDomain, parentPath);

        return domain;
      } catch (FileAlreadyExistsException e) {
        LOG.warn("object alredy exists");
        cdmiObjectDaoImpl.deleteCdmiObject(domain.getObjectId());
        throw new ConflictException("object already exists");
        // TODO here updateDomain
      } catch (NoSuchFileException | NullPointerException e) {
        LOG.error("ERROR: {}", e.getMessage());
        cdmiObjectDaoImpl.deleteCdmiObject(domain.getObjectID());
        throw new NotFoundException("resource was not found at the specified uri");
      } catch (Exception e) {
        LOG.error("ERROR: {}", e.getMessage());
        e.printStackTrace();
        cdmiObjectDaoImpl.deleteCdmiObject(domain.getObjectID());
      }
    }
    return null;
  }

  @Override
  public void deleteByPath(String path) {
    if (path.equals("/cdmi_domains/") || path.equals("/cdmi_domains"))
      throw new BadRequestException("bad request");
    LOG.debug("delete domain {}", path.trim());
    Path domainPath = Paths.get(baseDirectoryName.trim(), path.trim());

    Domain domain = (Domain) cdmiObjectDaoImpl.getCdmiObjectByPath(domainPath.toString());

    if (domain != null) {
      try {
        LOG.debug("delete directory {}", domainPath.toString());
        Files.delete(domainPath);

        cdmiObjectDaoImpl.deleteCdmiObject(domain.getObjectId());
        cdmiObjectDaoImpl.deleteCdmiObjectByPath(domainPath.toString());

        // delete childentry from parent
        String parentPath = domainPath.getParent().toString();
        CdmiObject parentObject = cdmiObjectDaoImpl.getCdmiObjectByPath(parentPath);
        LOG.debug("parent object {}", parentObject.toString());
        Domain parentDomain = (Domain) parentObject;
        JSONArray children = parentDomain.getChildren();
        for (int i = 0; i < children.length(); i++) {
          if (children.getString(i).equals(domain.getObjectName())) {
            children.remove(i);
            // break;
          }
        }
        parentDomain.setChildren(children);
        parentDomain.setChildrenrange("0-" + String.valueOf(children.length() - 1));
        cdmiObjectDaoImpl.updateCdmiObject(parentDomain);
        cdmiObjectDaoImpl.updateCdmiObject(parentDomain, parentPath);

      } catch (NoSuchFileException e) {
        LOG.warn("domain not found");
        throw new NotFoundException("domain not found");
      } catch (DirectoryNotEmptyException e) {
        LOG.warn("domain not empty");
        throw new BadRequestException("domain has subdomains");
      } catch (IOException e) {
        LOG.error("ERROR: {}", e.getMessage());
      } catch (Exception e) {
        LOG.error("ERROR: {}", e.getMessage());
      }
    }


  }

  @Override
  public CdmiObject findByObjectId(String objectId) {
    return cdmiObjectDaoImpl.getCdmiObject(objectId);
  }

  @Override
  public CdmiObject findByPath(String path) {
    Path domainPath = Paths.get(baseDirectoryName.trim(), path.trim());
    LOG.debug("path is {}", path);
    return (Domain) cdmiObjectDaoImpl.getCdmiObjectByPath(domainPath.toString());
  }

}
