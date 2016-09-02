/*
 * Original work Copyright (c) 2010, Sun Microsystems, Inc. Copyright (c) 2010, The Storage
 * Networking Industry Association.
 *
 * Modified work Copyright (c) 2016, Karlsruhe Institute of Technology (KIT)
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of The Storage Networking Industry Association (SNIA) nor the names of its
 * contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CdmiObjectDao;
import org.snia.cdmiserver.dao.ContainerDao;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.Container;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <p>
 * Concrete implementation of {@link ContainerDao} using the local filesystem as the backing store.
 * </p>
 */
public class ContainerDaoImpl implements ContainerDao {

  private static final Logger log = LoggerFactory.getLogger(ContainerDaoImpl.class);

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
  public Container createByPath(String path, Container containerRequest) {
    try {
      final Path containerPath = Paths.get(baseDirectoryName.trim(), path.trim());
      // create the container directory
      Files.createDirectory(containerPath);
      log.debug("create container {} {}", path.trim(), containerRequest.toString());
      log.debug("create directory {}", containerPath.toString());
    } catch (FileAlreadyExistsException ex) {
      log.error("File already exists {}", ex.getMessage());
    } catch (Exception ex) {
      //ex.printStackTrace();
      log.error(ex.getMessage());
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

    // fix: try recursively for non-existing parents
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

    return container;
  }

  @Override
  public Container deleteByPath(String path) {
    Container container = null;
    try {
      log.debug("delete container {}", path.trim());

      container = (Container) cdmiObjectDao.getCdmiObjectByPath(path.trim());

      if (container != null) {
        final Path containerPath = Paths.get(baseDirectoryName.trim(), path.trim());

        log.debug("delete directory {}", containerPath.toString());
        Files.delete(containerPath);

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
    return Files.isDirectory(Paths.get(baseDirectoryName.trim(), path.trim()));
  }
}
