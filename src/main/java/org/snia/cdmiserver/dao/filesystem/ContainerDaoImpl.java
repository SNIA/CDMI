/*
 * Copyright (c) 2010, Sun Microsystems, Inc. Copyright (c) 2010, The Storage Networking Industry
 * Association.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.ContainerDao;
import org.snia.cdmiserver.exception.ConflictException;
import org.snia.cdmiserver.exception.NotFoundException;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.Container;
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

/**
 * <p>
 * Concrete implementation of {@link ContainerDao} using the local filesystem as the backing store.
 * </p>
 */
@Component
public class ContainerDaoImpl implements ContainerDao {

  private static final Logger LOG = LoggerFactory.getLogger(ContainerDaoImpl.class);

  @Value("${cdmi.capabilitiesUri}")
  private String capabilitiesUri;

  @Value("${cdmi.domainUri}")
  private String domainUri;

  @Value("${cdmi.data.baseDirectory}")
  private String baseDirectoryName;

  @Autowired
  private CdmiObjectDaoImpl cdmiObjectDaoImpl;

  @Override
  public CdmiObject createByPath(String path, Container containerRequest) {
    LOG.debug("create container {} {}", path.trim(), containerRequest.toString());

    Container container = (Container) cdmiObjectDaoImpl.createCdmiObject(new Container());

    if (container != null) {
      Path containerPath = Paths.get(baseDirectoryName.trim(), path.trim());
      Path relPath = Paths.get(path.trim());
      try {
        Path directory = Files.createDirectory(containerPath);
        LOG.debug("created directory {}", directory.toString());

        CdmiObject parentObject =
            cdmiObjectDaoImpl.getCdmiObjectByPath(directory.getParent().toString());
        LOG.debug("parent object {}", parentObject.toString());

        container.setObjectType(MediaTypes.CONTAINER);
        container.setObjectName(directory.getFileName().toString());
        container.setParentURI(relPath.getParent().toString());
        container.setParentID(parentObject.getObjectId());
        container.setCapabilitiesURI(capabilitiesUri);
        container.setDomainURI(domainUri);
        container.setMetadata(containerRequest.getMetadata());

        // optional
        // container.setPercentComplete(percentComplete);
        // container.setExports(exports);
        // container.setSnapshots(snapshots);
        // container.setChildrenrange(childrenrange);
        // container.setChildren(children);

        container.setCompletionStatus("Complete");

        cdmiObjectDaoImpl.updateCdmiObject(container);

        if (cdmiObjectDaoImpl.createCdmiObject(container, directory.toString()) == null)
          cdmiObjectDaoImpl.updateCdmiObject(container, directory.toString());

        return container;
      } catch (FileAlreadyExistsException e) {
        LOG.warn("object alredy exists");
        cdmiObjectDaoImpl.deleteCdmiObject(container.getObjectID());
        throw new ConflictException("object already exists");
      } catch (IOException e) {
        LOG.error("ERROR: {}", e.getMessage());
        cdmiObjectDaoImpl.deleteCdmiObject(container.getObjectID());
        throw new NotFoundException("resource was not found at the specified uri");
      } catch (Exception e) {
        LOG.error("ERROR: {}", e.getMessage());
        try {
          Files.delete(containerPath);
        } catch (IOException e1) {
          LOG.error("ERROR: {}", e1.getMessage());
          // e1.printStackTrace();
        }
        cdmiObjectDaoImpl.deleteCdmiObject(container.getObjectID());
      }
    }
    return null;
  }

  @Override
  public void deleteByPath(String path) {
    LOG.debug("delete container {}", path.trim());
    Path containerPath = Paths.get(baseDirectoryName.trim(), path.trim());

    Container container =
        (Container) cdmiObjectDaoImpl.getCdmiObjectByPath(containerPath.toString());

    if (container != null) {
      try {
        LOG.debug("delete directory {}", containerPath.toString());
        Files.delete(containerPath);

        cdmiObjectDaoImpl.deleteCdmiObject(container.getObjectId());
        cdmiObjectDaoImpl.deleteCdmiObjectByPath(containerPath.toString());

      } catch (NoSuchFileException e) {
        LOG.warn("container not found");
        throw new NotFoundException("container not found");
      } catch (DirectoryNotEmptyException e) {
        LOG.warn("container not empty");
      } catch (IOException e) {
        LOG.error("ERROR: {}", e.getMessage());
      } catch (Exception e) {
        LOG.error("ERROR: {}", e.getMessage());
      }
    }
  }

  @Override
  public Container findByObjectId(String objectId) {
    return (Container) cdmiObjectDaoImpl.getCdmiObject(objectId);
  }

  @Override
  public Container findByPath(String path) {
    Path containerPath = Paths.get(baseDirectoryName.trim(), path.trim());
    return (Container) cdmiObjectDaoImpl.getCdmiObjectByPath(containerPath.toString());
  }

  @Override
  public boolean isContainer(String path) {
    Path containerPath = Paths.get(baseDirectoryName.trim(), path.trim());
    return Files.isDirectory(containerPath);
  }
}
