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

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CapabilityDao;
import org.snia.cdmiserver.exception.NotFoundException;
import org.snia.cdmiserver.model.Capability;
import org.snia.cdmiserver.util.MediaTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * <p>
 * Concrete implementation of {@link CapabilityObjectDao} using the local filesystem as the backing
 * store.
 * </p>
 */
@Component
public class CapabilityDaoImpl implements CapabilityDao {

  private static final Logger LOG = LoggerFactory.getLogger(CapabilityDaoImpl.class);

  // -------------------------------------------------------------- Properties

  @Value("${cdmi.data.rootObjectId}")
  private String ROOTobjectID;

  private JSONObject json;
  private JSONObject system;
  private JSONObject dataobject;
  private JSONObject container;
  private String properties;

  // ---------------------------------------------------- ContainerDao Methods

  @Override
  public Capability findByObjectId(String objectId) {
    throw new UnsupportedOperationException("CapabilityDaoImpl.findByObjectId()");
  }

  private void readProperties() {
    ApplicationContext ctx = new ClassPathXmlApplicationContext();
    Resource capabilitiesConfiguration = ctx.getResource("classpath:capabilities.properties.json");
    Resource applicationConfiguration = ctx.getResource("classpath:application.properties");
    LOG.debug("Load capabilities configuration: {}", capabilitiesConfiguration.getFilename());
    LOG.debug("Load application configuration: {}", applicationConfiguration.getFilename());

    String file;
    InputStream in = null;
    try {
      in = applicationConfiguration.getInputStream();
      byte bt[] = new byte[(int) applicationConfiguration.contentLength()];
      in.read(bt);

      properties = new String(bt);
      in.close();

      in = capabilitiesConfiguration.getInputStream();
      bt = new byte[(int) capabilitiesConfiguration.contentLength()];
      in.read(bt);
      file = new String(bt);
      in.close();

      json = new JSONObject(file);
      system = json.getJSONObject("system-capabilities");
      dataobject = json.getJSONObject("data-object-capabilities");
      container = json.getJSONObject("container-capabilities");

    } catch (Exception e) {
      LOG.error("ERROR: {}", e.getMessage());
      // e.printStackTrace();
    } finally {
      ((ConfigurableApplicationContext) ctx).close();
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException ex) {
        LOG.error("ERROR: {}", ex.getMessage());
      }
    }
  }

  @Override
  public Capability findByPath(String path) {
    LOG.trace("In Capability.findByPath, path is: {}", path);
    Capability capability = new Capability();
    readProperties();

    String[] pathsplit = path.split("/");
    String request = null;
    if (pathsplit.length >= 3) {
      switch (pathsplit[2]) {
        case "container":
          request = "container";
          break;
        case "dataobject":
          request = "dataobject";
          break;
        default:
          throw new NotFoundException("capability not found");
      }
      if (request != null) {
        JSONObject object = null;
        switch (request) {
          case "container":
            object = container;
            break;
          case "dataobject":
            object = dataobject;
            break;
          default:
            throw new NotFoundException("capability not found");
        }
        if (object != null) {
          try {
            path = path.split("/cdmi_capabilities/" + request + "/")[1];
            if (path.equals(""))
              throw new IndexOutOfBoundsException();
            capability.setParentURI("cdmi_capabilities/" + request);
            rekursivGetCapability(capability, path, object);
          } catch (IndexOutOfBoundsException e) {
            capability.setParentURI("cdmi_capabilities");
            capability.setParentID(getIdByUri(capability.getParentURI()));
            capability.setObjectName(request);
            capability.setObjectID(
                getIdByUri(capability.getParentURI() + "/" + capability.getObjectName()));
            Iterator<?> keys = object.keys();
            while (keys.hasNext()) {
              String child = (String) keys.next();
              if (object.get(child) instanceof JSONObject)
                capability.getChildren().add(child);
              else
                capability.getCapabilities().put(child, String.valueOf(object.get(child)));
            }
            if (!capability.getChildren().isEmpty())
              capability
                  .setChildrenrange("0-" + String.valueOf(capability.getChildren().size() - 1));
          }
        }
      }
    } else {
      // System Capabilities
      LOG.trace("System Capabilities");

      Iterator<?> keys = system.keys();
      while (keys.hasNext()) {
        String key = (String) keys.next();
        capability.getCapabilities().put(key, String.valueOf(system.get(key)));
      }
      capability.getChildren().add("container");
      capability.getChildren().add("dataobject");
      capability.setChildrenrange("0-" + String.valueOf(capability.getChildren().size() - 1));
      capability.setObjectID(getIdByUri("cdmi_capabilities"));
      capability.setObjectName("cdmi_capabilities");
      capability.setParentURI("/");
      capability.setParentID(ROOTobjectID);
    }
    capability.setObjectType(MediaTypes.CAPABILITY);
    return (capability);

  }

  private Capability rekursivGetCapability(Capability capability, String path, JSONObject object) {
    Boolean end = false;
    if (path.endsWith("/"))
      path = path.substring(0, path.length() - 1);
    String[] pathSplit = path.split("/", 2);
    String pathPart = pathSplit[0];
    try {
      path = pathSplit[1];
    } catch (IndexOutOfBoundsException e) {
      end = true;
    }

    try {
      if (end) {
        JSONObject newObject = object.getJSONObject(pathPart);
        Iterator<?> keys = newObject.keys();
        while (keys.hasNext()) {
          String cap = (String) keys.next();
          if (newObject.get(cap) instanceof JSONObject)
            capability.getChildren().add(cap);
          else
            capability.getCapabilities().put(cap, String.valueOf(newObject.get(cap)));
        }
        if (!capability.getChildren().isEmpty())
          capability.setChildrenrange("0-" + String.valueOf(capability.getChildren().size() - 1));
        String parentUri = capability.getParentURI();
        capability.setParentID(getIdByUri(parentUri));
        capability.setObjectName(pathPart);
        capability.setObjectID(getIdByUri(parentUri + "/" + pathPart));
        return capability;
      } else {
        JSONObject newObject = object.getJSONObject(pathPart);
        capability.setParentURI(capability.getParentURI() + "/" + pathPart);
        return rekursivGetCapability(capability, path, newObject);
      }
    } catch (org.json.JSONException e) {
      throw new NotFoundException("Capability not found");
    }

  }

  private String getIdByUri(String uri) {
    LOG.trace("InCapabilities.getIdByUri URI is {}", uri);
    String searchkey = uri.replace("/", ".");
    if (searchkey.endsWith("."))
      searchkey = searchkey + "ObjectId";
    else
      searchkey = searchkey + ".ObjectId";
    if (searchkey.startsWith("."))
      searchkey = searchkey.substring(1, searchkey.length());
    int position = properties.toLowerCase().indexOf(searchkey.toLowerCase());
    if (position != -1) {
      int startindex = position + searchkey.length() + 1;
      String id = properties.substring(startindex, startindex + 50).trim();
      return id;
    }
    return null;
  }

}
