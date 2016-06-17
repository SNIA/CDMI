/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.cdmi;

import org.indigo.cdmi.BackEndException;
import org.indigo.cdmi.BackendCapability;
import org.indigo.cdmi.BackendCapability.CapabilityType;
import org.indigo.cdmi.CdmiObjectStatus;
import org.indigo.cdmi.spi.StorageBackend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CapabilityDao;
import org.snia.cdmiserver.dao.CdmiObjectDao;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FilesystemBackend implements StorageBackend {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(FilesystemBackend.class);

  private CdmiObjectDao cdmiObjectDao;
  private CapabilityDao capabilityDao;

  public CdmiObjectDao getCdmiObjectDao() {
    return cdmiObjectDao;
  }

  public void setCdmiObjectDao(CdmiObjectDao cdmiObjectDao) {
    this.cdmiObjectDao = cdmiObjectDao;
  }

  public CapabilityDao getCapabilityDao() {
    return capabilityDao;
  }

  public void setCapabilityDao(CapabilityDao capabilityDao) {
    this.capabilityDao = capabilityDao;
  }

  @Override
  public List<BackendCapability> getCapabilities() {
    HashMap<String, String> capabilities = new HashMap<>();
    capabilities.put("cdmi_capabilities_templates", "true");
    capabilities.put("cdmi _capabilities_exact_inherit", "true");
    capabilities.put("cdmi_data_redundancy", "true");
    capabilities.put("cdmi_geographic_placement", "true");
    capabilities.put("cdmi_latency", "true");
    capabilities.put("cdmi_capabilities_allowed", "");

    HashMap<String, String> metadata = new HashMap<>();
    metadata.put("cdmi_data_redundancy", "4");
    metadata.put("cdmi_geographic_placement", "[DE, FR]");
    metadata.put("cdmi_latency", "100");

    BackendCapability containerCapabilities =
        new BackendCapability("profile1", CapabilityType.CONTAINER);
    containerCapabilities.setMetadata(metadata);
    containerCapabilities.setCapabilities(capabilities);

    BackendCapability dataobjectCapabilities =
        new BackendCapability("profile1", CapabilityType.DATAOBJECT);
    dataobjectCapabilities.setMetadata(metadata);
    dataobjectCapabilities.setCapabilities(capabilities);

    return Arrays.asList(containerCapabilities, dataobjectCapabilities);
  }

  @Override
  public void updateCdmiObject(String path, String capabilitiesUri) throws BackEndException {
    // TODO Auto-generated method stub

  }

  @Override
  public CdmiObjectStatus getCurrentStatus(String path) {
    // TODO Auto-generated method stub
    return null;
  }

}
