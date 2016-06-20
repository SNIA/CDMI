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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CdmiObjectDao;
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

  private static final Logger LOG = LoggerFactory.getLogger(DataObjectDaoImpl.class);

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
  public DataObject createByPath(String path, DataObject dataObjectRequest) {
    if (path == null) {
      return null;
    }

    final Path dataObjectPath = Paths.get(baseDirectoryName.trim(), path.trim());

    try {
      Files.createFile(dataObjectPath);
      LOG.debug("create data object {} {}", path.trim(), dataObjectRequest.toString());

      if (dataObjectRequest.getValue() != null) {
        Files.write(dataObjectPath, dataObjectRequest.getValue().getBytes());
        LOG.debug("writing value to data object");
      }
    } catch (FileAlreadyExistsException ex) {
      LOG.error(ex.getMessage());
      return null;
    } catch (Exception ex) {
      LOG.error(ex.getMessage());
      return null;
    }

    String objectName = dataObjectPath.getFileName().toString();
    String parentUri = Paths.get(path.trim()).getParent() == null ? "/"
        : Paths.get(path.trim()).getParent().toString();
    String parentPath = dataObjectPath.getParent() == null ? baseDirectoryName
        : dataObjectPath.getParent().toString();

    Container parentContainer =
        (Container) cdmiObjectDao.getCdmiObjectByPath(parentPath.toString());

    DataObject dataObject = new DataObject(objectName, parentUri, parentContainer.getObjectId());

    if (parentContainer.getChildren() == null) {
      parentContainer.setChildren(new JSONArray());
    }
    parentContainer.getChildren().put(dataObject.getObjectName());
    String childrenRange = CdmiObject.getChildrenRange(parentContainer.getChildren());
    parentContainer.setChildrenrange(childrenRange);

    dataObject.setCompletionStatus("Complete");
    dataObject.setMetadata(dataObjectRequest.getMetadata());

    cdmiObjectDao.createCdmiObject(dataObject, dataObjectPath.toString());
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
    if (path == null) {
      return null;
    }
    LOG.debug("delete data object {}", path.trim());

    final Path objectPath = Paths.get(baseDirectoryName.trim(), path.trim());
    DataObject dataObject = (DataObject) cdmiObjectDao.getCdmiObjectByPath(objectPath.toString());

    if (dataObject != null) {
      try {
        LOG.debug("delete file {}", objectPath.toString());
        Files.delete(objectPath);

        cdmiObjectDao.deleteCdmiObject(dataObject.getObjectId());
        cdmiObjectDao.deleteCdmiObjectByPath(objectPath.toString());

        // removeChild(objectPath.getFileName().toString(), objectPath.getParent().toString());

      } catch (Exception e) {
        LOG.error("ERROR: {}", e.getMessage());
      }
    }
    return dataObject;
  }

  @Override
  public DataObject findByPath(String path) {
    if (path == null) {
      return null;
    }

    Path objectPath = Paths.get(baseDirectoryName.trim(), path.trim());
    DataObject dataObject = (DataObject) cdmiObjectDao.getCdmiObjectByPath(objectPath.toString());
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
    } catch (IOException e) {
      e.printStackTrace();
    }
    return content;
  }

  @Override
  public DataObject updateContent(String path, byte[] content) {
    if (path == null) {
      return null;
    }

    Path objectPath = Paths.get(baseDirectoryName.trim(), path.trim());
    DataObject dataObject = (DataObject) cdmiObjectDao.getCdmiObjectByPath(objectPath.toString());

    if (dataObject != null) {
      try {
        Files.write(objectPath, content, StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING);
        LOG.debug("writing value to data object");
      } catch (IOException ex) {
        // ex.printStackTrace();
        LOG.error("ERROR {}", ex.getMessage());
      }
    }
    return dataObject;
  }
}
