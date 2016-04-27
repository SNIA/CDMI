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

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.ContainerDao;
import org.snia.cdmiserver.exception.BadRequestException;
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
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

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

  @Value("${cdmi.data.rootObjectId}")
  private String rootObjectId;

  @Autowired
  private CdmiObjectDaoImpl cdmiObjectDaoImpl;

  @Autowired
  private DomainDaoImpl domainDaoImpl;

  public CdmiObject createRootContainer() {
    LOG.debug("create RootContainer {}", baseDirectoryName.trim());

    Path containerPath = Paths.get(baseDirectoryName.trim());
    try {

      Path directory = Files.createDirectories(containerPath);
      if (directory != null) {
        LOG.debug("created directory {}", directory.toString());

        if (!Files.exists(Paths.get(baseDirectoryName, "/cdmi_objectid"))) {
          LOG.info("objectid-Container wasn't created yet");
          createRootIdContainer();
        }
        Container container = new Container();
        if (container != null) {
          container.setObjectType(MediaTypes.CONTAINER);
          container.setObjectName("");
          container.setParentURI("");
          container.setParentID("");
          container.setCapabilitiesURI(capabilitiesUri + "/container/default");
          container.setDomainURI(domainUri);
          container.setCompletionStatus("Complete");
          container.setObjectID(rootObjectId);

          ArrayList<Object> children = new ArrayList<Object>();
          children.add("cdmi_objectid");
          children.add("cdmi_domains");
          container.setChildren(children);
          container.setChildrenrange("0-" + String.valueOf(children.size() - 1));

          cdmiObjectDaoImpl.createCdmiObject(container);

          if (cdmiObjectDaoImpl.createCdmiObject(container, directory.toString()) == null)
            cdmiObjectDaoImpl.updateCdmiObject(container, directory.toString());

          return container;
        }
      }
    } catch (Exception e) {
      LOG.error("ERROR: {}", e.getMessage());
      try {
        Files.delete(containerPath);
      } catch (IOException e1) {
        LOG.error("ERROR: {}", e1.getMessage());
        // e1.printStackTrace();
      }
    }
    return null;

  }

  public Path createRootIdContainer() {

    try {
      Path idDirectory = Files.createDirectory(Paths.get(baseDirectoryName, "/cdmi_objectid"));
      LOG.debug("created directory {}", idDirectory.toString());
      return idDirectory;
    } catch (IOException e) {
      LOG.error("ERROR {}", e.getMessage());
      return null;
    }


  }

  @Override
  public CdmiObject createByPath(String path, Container containerRequest) {
    LOG.debug("create container {} {}", path.trim(), containerRequest.toString());

    if (containerRequest.getCopy() != null) {
      if (findByPath(path) == null)
        return copyCreate(containerRequest, path);
      else
        return copyUpdate(containerRequest, path);
    } else if (containerRequest.getMove() != null) {
      return move(containerRequest, containerRequest.getMove(), path);
    } else {

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
          container.setCapabilitiesURI(capabilitiesUri + "/container/default");
          if (containerRequest.getDomainURI() != null) {
            String domain = containerRequest.getDomainURI();
            if (domainDaoImpl.findByPath(domain) != null)
              container.setDomainURI(domain);
            else
              throw new BadRequestException("The specified domainURI doesn't exist");
          } else
            container.setDomainURI(((Container) parentObject).getDomainURI());
          container.setMetadata(containerRequest.getMetadata());
          container.setCompletionStatus("Complete");

          cdmiObjectDaoImpl.updateCdmiObject(container);

          if (cdmiObjectDaoImpl.createCdmiObject(container, directory.toString()) == null)
            cdmiObjectDaoImpl.updateCdmiObject(container, directory.toString());

          addChild(containerPath.getFileName().toString(),
              containerPath.getParent().toString());

          return container;
        } catch (BadRequestException e) {
          throw new BadRequestException(e.getMessage());
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
          e.printStackTrace();
          try {
            Files.delete(containerPath);
          } catch (IOException e1) {
            LOG.error("ERROR: {}", e1.getMessage());
            // e1.printStackTrace();
          }
          cdmiObjectDaoImpl.deleteCdmiObject(container.getObjectID());
        }
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

        removeChild(containerPath.getFileName().toString(), containerPath.getParent().toString());

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

  private void removeChild(String childname, String parentPath) {
    LOG.debug("In removeChild parentPath is {}", parentPath);
    CdmiObject oldParentObject = cdmiObjectDaoImpl.getCdmiObjectByPath(parentPath);
    LOG.debug("parent object {}", oldParentObject.toString());
    Container parentContainer = (Container) oldParentObject;
    List<Object> children = parentContainer.getChildren();
    for (int i = 0; i < children.size(); i++) {
      if (children.get(i).equals(childname)) {
        children.remove(i);
      }
    }
    // if (children.size() == 1 && children.get(0).equals(childname))
    // children = new ArrayList<Object>();
    parentContainer.setChildren(children);
    parentContainer.setChildrenrange("0-" + String.valueOf(children.size() - 1));
    cdmiObjectDaoImpl.updateCdmiObject(parentContainer);
    cdmiObjectDaoImpl.updateCdmiObject(parentContainer, parentPath);
  }



  private void addChild(String childname, String parentPath) {
    Container parentContainer = (Container) cdmiObjectDaoImpl.getCdmiObjectByPath(parentPath);
    List<Object> children = parentContainer.getChildren();
    if (children == null)
      children = new ArrayList<Object>();
    children.add(childname);
    parentContainer.setChildren(children);
    parentContainer.setChildrenrange("0-" + String.valueOf(children.size() - 1));
    cdmiObjectDaoImpl.updateCdmiObject(parentContainer);
    cdmiObjectDaoImpl.updateCdmiObject(parentContainer, parentPath);
  }


  /**
   * copies a Container with all subcontainers to a new created container at the specified path
   * 
   * @param containerRequest the {@link Container}
   * @param path the {@link String}
   * @return a {@link CdmiObject}
   */
  private CdmiObject copyCreate(Container containerRequest, String path) {
    Path source = Paths.get(baseDirectoryName.trim(), containerRequest.getCopy().trim());
    Path target = Paths.get(baseDirectoryName.trim(), path.trim());
    LOG.debug("In copyCreate source is {}", source);
    LOG.debug("In copyCreate target is {}", target);
    try {
      Container container = (Container) cdmiObjectDaoImpl.createCdmiObject(new Container());
      Container oldContainer = (Container) findByPath(containerRequest.getCopy().trim());
      FileUtils.copyDirectory(source.toFile(), target.toFile());
      Container parentObject =
          (Container) cdmiObjectDaoImpl.getCdmiObjectByPath(target.getParent().toString());
      LOG.debug("parent object {}", parentObject.toString());
      container.setObjectName(target.getFileName().toString());
      container.setParentURI(Paths.get(path).getParent().toString());
      container.setParentID(parentObject.getObjectId());
      container.setObjectType(MediaTypes.CONTAINER);
      container.setCapabilitiesURI(oldContainer.getCapabilitiesURI());
      container.setDomainURI(parentObject.getDomainURI());
      container.setMetadata(oldContainer.getMetadata());

      if (containerRequest.getMetadata() != null && !containerRequest.getMetadata().isEmpty())
        container.setMetadata(containerRequest.getMetadata());
      if (containerRequest.getDomainURI() != null && !containerRequest.getDomainURI().isEmpty())
        container.setDomainURI(containerRequest.getDomainURI());

      cdmiObjectDaoImpl.updateCdmiObject(container);
      if (cdmiObjectDaoImpl.createCdmiObject(container, target.toString()) == null)
        cdmiObjectDaoImpl.updateCdmiObject(container, target.toString());

      // addChild to parent
      addChild(container.getObjectName(), target.getParent().toString());
      // addChilds
      List<Object> children = oldContainer.getChildren();
      if (children != null) {
        if (container.getChildren() == null) {
          container.setChildren(new ArrayList<Object>());
        }
        for (int i = 0; i < children.size(); i++) {
          addChild((String) children.get(i), target.toString());
        }
      }
      try {
        editCopiedIdsRecursivly(container, Paths.get(path));
      } catch (Exception e) {
        e.printStackTrace();
        FileUtils.deleteDirectory(target.toFile());
      }

      return container;
    } catch (ClassCastException e) {
      e.printStackTrace();
      throw new BadRequestException("Requested Resource is not a Container");
    } catch (FileAlreadyExistsException e) {
      throw new BadRequestException("Bad Request");
    } catch (IOException e) {
      throw new NotFoundException("Object not found");
    }
  }


  /**
   * updates the copied Containers: updates the objectId and parentObjectId in the .cdmi_-file and
   * creates the objectId-file
   * 
   * @param container the {@link Container}
   * @param path the {@link Path}
   */
  private void editCopiedIdsRecursivly(Container container, Path path) {
    if (container.hasChildren()) {
      String[] children = Paths.get(baseDirectoryName, path.toString()).toFile().list();
      if (children != null) {
        for (int i = 0; i < children.length; i++) {
          String childname = children[i];
          Path newpath = Paths.get(path.toString(), childname);

          if (Paths.get(baseDirectoryName, newpath.toString()).toFile().isDirectory()) {
            Container child = (Container) findByPath(newpath.toString());
            if (child != null) {
              Container newContainer =
                  (Container) cdmiObjectDaoImpl.createCdmiObject(new Container());
              Container parentObject = (Container) cdmiObjectDaoImpl
                  .getCdmiObjectByPath(Paths.get(baseDirectoryName, path.toString()).toString());
              newContainer.setObjectName(newpath.getFileName().toString());
              newContainer.setParentURI(newpath.getParent().toString());
              newContainer.setParentID(parentObject.getObjectId());
              newContainer.setObjectType(MediaTypes.CONTAINER);
              newContainer.setCapabilitiesURI(child.getCapabilitiesURI());
              newContainer.setDomainURI(parentObject.getDomainURI());
              newContainer.setMetadata(child.getMetadata());

              cdmiObjectDaoImpl.updateCdmiObject(newContainer);

              if (cdmiObjectDaoImpl.createCdmiObject(newContainer,
                  Paths.get(baseDirectoryName, newpath.toString()).toString()) == null)
                cdmiObjectDaoImpl.updateCdmiObject(newContainer,
                    Paths.get(baseDirectoryName, newpath.toString()).toString());
              editCopiedIdsRecursivly(child, newpath);
            }
          }
        }
      }
    } else
      LOG.trace("PATH {} has no children", path);
  }

  private CdmiObject copyUpdate(Container containerRequest, String path) {
    // TODO
    return null;
  }

  private Container move(Container containerRequest, String moveFrom, String moveTo) {
    Path source = Paths.get(baseDirectoryName.trim(), moveFrom.trim());
    Path target = Paths.get(baseDirectoryName.trim(), moveTo.trim());
    LOG.debug("Move source is {}", source);
    LOG.debug("Move target is {}", target);
    try {
      Container container = (Container) findByPath(moveFrom);
      if (container != null) {
        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        Container parentObject =
            (Container) cdmiObjectDaoImpl.getCdmiObjectByPath(target.getParent().toString());
        LOG.debug("parent object {}", parentObject.toString());
        container.setObjectName(target.getFileName().toString());
        container.setParentURI(Paths.get(moveTo).getParent().toString());
        container.setParentID(parentObject.getObjectId());
        container.setDomainURI(parentObject.getDomainURI());

        if (containerRequest.getMetadata() != null && !containerRequest.getMetadata().isEmpty())
          container.setMetadata(containerRequest.getMetadata());
        if (containerRequest.getDomainURI() != null && !containerRequest.getDomainURI().isEmpty())
          container.setDomainURI(containerRequest.getDomainURI());

        cdmiObjectDaoImpl.updateCdmiObject(container);

        if (cdmiObjectDaoImpl.createCdmiObject(container, target.toString()) == null)
          cdmiObjectDaoImpl.updateCdmiObject(container, target.toString());

        Container newContainer = (Container) findByPath(moveTo);
        if (newContainer != null) {
          cdmiObjectDaoImpl.deleteCdmiObjectByPath(source.toString());

          String parentPath = source.getParent().toString();

          removeChild(source.getFileName().toString(), parentPath);
          addChild(container.getObjectName(),
              target.getParent().toString());

          return newContainer;
        } else {
          throw new NotFoundException("Not found");
        }
      }
    } catch (FileAlreadyExistsException e) {
      throw new ConflictException("Container already exists");
    } catch (IOException e) {
      LOG.error("ERROR {}", e.getMessage());
      throw new NotFoundException("container not found");
    }
    return null;
  }
}
