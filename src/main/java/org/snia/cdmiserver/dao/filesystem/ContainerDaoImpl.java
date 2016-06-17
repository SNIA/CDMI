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

  private static final Logger LOG = LoggerFactory.getLogger(ContainerDaoImpl.class);

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
    if (path == null) {
      return null;
    }

    final Path containerPath = Paths.get(baseDirectoryName.trim(), path.trim());

    try {
      // create the container directory
      Files.createDirectory(containerPath);
      LOG.debug("create container {} {}", path.trim(), containerRequest.toString());
    } catch (FileAlreadyExistsException ex) {
      LOG.error("File already exists {}", ex.getMessage());
      return null;
    } catch (Exception ex) {
      LOG.error(ex.getMessage());
      return null;
    }

    // generate the container meta-data
    String containerName = containerPath.getFileName().toString();
    String parentUri = Paths.get(path.trim()).getParent() == null ? "/"
        : Paths.get(path.trim()).getParent().toString();
    String parentPath = containerPath.getParent() == null ? baseDirectoryName
        : containerPath.getParent().toString();

    Container parentContainer =
        (Container) cdmiObjectDao.getCdmiObjectByPath(parentPath.toString());

    Container container = new Container(containerName, parentUri, parentContainer.getObjectId());

    if (parentContainer.getChildren() == null) {
      parentContainer.setChildren(new JSONArray());
    }
    parentContainer.getChildren().put(container.getObjectName());
    String childrenRange = CdmiObject.getChildrenRange(parentContainer.getChildren());
    parentContainer.setChildrenrange(childrenRange);

    container.setCompletionStatus("Complete");
    container.setMetadata(containerRequest.getMetadata());

    cdmiObjectDao.createCdmiObject(container, containerPath.toString());
    cdmiObjectDao.updateCdmiObject(parentContainer);

    return container;
  }

  @Override
  public Container deleteByPath(String path) {
    if (path == null) {
      return null;
    }
    LOG.debug("delete container {}", path.trim());

    final Path containerPath = Paths.get(baseDirectoryName.trim(), path.trim());
    Container container = (Container) cdmiObjectDao.getCdmiObjectByPath(containerPath.toString());

    if (container != null) {
      try {
        LOG.debug("delete directory {}", containerPath.toString());
        Files.delete(containerPath);

        cdmiObjectDao.deleteCdmiObject(container.getObjectId());
        cdmiObjectDao.deleteCdmiObjectByPath(containerPath.toString());

        // removeChild(containerPath.getFileName().toString(),
        // containerPath.getParent().toString());

      } catch (Exception ex) {
        LOG.error("ERROR: {}", ex.getMessage());
      }
    }
    return container;
  }

  @Override
  public Container findByObjectId(String objectId) {
    return (Container) cdmiObjectDao.getCdmiObject(objectId);
  }

  @Override
  public Container findByPath(String path) {
    return (Container) cdmiObjectDao
        .getCdmiObjectByPath(Paths.get(baseDirectoryName.trim(), path.trim()).toString());
  }

  @Override
  public boolean isContainer(String path) {
    return Files.isDirectory(Paths.get(baseDirectoryName.trim(), path.trim()));
  }
}
