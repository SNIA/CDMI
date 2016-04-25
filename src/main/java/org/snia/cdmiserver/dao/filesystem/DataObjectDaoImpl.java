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
            dataObject.setDomainURI(domainUri);
          dataObject.setMetadata(dataObjectRequest.getMetadata());

          // optional
          // dataObject.setPercentComplete(percentComplete);
          // dataObject.setMimetype(mimetype);

          dataObject.setCompletionStatus("Complete");

          cdmiObjectDaoImpl.updateCdmiObject(dataObject);

          if (cdmiObjectDaoImpl.createCdmiObject(dataObject, file.toString()) == null)
            cdmiObjectDaoImpl.updateCdmiObject(dataObject, file.toString());
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
}
