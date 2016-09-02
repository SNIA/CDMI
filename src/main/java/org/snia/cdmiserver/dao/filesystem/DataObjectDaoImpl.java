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

package org.snia.cdmiserver.dao.filesystem;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CdmiObjectDao;
import org.snia.cdmiserver.dao.ContainerDao;
import org.snia.cdmiserver.dao.DataObjectDao;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.Container;
import org.snia.cdmiserver.model.DataObject;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * <p>
 * Concrete implementation of {@link DataObjectDao} using the local file system as the backing
 * store.
 * </p>
 */
public class DataObjectDaoImpl implements DataObjectDao {

  private static final Logger log = LoggerFactory.getLogger(DataObjectDaoImpl.class);

  private String baseDirectoryName;

  private CdmiObjectDao cdmiObjectDao;

  private ContainerDao containerDao;

  public ContainerDao getContainerDao() {
    return containerDao;
  }

  public void setContainerDao(ContainerDao containerDao) {
    this.containerDao = containerDao;
  }

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
  public DataObject createByPath(String path, DataObject dataObjectRequest) {
    try {
      final Path dataObjectPath = Paths.get(baseDirectoryName.trim(), path.trim());
      // create the data object file
      Files.createFile(dataObjectPath);
      log.debug("create data object {} {}", path.trim(), dataObjectRequest.toString());
      log.debug("create file {}", dataObjectPath.toString());
      if (dataObjectRequest.getValue() != null) {
        Files.write(dataObjectPath, dataObjectRequest.getValue().getBytes());
        log.debug("writing value to data object");
      }
    } catch (FileAlreadyExistsException ex) {
      log.error(ex.getMessage());
    } catch (Exception ex) {
      log.error(ex.getMessage());
      return null;
    }

    Path urlPath = Paths.get(path.trim());
    Path parentPath = urlPath.getParent();
    if (parentPath == null) {
      // root container
      return null;
    }

    // create the data object meta-data files
    Container parentContainer =
        (Container) cdmiObjectDao.getCdmiObjectByPath(parentPath.toString());

    // fix: try recursively for non-existing parents
    if (parentContainer == null) {
      parentContainer = containerDao.createByPath(parentPath.toString(),
          Container.fromJson(new JSONObject("{}")));
    }

    DataObject dataObject = new DataObject(urlPath.getFileName().toString(), parentPath.toString(),
        parentContainer.getObjectId());

    if (parentContainer.getChildren() == null) {
      parentContainer.setChildren(new JSONArray());
    }

    JSONArray children = parentContainer.getChildren();
    JSONArray filteredChildren = new JSONArray();

    for (int i = 0; i < children.length(); i++) {
      if (!children.get(i).equals(dataObject.getObjectName())) {
        filteredChildren.put(children.get(i));
      }
    }

    filteredChildren.put(dataObject.getObjectName());
    parentContainer.setChildren(filteredChildren);

    String childrenRange = CdmiObject.getChildrenRange(parentContainer.getChildren());
    parentContainer.setChildrenrange(childrenRange);

    dataObject.setCompletionStatus("Complete");
    dataObject.setMetadata(dataObjectRequest.getMetadata());
    // dataObject.setCapabilitiesUri(dataObjectRequest.getCapabilitiesUri());

    dataObject = (DataObject) cdmiObjectDao.createCdmiObject(dataObject, urlPath.toString());
    cdmiObjectDao.updateCdmiObject(parentContainer);

    return dataObject;
  }

  @Override
  public DataObject createNonCdmiByPath(String path, String contentType, DataObject dataObj) {
    throw new UnsupportedOperationException("DataObjectDaoImpl.createNonCDMIByPath()");
  }

  @Override
  public DataObject createById(String objectId, DataObject dataObj) {
    throw new UnsupportedOperationException("DataObjectDaoImpl.createById()");
  }

  @Override
  public DataObject deleteByPath(String path) {
    DataObject dataObject = null;
    try {
      log.debug("delete data object {}", path.trim());

      dataObject = (DataObject) cdmiObjectDao.getCdmiObjectByPath(path.trim());

      if (dataObject != null) {
        final Path objectPath = Paths.get(baseDirectoryName.trim(), path.trim());

        log.debug("delete file {}", objectPath.toString());
        Files.delete(objectPath);

        cdmiObjectDao.deleteCdmiObject(dataObject.getObjectId());
        cdmiObjectDao.deleteCdmiObjectByPath(path.trim());

        // removeChild(objectPath.getFileName().toString(), objectPath.getParent().toString());
      }
    } catch (Exception ex) {
      log.error("ERROR: {}", ex.getMessage());
    }
    return dataObject;
  }

  @Override
  public DataObject findByPath(String path) {
    DataObject dataObject = (DataObject) cdmiObjectDao.getCdmiObjectByPath(path.trim());
    // if (dataObject != null) {
    // dataObject.setValue(new String(getDataObjectContent(path)));
    // }
    return dataObject;
  }

  @Override
  public DataObject findByObjectId(String objectId) {
    DataObject dataObject = (DataObject) cdmiObjectDao.getCdmiObject(objectId);
    // if (dataObject != null) {
    // Path path = Paths.get(dataObject.getParentUri(), dataObject.getObjectName());
    // dataObject.setValue(new String(getDataObjectContent(path.toString())));
    // }
    return dataObject;
  }

  @SuppressWarnings("unused")
  private byte[] getDataObjectContent(String path) {
    Path objectPath = Paths.get(baseDirectoryName.trim(), path.trim());
    byte[] content = null;
    try {
      content = Files.readAllBytes(objectPath);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return content;
  }

  @Override
  public DataObject updateContent(String path, byte[] content) {
    DataObject dataObject = (DataObject) cdmiObjectDao.getCdmiObjectByPath(path.trim());

    if (dataObject != null) {
      try {
        Path objectPath = Paths.get(baseDirectoryName.trim(), path.trim());
        Files.write(objectPath, content, StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING);
        log.debug("writing value to data object");
      } catch (IOException ex) {
        // ex.printStackTrace();
        log.error("ERROR {}", ex.getMessage());
      }
    }
    return dataObject;
  }
}
