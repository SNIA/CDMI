/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package org.snia.cdmiserver.dao.filesystem;

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

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * This is a prototype implementation of CdmiObject CRUD operations for a file system storage
 * back-end.
 * 
 * @author benjamin
 *
 */
public class CdmiObjectDaoImpl implements CdmiObjectDao {

  private static final Logger log = LoggerFactory.getLogger(CdmiObjectDaoImpl.class);

  private String objectIdPrefix;
  private String baseDirectory;
  private String objectIdDirectory;

  public String getObjectIdPrefix() {
    return objectIdPrefix;
  }

  public void setObjectIdPrefix(String objectIdPrefix) {
    this.objectIdPrefix = objectIdPrefix;
  }

  public String getBaseDirectory() {
    return baseDirectory;
  }

  public void setBaseDirectory(String baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  public String getObjectIdDirectory() {
    return objectIdDirectory;
  }

  public void setObjectIdDirectory(String objectIdDirectory) {
    this.objectIdDirectory = objectIdDirectory;
  }

  private Path getCdmiObjectFilePathByUrl(String path) {
    Path fileSystemRoot = Paths.get(baseDirectory.trim());
    Path fileSystemPath = Paths.get(baseDirectory.trim(), path.trim());

    Path returnPath = null;

    if (fileSystemPath.compareTo(fileSystemRoot) == 0) {
      returnPath = fileSystemRoot.resolve(objectIdPrefix);
    } else {
      String name = objectIdPrefix + fileSystemPath.getFileName();
      returnPath = fileSystemPath.getParent().resolve(name);
    }

    log.debug("return cdmi object file path {} for uri {}", returnPath.toString(), path);
    return returnPath;
  }

  private Path getObjectIdFilePath(String objectId) {
    return Paths.get(baseDirectory.trim(), objectIdDirectory, objectId);
  }

  /**
   * Creates a new CDMI object with the given object id in the parent directory of the given path.
   * The object's file name will be prefixed with the configured objectIdPrefix.
   * <p>
   * e.g. creating an object at baseDir/container1 will create a metadata file at baseDir with file
   * name .cdmi_container1.
   * </p>
   * 
   * @param object the object's id
   * @param path the CDMI URL path
   * @return the created {@link CdmiObject}
   */
  @Override
  public CdmiObject createCdmiObject(CdmiObject object, String path) {
    CdmiObject objectById = createCdmiObject(object);

    if (objectById != null) {
      try {

        Files.createLink(getCdmiObjectFilePathByUrl(path),
            getObjectIdFilePath(objectById.getObjectId()));

        // Files.write(getObjectPathForPath(path), object.toJson().toString().getBytes(),
        // StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);

        log.debug("create new objectId link {} to {}", getCdmiObjectFilePathByUrl(path).toString(),
            getObjectIdFilePath(objectById.getObjectId()).toString());
      } catch (FileAlreadyExistsException ex) {
        log.error("File already exists");
        log.debug("remove temporary object {}", objectById.getObjectId());
        CdmiObject removedObjectId = deleteCdmiObject(objectById.getObjectId());
        log.debug("removed temporary object {}", removedObjectId.toJson());
        log.debug("return existing object {}", getCdmiObjectByPath(path).toString());
        return getCdmiObjectByPath(path);
      } catch (Exception ex) {
        // ex.printStackTrace();
        log.error("{} {}", ex.getClass().getName(), ex.getMessage());
        deleteCdmiObject(objectById.getObjectId());
        return null;
      }
    }
    return objectById;
  }

  @Override
  public CdmiObject createCdmiObject(CdmiObject object) {
    try {

      Files.write(getObjectIdFilePath(object.getObjectId()), object.toJson().toString().getBytes(),
          StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);

      log.debug("create new objectId file {} {}", object.toString(), object.toJson());

    } catch (Exception ex) {
      // ex.printStackTrace();
      log.error("{} {}", ex.getClass().getName(), ex.getMessage());
      return null;
    }
    return object;
  }

  /**
   * Updates the given CDMI object.
   * 
   * @param updateObject the updated object
   * @param path the CDMI URL path
   * @return the updated {@link CdmiObject}
   */
  @Override
  public CdmiObject updateCdmiObject(CdmiObject updateObject, String path) {
    try {

      Files.write(getCdmiObjectFilePathByUrl(path), updateObject.toJson().toString().getBytes(),
          StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

      log.debug("update objectId file {} {}", updateObject.toString(), updateObject.toJson());

    } catch (Exception ex) {
      // ex.printStackTrace();
      log.error("{} {}", ex.getClass().getName(), ex.getMessage());
      return null;
    }
    return updateObject;
  }

  @Override
  public CdmiObject updateCdmiObject(CdmiObject object) {
    try {

      Files.write(getObjectIdFilePath(object.getObjectId()), object.toJson().toString().getBytes(),
          StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

      log.debug("update objectId file {} {}", object.toString(), object.toJson());

    } catch (Exception ex) {
      // ex.printStackTrace();
      log.error("{} {}", ex.getClass().getName(), ex.getMessage());
      return null;
    }
    return object;
  }

  /**
   * Deletes a CdmiObject by path.
   * 
   * @param path the CDMI URL path
   * @return the deleted {@link CdmiObject}
   */
  @Override
  public CdmiObject deleteCdmiObjectByPath(String path) {
    CdmiObject object = getCdmiObjectByPath(path);
    if (object != null) {
      try {

        boolean deleted = Files.deleteIfExists(getCdmiObjectFilePathByUrl(path));

        log.debug("delete objectId file {} success {}", path, deleted);

        deleteCdmiObject(object.getObjectId());

      } catch (Exception ex) {
        // ex.printStackTrace();
        log.error("{} {}", ex.getClass().getName(), ex.getMessage());
        return null;
      }
    }
    return object;
  }

  @Override
  public CdmiObject deleteCdmiObject(String objectId) {
    CdmiObject object = getCdmiObject(objectId);
    if (object != null) {
      try {

        boolean deleted = Files.deleteIfExists(getObjectIdFilePath(objectId));

        log.debug("delete objectId file {} success {}", object.toString(), deleted);

      } catch (Exception ex) {
        // ex.printStackTrace();
        log.error("{} {}", ex.getClass().getName(), ex.getMessage());
        return null;
      }
    }
    return object;
  }

  /**
   * Gets a CDMI object by path.
   * 
   * @param path the CDMI URL path
   * @return the {@link CdmiObject}
   */
  @Override
  public CdmiObject getCdmiObjectByPath(String path) {
    try {

      byte[] content = Files.readAllBytes(getCdmiObjectFilePathByUrl(path));
      JSONObject json = new JSONObject(new String(content));

      String objectType = json.optString("objectType");
      if (objectType != null) {
        if (objectType.equals(MediaTypes.CONTAINER)) {
          return Container.fromJson(json);
        } else if (objectType.equals(MediaTypes.DATA_OBJECT)) {
          return DataObject.fromJson(json);
        } else if (objectType.equals(MediaTypes.ACCOUNT)) {
          return new Domain(json);
        } else if (objectType.equals(MediaTypes.CAPABILITY)) {
          return Capability.fromJson(json);
        } else {
          return CdmiObject.fromJson(json);
        }
      }
    } catch (Exception ex) {
      // ex.printStackTrace();
      log.error("{} {}", ex.getClass().getName(), ex.getMessage());
    }
    return null;
  }

  @Override
  public CdmiObject getCdmiObject(String objectId) {
    try {
      log.debug("Get object {}", getObjectIdFilePath(objectId));
      byte[] content = Files.readAllBytes(getObjectIdFilePath(objectId));
      JSONObject json = new JSONObject(new String(content));

      String objectType = json.optString("objectType");
      if (objectType != null) {
        if (objectType.equals(MediaTypes.CONTAINER)) {
          return Container.fromJson(json);
        } else if (objectType.equals(MediaTypes.DATA_OBJECT)) {
          return DataObject.fromJson(json);
        } else if (objectType.equals(MediaTypes.ACCOUNT)) {
          return new Domain(json);
        } else if (objectType.equals(MediaTypes.CAPABILITY)) {
          return Capability.fromJson(json);
        } else {
          return CdmiObject.fromJson(json);
        }
      }
    } catch (Exception ex) {
      // ex.printStackTrace();
      log.error("{} {}", ex.getClass().getName(), ex.getMessage());
    }
    return null;
  }
}
