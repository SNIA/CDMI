/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package org.snia.cdmiserver.dao.filesystem;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CdmiObjectDao;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.Container;
import org.snia.cdmiserver.model.DataObject;
import org.snia.cdmiserver.model.Domain;
import org.snia.cdmiserver.util.MediaTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Component
public class CdmiObjectDaoImpl implements CdmiObjectDao {

  private static final Logger log = LoggerFactory.getLogger(CdmiObjectDaoImpl.class);

  @Value("${cdmi.data.rootObjectId}")
  private String rootObjectId;

  @Value("${cdmi.data.objectPrefix}")
  private String objectIdPrefix;

  @Value("${cdmi.data.objectidDirectory}")
  private String objectIdDirectoryName;

  @Value("${cdmi.data.baseDirectory}")
  private String baseDirectoryName;

  /**
   * Creates a new CDMI object with the given object id at the given path.
   * 
   * @param objectId the object's id
   * @param path the file system path
   * @return the created {@link CdmiObject}
   */
  public CdmiObject createCdmiObject(CdmiObject objectId, String path) {
    try {
      Path sanitizedPath = Paths.get(path.trim());
      Path newPath;
      try {
        newPath = Paths.get(sanitizedPath.getParent().toString(),
            objectIdPrefix + sanitizedPath.getFileName().toString());
      } catch (NullPointerException e) {
        newPath = Paths.get(objectIdPrefix + sanitizedPath.getFileName().toString());
      }
      Files.write(newPath, objectId.toJson().toString().getBytes(), StandardOpenOption.WRITE,
          StandardOpenOption.CREATE_NEW);

      log.debug("create new objectId file {} {}", objectId.toString(), objectId.toJson());
    } catch (JSONException e) {
      log.error("could not format attribute map to JSON");
      return null;
    } catch (IOException e) {
      log.error("ERROR: {}", e.getMessage());
      // e.printStackTrace();
      return null;
    }
    return objectId;
  }

  @Override
  public CdmiObject createCdmiObject(CdmiObject objectId) {
    try {
      Path path =
          Paths.get(baseDirectoryName.trim(), objectIdDirectoryName.trim(), objectId.getObjectId());
      if (!Files.exists(Paths.get(baseDirectoryName.trim(), objectIdDirectoryName.trim()))) {
        Files.createDirectory(Paths.get(baseDirectoryName.trim(), objectIdDirectoryName.trim()));
      }
      log.debug("path is {}", path);
      Files.write(path, objectId.toJson().toString().getBytes(), StandardOpenOption.WRITE,
          StandardOpenOption.CREATE_NEW);

      log.debug("create new objectId file {} {}", objectId.toString(), objectId.toJson());
    } catch (JSONException e) {
      log.error("could not format attribute map to JSON");
      return null;
    } catch (IOException e) {
      log.error("ERROR: {}", e.getMessage());
      // e.printStackTrace();
      return null;
    }
    return objectId;
  }

  /**
   * Updates the given CDMI object.
   * 
   * @param objectId the object's id
   * @param path the object's file system path
   * @return the updated {@link CdmiObject}
   */
  public CdmiObject updateCdmiObject(CdmiObject objectId, String path) {
    try {
      Path sanitizedPath = Paths.get(path.trim());
      Path newPath;
      try {
        newPath = Paths.get(sanitizedPath.getParent().toString(),
            objectIdPrefix + sanitizedPath.getFileName().toString());
      } catch (NullPointerException e) {
        newPath = Paths.get(objectIdPrefix + sanitizedPath.getFileName().toString());
      }
      Files.write(newPath, objectId.toJson().toString().getBytes(), StandardOpenOption.WRITE,
          StandardOpenOption.TRUNCATE_EXISTING);

      log.debug("update objectId file {} {}", objectId.toString(), objectId.toJson());
    } catch (JSONException e) {
      log.error("could not format attribute map to JSON");
      return null;
    } catch (IOException e) {
      log.error("ERROR: {}", e.getMessage());
      // e.printStackTrace();
      return null;
    }
    return objectId;
  }

  @Override
  public CdmiObject updateCdmiObject(CdmiObject objectId) {
    try {
      Path path =
          Paths.get(baseDirectoryName.trim(), objectIdDirectoryName.trim(), objectId.getObjectId());
      Files.write(path, objectId.toJson().toString().getBytes(), StandardOpenOption.WRITE,
          StandardOpenOption.TRUNCATE_EXISTING);

      log.debug("update objectId file {} {}", objectId.toString(), objectId.toJson());
    } catch (JSONException e) {
      log.error("could not format attribute map to JSON");
      return null;
    } catch (IOException e) {
      log.error("ERROR: {}", e.getMessage());
      return null;
    }
    return objectId;
  }

  /**
   * Deletes a CdmiObject by path.
   * 
   * @param path the file system path to the CDMI object
   * @return the deleted {@link CdmiObject}
   */
  public CdmiObject deleteCdmiObjectByPath(String path) {
    CdmiObject object = getCdmiObjectByPath(path);
    if (object != null) {
      Path sanitizedPath = Paths.get(path.trim());
      Path newPath;
      try {
        newPath = Paths.get(sanitizedPath.getParent().toString(),
            objectIdPrefix + sanitizedPath.getFileName().toString());
      } catch (NullPointerException e) {
        newPath = Paths.get(objectIdPrefix + sanitizedPath.getFileName().toString());
      }
      try {
        boolean deleted = Files.deleteIfExists(newPath);
        log.debug("delete objectId file {} success {}", object.toString(), deleted);
      } catch (IOException e) {
        log.error("ERROR: {}", e.getMessage());
      }
    }
    return object;
  }

  @Override
  public CdmiObject deleteCdmiObject(String objectId) {
    CdmiObject object = getCdmiObject(objectId);
    if (object != null) {
      Path path = Paths.get(baseDirectoryName.trim(), objectIdDirectoryName.trim(), objectId);
      try {
        boolean deleted = Files.deleteIfExists(path);
        log.debug("delete objectId file {} success {}", object.toString(), deleted);
      } catch (IOException e) {
        log.error("ERROR: {}", e.getMessage());
      }
    }
    return object;
  }

  /**
   * Gets a CDMI object by path.
   * 
   * @param path the object's file system path
   * @return the {@link CdmiObject}
   */
  public CdmiObject getCdmiObjectByPath(String path) {
    CdmiObject object = null;
    Path sanitizedPath = Paths.get(path.trim().replaceAll("/$", ""));
    Path newPath;
    try {
      newPath = Paths.get(sanitizedPath.getParent().toString(),
          objectIdPrefix + sanitizedPath.getFileName().toString());
    } catch (NullPointerException e) {
      newPath = Paths.get(objectIdPrefix + sanitizedPath.getFileName().toString());
    }
    try {
      log.debug("path is {}", newPath);
      byte[] content = Files.readAllBytes(newPath);
      JSONObject json = new JSONObject(new String(content));

      String objectType = json.optString("objectType");
      if (objectType != null) {
        if (objectType.equals(MediaTypes.CONTAINER)) {
          return new Container(json);
        } else if (objectType.equals(MediaTypes.DATA_OBJECT)) {
          return new DataObject(json);
        } else if (objectType.equals(MediaTypes.ACCOUNT)) {
          return new Domain(json);
        }
      }
      object = new CdmiObject(json);
      log.debug("get objectId from file {}", object.toString());
    } catch (JSONException e) {
      log.error("could not format attribute map to JSON");
    } catch (IOException e) {
      log.error("ERROR: {}", e.getMessage());
      // e.printStackTrace();
    }
    return object;
  }

  @Override
  public CdmiObject getCdmiObject(String objectId) {
    CdmiObject object = null;
    Path path = Paths.get(baseDirectoryName.trim(), objectIdDirectoryName.trim(), objectId);
    try {
      byte[] content = Files.readAllBytes(path);
      JSONObject json = new JSONObject(new String(content));

      String objectType = json.optString("objectType");
      if (objectType != null) {
        if (objectType.equals(MediaTypes.CONTAINER)) {
          return new Container(json);
        } else if (objectType.equals(MediaTypes.DATA_OBJECT)) {
          return new DataObject(json);
        }
      }
      object = new CdmiObject(json);
      log.debug("get objectId from file {}", object.toString());
    } catch (JSONException e) {
      log.error("could not format attribute map to JSON");
    } catch (IOException e) {
      log.error("ERROR: {}", e.getMessage());
    }
    return object;
  }

}
