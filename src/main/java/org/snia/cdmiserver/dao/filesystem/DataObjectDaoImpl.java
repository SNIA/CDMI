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

package org.snia.cdmiserver.dao.filesystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.DataObjectDao;
import org.snia.cdmiserver.exception.BadRequestException;
import org.snia.cdmiserver.exception.ConflictException;
import org.snia.cdmiserver.exception.NotFoundException;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.Container;
import org.snia.cdmiserver.model.DataObject;
import org.snia.cdmiserver.util.MediaTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <p>
 * Concrete implementation of {@link DataObjectDao} using the local filesystem as the backing store.
 * </p>
 */
@Component
public class DataObjectDaoImpl implements DataObjectDao {

  private static final Logger LOG = LoggerFactory.getLogger(DataObjectDaoImpl.class);

  // -------------------------------------------------------------- Properties
  @Value("${cdmi.capabilitiesUri}")
  private String capabilitiesUri;

  @Value("${cdmi.domainUri}")
  private String domainUri;

  @Value("${cdmi.data.baseDirectory}")
  private String baseDirectoryName;

  @Autowired
  private CdmiObjectDaoImpl cdmiObjectDaoImpl;

  @Autowired
  private DomainDaoImpl domainDaoImpl;

  @Override
  public DataObject createByPath(String path, DataObject dataObjectRequest) {
    LOG.debug("create data object {} {}", path.trim(), dataObjectRequest.toString());

    if (dataObjectRequest.getCopy() != null) {
      return (DataObject) copy(dataObjectRequest, path);
    } else if (dataObjectRequest.getMove() != null) {
      return move(dataObjectRequest, dataObjectRequest.getMove(), path);
    } else {
      if (path.contains("?"))
        path = path.split(Pattern.quote("?"))[0];
      DataObject dataObject = (DataObject) cdmiObjectDaoImpl.createCdmiObject(new DataObject());

      if (dataObject != null) {
        Path objectPath = Paths.get(baseDirectoryName.trim(), path.trim());
        Path relPath = Paths.get(path.trim());
        try {
          Path file;
          if (dataObjectRequest.getCopy() != null) {
            String copyFrom = dataObjectRequest.getCopy();
            DataObject copiedObject = findByPath(copyFrom);
            dataObject = createByPath(path, copiedObject);
          } else {
            if (dataObjectRequest.getValue() == null)
              file = Files.createFile(objectPath);
            else
              file = Files.write(objectPath, dataObjectRequest.getValue().getBytes());

            LOG.debug("created file {}", file.toString());

            CdmiObject parentObject =
                cdmiObjectDaoImpl.getCdmiObjectByPath(file.getParent().toString());
            LOG.debug("parent object {}", parentObject.toString());

            dataObject.setObjectType(MediaTypes.DATA_OBJECT);
            dataObject.setObjectName(file.getFileName().toString());
            dataObject.setParentURI(relPath.getParent().toString());
            dataObject.setParentID(parentObject.getObjectId());
            dataObject.setCapabilitiesURI(capabilitiesUri + "/dataobject/default");
            if (dataObjectRequest.getDomainURI() != null) {
              String domain = dataObjectRequest.getDomainURI();
              if (domainDaoImpl.findByPath(domain) != null)
                dataObject.setDomainURI(domain);
              else
                throw new BadRequestException("The specified domainURI doesn't exist");
            } else
              dataObject.setDomainURI(((Container) parentObject).getDomainURI());
            dataObject.setMetadata(dataObjectRequest.getMetadata());

            // optional
            // dataObject.setPercentComplete(percentComplete);
            // dataObject.setMimetype(mimetype);

            dataObject.setCompletionStatus("Complete");

            cdmiObjectDaoImpl.updateCdmiObject(dataObject);

            if (cdmiObjectDaoImpl.createCdmiObject(dataObject, file.toString()) == null)
              cdmiObjectDaoImpl.updateCdmiObject(dataObject, file.toString());

            addChild(file.getFileName().toString(), objectPath.getParent().toString());
          }
          return dataObject;
        } catch (BadRequestException e) {
          throw new BadRequestException(e.getMessage());
        } catch (FileAlreadyExistsException e) {
          LOG.warn("object alredy exists");
          cdmiObjectDaoImpl.deleteCdmiObject(dataObject.getObjectId());
          throw new ConflictException("object already exists");
        } catch (IOException e) {
          LOG.error("ERROR: {}", e.getMessage());
          cdmiObjectDaoImpl.deleteCdmiObject(dataObject.getObjectID());
          throw new NotFoundException("resource was not found at the specified uri");
        } catch (Exception e) {
          LOG.error("ERROR: {}", e.getMessage());
          try {
            Files.deleteIfExists(objectPath);
          } catch (IOException e1) {
            LOG.error("ERROR: {}", e1.getMessage());
            // e1.printStackTrace();
          }
          cdmiObjectDaoImpl.deleteCdmiObject(dataObject.getObjectID());
        }
      }
      return null;
    }
  }

  @Override
  public DataObject createNonCDMIByPath(String path, String contentType, DataObject dObj)
      throws Exception {
    throw new UnsupportedOperationException("DataObjectDaoImpl.createNonCDMIByPath()");
  }

  @Override
  public DataObject createById(String objectId, DataObject dObj) {
    throw new UnsupportedOperationException("DataObjectDaoImpl.createById()");
  }

  @Override
  public void deleteByPath(String path) {
    LOG.debug("delete data object {}", path.trim());
    Path objectPath = Paths.get(baseDirectoryName.trim(), path.trim());

    DataObject dataObject =
        (DataObject) cdmiObjectDaoImpl.getCdmiObjectByPath(objectPath.toString());

    if (dataObject != null) {
      try {
        LOG.debug("delete file {}", objectPath.toString());
        Files.delete(objectPath);

        cdmiObjectDaoImpl.deleteCdmiObject(dataObject.getObjectId());
        cdmiObjectDaoImpl.deleteCdmiObjectByPath(objectPath.toString());

        removeChild(objectPath.getFileName().toString(), objectPath.getParent().toString());

      } catch (NoSuchFileException e) {
        LOG.warn("data object not found");
        throw new NotFoundException("data object not found");
      } catch (IOException e) {
        LOG.error("ERROR: {}", e.getMessage());
      } catch (Exception e) {
        LOG.error("ERROR: {}", e.getMessage());
      }
    }
  }

  @Override
  public DataObject findByPath(String path) {
    Path objectPath = Paths.get(baseDirectoryName.trim(), path.trim());
    DataObject dataObject =
        (DataObject) cdmiObjectDaoImpl.getCdmiObjectByPath(objectPath.toString());

    if (dataObject != null) {
      dataObject.setValue(new String(getDataObjectContent(path)));
    }
    return dataObject;
  }

  @Override
  public DataObject findByObjectId(String objectId) {
    DataObject dataObject = (DataObject) cdmiObjectDaoImpl.getCdmiObject(objectId);
    if (dataObject != null) {
      Path path = Paths.get(dataObject.getParentURI(), dataObject.getObjectName());
      dataObject.setValue(new String(getDataObjectContent(path.toString())));
    }
    return dataObject;
  }

  private byte[] getDataObjectContent(String path) {
    Path objectPath = Paths.get(baseDirectoryName.trim(), path.trim());
    byte[] content = null;
    try {
      content = Files.readAllBytes(objectPath);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return content;
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

  private DataObject move(DataObject dataobjectRequest, String moveFrom, String moveTo) {
    Path source = Paths.get(baseDirectoryName.trim(), moveFrom.trim());
    Path target = Paths.get(baseDirectoryName.trim(), moveTo.trim());
    LOG.debug("Move source is {}", source);
    LOG.debug("Move target is {}", target);
    try {
      DataObject dataObject = (DataObject) findByPath(moveFrom);
      if (dataObject != null) {
        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        Container parentObject =
            (Container) cdmiObjectDaoImpl.getCdmiObjectByPath(target.getParent().toString());
        LOG.debug("parent object {}", parentObject.toString());
        dataObject.setObjectName(target.getFileName().toString());
        dataObject.setParentURI(Paths.get(moveTo).getParent().toString());
        dataObject.setParentID(parentObject.getObjectId());
        dataObject.setDomainURI(parentObject.getDomainURI());

        if (dataobjectRequest.getMetadata() != null && !dataobjectRequest.getMetadata().isEmpty())
          dataObject.setMetadata(dataobjectRequest.getMetadata());
        if (dataobjectRequest.getDomainURI() != null && !dataobjectRequest.getDomainURI().isEmpty())
          dataObject.setDomainURI(dataobjectRequest.getDomainURI());

        cdmiObjectDaoImpl.updateCdmiObject(dataObject);

        if (cdmiObjectDaoImpl.createCdmiObject(dataObject, target.toString()) == null)
          cdmiObjectDaoImpl.updateCdmiObject(dataObject, target.toString());

        DataObject newDataObject = (DataObject) findByPath(moveTo);
        if (newDataObject != null) {
          cdmiObjectDaoImpl.deleteCdmiObjectByPath(source.toString());

          String parentPath = source.getParent().toString();

          removeChild(source.getFileName().toString(), parentPath);
          addChild(dataObject.getObjectName(), target.getParent().toString());

          return newDataObject;
        } else {
          throw new NotFoundException("Not found");
        }
      }
    } catch (FileAlreadyExistsException e) {
      throw new ConflictException("DataObject already exists");
    } catch (IOException e) {
      LOG.error("ERROR {}", e.getMessage());
      throw new NotFoundException("dataObject not found");
    }
    return null;
  }

  private CdmiObject copy(DataObject dataobjectRequest, String path) {
    // getting requested Fields
    String[] requestedFields = null;
    String sourcePath = dataobjectRequest.getCopy().trim();
    if (path.contains("?") && sourcePath.contains("?"))
      throw new BadRequestException(
          "The destination dataObject URI and the copy source object URI both specify fields");
    if (sourcePath.contains("?")) {
      requestedFields = sourcePath.split(Pattern.quote("?"))[1].split(";");
      sourcePath = sourcePath.split(Pattern.quote("?"))[0];
    }
    if (path.contains("?")) {
      requestedFields = path.split(Pattern.quote("?"))[1].split(";");
      path = path.split(Pattern.quote("?"))[0];
    }
    // paths
    Path source = Paths.get(baseDirectoryName.trim(), sourcePath);
    Path target = Paths.get(baseDirectoryName.trim(), path.trim());
    LOG.debug("In copy source is {}", source);
    LOG.debug("In copy target is {}", target);

    try {
      DataObject sourceObject = (DataObject) findByPath(sourcePath);
      // check if dataobject is created or updated
      DataObject object = findByPath(path);
      Boolean update = true;
      if (object == null) {
        object = (DataObject) cdmiObjectDaoImpl.createCdmiObject(new DataObject());
        update = false;
      }

      Container parentObject =
          (Container) cdmiObjectDaoImpl.getCdmiObjectByPath(target.getParent().toString());

      if (requestedFields == null) {
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        object.setDomainURI(sourceObject.getDomainURI());
        object.setMetadata(sourceObject.getMetadata());
        object.setMimetype(sourceObject.getMimetype());
        object.setValue(sourceObject.getValue());
      } else {
        for (int i = 0; i < requestedFields.length; i++) {
          String field = requestedFields[i];
          switch (field) {
            case "metadata":
              object.setMetadata(sourceObject.getMetadata());
              break;
            case "domainURI":
              object.setDomainURI(sourceObject.getDomainURI());
              break;
            case "value":
              Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
              object.setValue(sourceObject.getValue());
              break;
            case "mimetype":
              object.setMimetype(sourceObject.getMimetype());
              break;
            default:
              if (field.startsWith("metadata:")) {
                String subfield = field.split(":")[1];
                Map<String, Object> metadata = object.getMetadata();
                if (metadata == null)
                  metadata = new HashMap<String, Object>();
                Map<String, Object> oldMetadata = sourceObject.getMetadata();
                if (oldMetadata == null)
                  oldMetadata = new HashMap<String, Object>();
                metadata.put(subfield, oldMetadata.get(subfield));
                object.setMetadata(metadata);
              }
          }

        }
      }
      object.setCapabilitiesURI(sourceObject.getCapabilitiesURI());
      object.setCompletionStatus(sourceObject.getCompletionStatus());
      object.setObjectName(target.getFileName().toString());
      object.setParentURI(Paths.get(path).getParent().toString());
      object.setParentID(parentObject.getObjectID());
      object.setObjectType(MediaTypes.DATA_OBJECT);

      if (!Files.exists(target))
          Files.write(target, "".getBytes(), StandardOpenOption.CREATE_NEW);

      if (dataobjectRequest.getMetadata() != null && !dataobjectRequest.getMetadata().isEmpty())
        object.setMetadata(dataobjectRequest.getMetadata());
      if (dataobjectRequest.getDomainURI() != null && !dataobjectRequest.getDomainURI().isEmpty())
        object.setDomainURI(dataobjectRequest.getDomainURI());
      if (dataobjectRequest.getMimetype() != null && !dataobjectRequest.getMimetype().isEmpty())
        object.setMimetype(dataobjectRequest.getMimetype());

      // update metadata-files
      cdmiObjectDaoImpl.updateCdmiObject(object);
      if (cdmiObjectDaoImpl.createCdmiObject(object, target.toString()) == null)
        cdmiObjectDaoImpl.updateCdmiObject(object, target.toString());

      // add child to parent
      if (!update)
        addChild(object.getObjectName(), target.getParent().toString());

      return findByPath(path);
    } catch (ClassCastException e) {
      e.printStackTrace();
      throw new BadRequestException("Requested Resource is not a DataObject");
    } catch (FileAlreadyExistsException e) {
      throw new BadRequestException("Bad Request");
    } catch (IOException e) {
      throw new NotFoundException("Object not found");
    }
  }

}
