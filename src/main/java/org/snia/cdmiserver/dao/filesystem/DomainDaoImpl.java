/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package org.snia.cdmiserver.dao.filesystem;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.regex.Pattern;

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
        if (domainRequest.getCopy() != null) {
          if (findByPath(path) == null) {
            return copyWithChildrenNew(domainRequest, path);
          } else {
            return updateByPath(path, domainRequest, null);
          }
        } else if (domainRequest.getMove() != null) {
          try {
            return move(domainRequest, domainRequest.getMove(), path);
          } catch (NotFoundException e) {
            throw new BadRequestException("Bad Request");
          }
        } else {

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
          domain.setCapabilitiesURI(capabilitiesUri + "/domain");
          domain.setDomainURI(domainUri);
          domain.setMetadata(domainRequest.getMetadata());

          cdmiObjectDaoImpl.updateCdmiObject(domain);

          Path file = Paths.get(baseDirectoryName, relPath.toString().trim());
          if (cdmiObjectDaoImpl.createCdmiObject(domain, file.toString()) == null)
            cdmiObjectDaoImpl.updateCdmiObject(domain, file.toString());

          // Add child-entry for parent
          Domain parentDomain = (Domain) parentObject;
          addChild(parentDomain, domain.getObjectName(), file.getParent().toString());
        }
        return domain;
      } catch (FileAlreadyExistsException e) {
        LOG.warn("object alredy exists");
        cdmiObjectDaoImpl.deleteCdmiObject(domain.getObjectId());
        return updateByPath(path, domainRequest, null);
      } catch (NotFoundException e) {
        throw new NotFoundException(e.getMessage());
      } catch (NoSuchFileException | NullPointerException e) {
        LOG.error("ERROR: {}", e.getMessage());
        cdmiObjectDaoImpl.deleteCdmiObject(domain.getObjectID());
        e.printStackTrace();
        throw new NotFoundException("resource was not found at the specified uri");
      } catch (BadRequestException e) {
        throw new BadRequestException(e.getMessage());
      } catch (Exception e) {
        LOG.error("ERROR: {}", e.getMessage());
        e.printStackTrace();
        cdmiObjectDaoImpl.deleteCdmiObject(domain.getObjectID());
        try {
          Files.delete(Paths.get(baseDirectoryName.trim(), path.trim()));
        } catch (IOException e1) {
          LOG.error("ERROR: {}", e1.getMessage());
        }
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
        removeChild(domain.getObjectName(), parentPath);

      } catch (NoSuchFileException e) {
        LOG.warn("domain not found");
        throw new NotFoundException("domain not found");
      } catch (DirectoryNotEmptyException e) {
        LOG.warn("domain not empty");
        throw new BadRequestException("domain has subdomains");
      } catch (IOException e) {
        LOG.error("ERROR: {}", e.getMessage());
      } catch (ConflictException e) {
        throw new ConflictException("Domain already exists");
      } catch (NotFoundException e) {
        throw new NotFoundException("Domain not found");
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

  public CdmiObject updateByPath(String path, Domain domainRequest, String[] requestedFields) {
    LOG.debug("create domain {} {}", path.trim(), domainRequest.toString());

    Domain domain = (Domain) findByPath(path);
    LOG.debug("domain is {}", domain.toJson().toString());
    if (domain != null) {

      Path relPath = Paths.get(path.trim());
      try {
        // copy
        if (domainRequest.getCopy() != null) {
          String source = domainRequest.getCopy();
          String sourceFields = null;
          if (source.contains("?")) {
            sourceFields = source.split(Pattern.quote("?"))[1];
            source = source.split(Pattern.quote("?"))[0];
          }
          Domain copiedObject = (Domain) findByPath(source);
          if (domainRequest.getMetadata() != null && !domainRequest.getMetadata().isEmpty())
            copiedObject.setMetadata(domainRequest.getMetadata());
          Map<String, Object> copiedMetadata = copiedObject.getMetadata();
          Map<String, Object> metadata = domain.getMetadata();
          if (requestedFields != null && sourceFields != null)
            throw new BadRequestException(
                "The destination container object URI and the copy source object URI both specify fields");
          else if (requestedFields == null && sourceFields == null) {
            metadata = copiedMetadata;
          } else if (requestedFields == null) {
            requestedFields = sourceFields.split(";");
          }
          for (int i = 0; i < requestedFields.length; i++) {
            String field = requestedFields[i];
            if (field.contains("metadata:")) {
              String prefix = field.split("metadata:")[1];
              metadata.put(prefix, copiedMetadata.get(prefix));
            } else if (field.equals("metadata")) {
              metadata = copiedMetadata;
              break;
            } else {
              throw new BadRequestException("bad fieldname: " + field);
            }
          }

          Domain parent = (Domain) findByPath(relPath.getParent().toString());
          domain.setMetadata(metadata);
          domain.setCapabilitiesURI(copiedObject.getCapabilitiesURI());
          domain.setDomainURI(copiedObject.getDomainURI());
          domain.setObjectName(relPath.getFileName().toString());
          domain.setObjectType(MediaTypes.ACCOUNT);
          domain.setParentID(parent.getObjectId());
          domain.setParentURI(relPath.getParent().toString());
          cdmiObjectDaoImpl.updateCdmiObject(domain);
          Path file = Paths.get(baseDirectoryName, relPath.toString().trim());
          cdmiObjectDaoImpl.updateCdmiObject(domain, file.toString());
          return domain;
          // move
        } else if (domainRequest.getMove() != null) {
          throw new BadRequestException("Bad Request");
          // update
        } else {
          Map<String, Object> newAttributes = domain.getAttributeMap();

          if (requestedFields == null) {
            domain.setMetadata(domainRequest.getMetadata());
            newAttributes = domain.getAttributeMap();
          } else {
            Map<String, Object> requestAttributes = domainRequest.getAttributeMap();

            for (int i = 0; i < requestedFields.length; i++) {
              String field = requestedFields[i];
              if (field.equals("metadata")) { // only metadata shall be updated
                if (field.contains(":")) {
                  String subfield = field.split(":")[1];
                  field = field.split(":")[0];
                  JSONObject fieldObject = (JSONObject) newAttributes.get(field);
                  fieldObject.put(subfield,
                      ((JSONObject) requestAttributes.get(field)).get(subfield));
                  newAttributes.put(field, fieldObject);
                } else {
                  if (requestAttributes.containsKey(field))
                    newAttributes.put(field, requestAttributes.get(field));
                }
              }
            }
          }
          domain.setAttributeMap(newAttributes);
          cdmiObjectDaoImpl.updateCdmiObject(domain);
          Path file = Paths.get(baseDirectoryName, relPath.toString().trim());
          cdmiObjectDaoImpl.updateCdmiObject(domain, file.toString());
        }
        return domain;
      } catch (BadRequestException e) {
        throw new BadRequestException(e.getMessage());
      } catch (Exception e) {
        LOG.error("ERROR: {}", e.getMessage());
        e.printStackTrace();
        cdmiObjectDaoImpl.deleteCdmiObject(domain.getObjectID());
      }
    }
    return null;
  }

  private CdmiObject copyWithChildren(Domain domainRequest, String path) {
    Path source = Paths.get(baseDirectoryName.trim(), domainRequest.getCopy().trim());
    Path target = Paths.get(baseDirectoryName.trim(), path.trim());
    LOG.debug("CopyWithChildren source is {}", source);
    LOG.debug("CopyWithChildren target is {}", target);
    try {
      Domain domain = (Domain) cdmiObjectDaoImpl.createCdmiObject(new Domain());
      Domain oldDomain = (Domain) findByPath(domainRequest.getCopy().trim());
      FileUtils.copyDirectory(new File(source.toString()), new File(target.toString()));
      CdmiObject parentObject =
          cdmiObjectDaoImpl.getCdmiObjectByPath(target.getParent().toString());
      LOG.debug("parent object {}", parentObject.toString());
      domain.setObjectName(target.getFileName().toString());
      domain.setParentURI(target.getParent().toString());
      domain.setParentID(parentObject.getObjectId());
      domain.setObjectType(MediaTypes.ACCOUNT);
      domain.setCapabilitiesURI(capabilitiesUri + "/domain");
      domain.setDomainURI(domainUri);
      domain.setMetadata(oldDomain.getMetadata());

      if (domainRequest.getMetadata() != null && !domainRequest.getMetadata().isEmpty())
        domain.setMetadata(domainRequest.getMetadata());

      cdmiObjectDaoImpl.updateCdmiObject(domain);

      if (cdmiObjectDaoImpl.createCdmiObject(domain, target.toString()) == null)
        cdmiObjectDaoImpl.updateCdmiObject(domain, target.toString());

      // addChild to parent
      addChild((Domain) parentObject, domain.getObjectName(), target.getParent().toString());
      // addChilds
      JSONArray children = oldDomain.getChildren();
      if (children != null) {
        if (domain.getChildren() == null) {
          domain.setChildren(new JSONArray());
        }
        for (int i = 0; i < children.length(); i++) {
          addChild(domain, children.getString(i), target.toString());
        }
      }

      return domain;
    } catch (ClassCastException e) {
      throw new BadRequestException("Requested Resource is not a Doamin");
    } catch (FileAlreadyExistsException e) {
      throw new BadRequestException("Bad Request");
    } catch (IOException e) {
      throw new NotFoundException("Object not found");
    }
  }

  private Domain move(Domain domainRequest, String moveFrom, String moveTo) {
    Path source = Paths.get(baseDirectoryName.trim(), moveFrom.trim());
    Path target = Paths.get(baseDirectoryName.trim(), moveTo.trim());
    LOG.debug("Move source is {}", source);
    LOG.debug("Move target is {}", target);
    try {
      Domain domain = (Domain) findByPath(moveFrom);
      if (domain != null) {
        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        CdmiObject parentObject =
            cdmiObjectDaoImpl.getCdmiObjectByPath(target.getParent().toString());
        LOG.debug("parent object {}", parentObject.toString());
        domain.setObjectName(target.getFileName().toString());
        domain.setParentURI(Paths.get(moveTo).getParent().toString());
        domain.setParentID(parentObject.getObjectId());

        if (domainRequest.getMetadata() != null && !domainRequest.getMetadata().isEmpty())
          domain.setMetadata(domainRequest.getMetadata());

        cdmiObjectDaoImpl.updateCdmiObject(domain);

        if (cdmiObjectDaoImpl.createCdmiObject(domain, target.toString()) == null)
          cdmiObjectDaoImpl.updateCdmiObject(domain, target.toString());

        Domain newDomain = (Domain) findByPath(moveTo);
        if (newDomain != null) {
          cdmiObjectDaoImpl.deleteCdmiObjectByPath(source.toString());

          // delete childentry from old parent
          String parentPath = source.getParent().toString();
          removeChild(source.getFileName().toString(), parentPath);

          addChild((Domain) parentObject, domain.getObjectName(), target.getParent().toString());

          return newDomain;
        } else {
          throw new NotFoundException("Not found");
        }
      }
    } catch (FileAlreadyExistsException e) {
      throw new ConflictException("Domain already exists");
    } catch (IOException e) {
      LOG.error("ERROR {}", e.getMessage());
      throw new NotFoundException("domain not found");
    }
    return null;
  }

  private void removeChild(String childname, String parentPath) {
    LOG.debug("In removeChild parentPath is {}", parentPath);
    CdmiObject oldParentObject = cdmiObjectDaoImpl.getCdmiObjectByPath(parentPath);
    LOG.debug("parent object {}", oldParentObject.toString());
    Domain parentDomain = (Domain) oldParentObject;
    JSONArray children = parentDomain.getChildren();
    for (int i = 0; i < children.length(); i++) {
      if (children.getString(i).equals(childname)) {
        children.remove(i);
      }
    }
    if (children.length() == 1 && children.getString(0).equals(childname))
      children = new JSONArray();
    parentDomain.setChildren(children);
    parentDomain.setChildrenrange("0-" + String.valueOf(children.length() - 1));
    cdmiObjectDaoImpl.updateCdmiObject(parentDomain);
    cdmiObjectDaoImpl.updateCdmiObject(parentDomain, parentPath);
  }

  private void addChild(Domain parentDomain, String childname, String parentPath) {
    JSONArray children = parentDomain.getChildren();
    if (children == null)
      children = new JSONArray();
    children.put(childname);
    parentDomain.setChildren(children);
    parentDomain.setChildrenrange("0-" + String.valueOf(children.length() - 1));
    cdmiObjectDaoImpl.updateCdmiObject(parentDomain);
    cdmiObjectDaoImpl.updateCdmiObject(parentDomain, parentPath);
  }

  public CdmiObject createRootdomain() {
    LOG.info("domain-Container wasn't created yet");

    String path = "/cdmi_domains";
    LOG.debug("create Rootdomain {}", path.trim());

    Domain domain = (Domain) cdmiObjectDaoImpl.createCdmiObject(new Domain());
    LOG.debug("domain is {}", domain.toJson().toString());
    if (domain != null) {
      Path relPath = Paths.get(path.trim());
      try {
        Path domainPath = Paths.get(baseDirectoryName.trim(), path.trim());
        Path directory = Files.createDirectory(domainPath);
        LOG.debug("created directory {}", directory.toString());

        domain.setObjectType(MediaTypes.ACCOUNT);
        domain.setObjectName(relPath.getFileName().toString());
        domain.setParentURI("/");
        domain.setParentID("");
        domain.setCapabilitiesURI(capabilitiesUri + "/domain");
        domain.setDomainURI(domainUri);

        cdmiObjectDaoImpl.updateCdmiObject(domain);

        Path file = Paths.get(baseDirectoryName, relPath.toString().trim());
        if (cdmiObjectDaoImpl.createCdmiObject(domain, file.toString()) == null)
          cdmiObjectDaoImpl.updateCdmiObject(domain, file.toString());

        return domain;
      } catch (FileAlreadyExistsException e) {
        LOG.warn("object alredy exists");
        cdmiObjectDaoImpl.deleteCdmiObject(domain.getObjectId());
      } catch (NoSuchFileException | NullPointerException e) {
        LOG.error("ERROR: {}", e.getMessage());
        cdmiObjectDaoImpl.deleteCdmiObject(domain.getObjectID());
        e.printStackTrace();
        throw new NotFoundException("resource was not found at the specified uri");
      } catch (BadRequestException e) {
        throw new BadRequestException("Bad Request");
      } catch (Exception e) {
        LOG.error("ERROR: {}", e.getMessage());
        e.printStackTrace();
        cdmiObjectDaoImpl.deleteCdmiObject(domain.getObjectID());
        try {
          Files.delete(Paths.get(baseDirectoryName.trim(), path.trim()));
        } catch (IOException e1) {
          LOG.error("ERROR: {}", e1.getMessage());
        }
      }
    }
    return null;
  }

  private CdmiObject copyWithChildrenNew(Domain domainRequest, String path) {
    Path source = Paths.get(baseDirectoryName.trim(), domainRequest.getCopy().trim());
    Path target = Paths.get(baseDirectoryName.trim(), path.trim());
    LOG.debug("CopyWithChildren source is {}", source);
    LOG.debug("CopyWithChildren target is {}", target);
    try {
      Domain domain = (Domain) cdmiObjectDaoImpl.createCdmiObject(new Domain());
      Domain oldDomain = (Domain) findByPath(domainRequest.getCopy().trim());
      FileUtils.copyDirectory(source.toFile(), target.toFile());
      CdmiObject parentObject =
          cdmiObjectDaoImpl.getCdmiObjectByPath(target.getParent().toString());
      LOG.debug("parent object {}", parentObject.toString());
      domain.setObjectName(target.getFileName().toString());
      domain.setParentURI(target.getParent().toString());
      domain.setParentID(parentObject.getObjectId());
      domain.setObjectType(MediaTypes.ACCOUNT);
      domain.setCapabilitiesURI(capabilitiesUri + "/domain");
      domain.setDomainURI(domainUri);
      domain.setMetadata(oldDomain.getMetadata());

      if (domainRequest.getMetadata() != null && !domainRequest.getMetadata().isEmpty())
        domain.setMetadata(domainRequest.getMetadata());

      cdmiObjectDaoImpl.updateCdmiObject(domain);

      if (cdmiObjectDaoImpl.createCdmiObject(domain, target.toString()) == null)
        cdmiObjectDaoImpl.updateCdmiObject(domain, target.toString());

      // addChild to parent
      addChild((Domain) parentObject, domain.getObjectName(), target.getParent().toString());
      // addChilds
      JSONArray children = oldDomain.getChildren();
      if (children != null) {
        if (domain.getChildren() == null) {
          domain.setChildren(new JSONArray());
        }
        for (int i = 0; i < children.length(); i++) {
          addChild(domain, children.getString(i), target.toString());
        }
      }
      try {
        editCopiedIdsRecursivly(domain, Paths.get(path));
      } catch (Exception e) {
        e.printStackTrace();
        FileUtils.deleteDirectory(target.toFile());
      }

      return domain;
    } catch (ClassCastException e) {
      throw new BadRequestException("Requested Resource is not a Doamin");
    } catch (FileAlreadyExistsException e) {
      throw new BadRequestException("Bad Request");
    } catch (IOException e) {
      throw new NotFoundException("Object not found");
    }
  }

  private void editCopiedIdsRecursivly(Domain domain, Path path) {
    if (domain.hasChildren()) {
      String[] children = Paths.get(baseDirectoryName, path.toString()).toFile().list();
      if (children != null) {
        for (int i = 0; i < children.length; i++) {
          String childname = children[i];
          Path newpath = Paths.get(path.toString(), childname);
          System.out.println(childname);
          System.out.println(newpath.toString());

          if (Paths.get(baseDirectoryName, newpath.toString()).toFile().isDirectory()) {
            System.out.println(newpath.toString());
            Domain child = (Domain) findByPath(newpath.toString());

            if (child != null) {

              Domain newDomain = (Domain) cdmiObjectDaoImpl.createCdmiObject(new Domain());
              CdmiObject parentObject = cdmiObjectDaoImpl
                  .getCdmiObjectByPath(Paths.get(baseDirectoryName, path.toString()).toString());
              newDomain.setObjectName(newpath.getFileName().toString());
              newDomain.setParentURI(newpath.getParent().toString());
              newDomain.setParentID(parentObject.getObjectId());
              newDomain.setObjectType(MediaTypes.ACCOUNT);
              newDomain.setCapabilitiesURI(capabilitiesUri + "/domain");
              newDomain.setDomainURI(domainUri);
              newDomain.setMetadata(child.getMetadata());

              cdmiObjectDaoImpl.updateCdmiObject(newDomain);

              if (cdmiObjectDaoImpl.createCdmiObject(newDomain,
                  Paths.get(baseDirectoryName, newpath.toString()).toString()) == null)
                cdmiObjectDaoImpl.updateCdmiObject(newDomain,
                    Paths.get(baseDirectoryName, newpath.toString()).toString());
              editCopiedIdsRecursivly(child, newpath);
            }
          }
        }
      }
    } else
      LOG.trace("PATH {} has no children", path);
  }

}
