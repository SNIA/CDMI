/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc;

import org.indigo.cdmi.BackendCapability;
import org.indigo.cdmi.BackendCapability.CapabilityType;
import org.indigo.cdmi.ConfigurableStorageBackend;
import org.indigo.cdmi.spi.StorageBackend;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CapabilityDao;
import org.snia.cdmiserver.dao.CdmiObjectDao;
import org.snia.cdmiserver.model.Capability;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.Container;
import org.snia.cdmiserver.model.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
@Profile("filesystem")
public class FilesystemConfiguration {

  private static final Logger log = LoggerFactory.getLogger(FilesystemConfiguration.class);

  @Value("${cdmi.data.baseDirectory}")
  private String baseDirectory;

  @Value("${cdmi.qos.backend.type}")
  private String backendType;

  @Autowired
  private CdmiObjectDao cdmiObjectDao;

  @Autowired
  private CapabilityDao capabilityDao;

  /**
   * Configuration for CDMI file system version.
   * 
   * @throws IOException in case directories couldn't be created
   */
  @PostConstruct
  public void init() throws IOException {
    log.debug("Set-up root container...");

    CdmiObject rootObject = new CdmiObject();

    Path path = Paths.get(baseDirectory);
    if (!Files.exists(path)) {
      Files.createDirectories(Paths.get(baseDirectory, "cdmi_objectid"));
      log.debug("root directory {} created", path.toString());

      Container rootContainer = new Container("/", "/", rootObject.getObjectId());
      rootContainer.setObjectId(rootObject.getObjectId());

      cdmiObjectDao.createCdmiObject(rootContainer, baseDirectory);
    }

    path = Paths.get(baseDirectory, "cdmi_capabilities");
    if (!Files.exists(path)) {
      Files.createDirectory(Paths.get(baseDirectory, "cdmi_capabilities"));

      rootObject = cdmiObjectDao.getCdmiObjectByPath(baseDirectory);

      Capability rootCapability =
          new Capability("cdmi_capabilities", "/", rootObject.getObjectId());
      cdmiObjectDao.createCdmiObject(rootCapability,
          Paths.get(baseDirectory, "cdmi_capabilities").toString());

      Capability containerCapability =
          new Capability("container", "/cdmi_capabilities", rootCapability.getObjectId());
      capabilityDao.createByPath(Paths.get("cdmi_capabilities", "container").toString(),
          containerCapability);

      Capability dataObjectCapability =
          new Capability("dataobject", "/cdmi_capabilities", rootCapability.getObjectId());
      capabilityDao.createByPath(Paths.get("cdmi_capabilities", "dataobject").toString(),
          dataObjectCapability);
    }

    path = Paths.get(baseDirectory, "cdmi_domains");
    if (!Files.exists(path)) {
      Files.createDirectory(path);
      log.debug("domain directory {} created", path.toString());

      rootObject = cdmiObjectDao.getCdmiObjectByPath(baseDirectory);

      Domain rootDomain = new Domain("cdmi_domains", "/", rootObject.getObjectId());

      cdmiObjectDao.createCdmiObject(rootDomain,
          Paths.get(baseDirectory, "cdmi_domains").toString());
    }

    Capability containerCapability =
        capabilityDao.findByPath(Paths.get("cdmi_capabilities", "container").toString());
    log.debug(containerCapability.toString());

    Capability dataObjectCapability =
        capabilityDao.findByPath(Paths.get("cdmi_capabilities", "dataobject").toString());
    log.debug(dataObjectCapability.toString());

    // Connect to a specific file system storage back-end implementation.
    //
    // Creates the provided for this specific storage back-end capabilities.
    try {
      StorageBackend storageBackend =
          ConfigurableStorageBackend.createStorageBackend(backendType, null);

      List<BackendCapability> capabilities = storageBackend.getCapabilities();
      for (BackendCapability capability : capabilities) {
        log.debug("Found capability type {} {}", capability.getType().name(), capability.getName());

        if (capability.getType().equals(CapabilityType.CONTAINER)) {
          Capability providedCapability = new Capability(capability.getName(),
              "/cdmi_capabilities/container", containerCapability.getObjectId());
          providedCapability.setCapabilities(new JSONObject(capability.getCapabilities()));
          providedCapability.setMetadata(new JSONObject(capability.getMetadata()));
          capabilityDao.createByPath(
              Paths.get("cdmi_capabilities", "container", capability.getName()).toString(),
              providedCapability);
        }
        if (capability.getType().equals(CapabilityType.DATAOBJECT)) {
          Capability providedCapability = new Capability(capability.getName(),
              "/cdmi_capabilities/dataobject", dataObjectCapability.getObjectId());
          providedCapability.setCapabilities(new JSONObject(capability.getCapabilities()));
          providedCapability.setMetadata(new JSONObject(capability.getMetadata()));
          capabilityDao.createByPath(
              Paths.get("cdmi_capabilities", "dataobject", capability.getName()).toString(),
              providedCapability);
        }
      }
    } catch (IllegalArgumentException ex) {
      log.warn("ERROR: {}", ex.getMessage());
    }
  }

  @PreDestroy
  public void cleanUp() {

  }
}
