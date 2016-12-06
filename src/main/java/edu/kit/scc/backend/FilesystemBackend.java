/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.backend;

import org.indigo.cdmi.BackEndException;
import org.indigo.cdmi.BackendCapability;
import org.indigo.cdmi.BackendCapability.CapabilityType;
import org.indigo.cdmi.CdmiObjectStatus;
import org.indigo.cdmi.spi.StorageBackend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class FilesystemBackend implements StorageBackend {

  private static final Logger log = LoggerFactory.getLogger(FilesystemBackend.class);

  // simulates back-end capabilities
  private ArrayList<BackendCapability> backendCapabilities = new ArrayList<>();

  // simulates monitored attributes
  private HashMap<String, Object> monitoredAttributes = new HashMap<>();

  // simulates QoS support
  private HashMap<String, CdmiObjectStatus> objectMap = new HashMap<>();

  // simulates storage back-end capabilities
  private HashMap<String, Object> capabilities = new HashMap<>();
  private HashMap<String, Object> metadata = new HashMap<>();

  private Map<String, String> properties;

  /**
   * Constructs a dummy file-system module with dummy values.
   * 
   * @param properties file-system properties
   */
  public FilesystemBackend(Map<String, String> properties) {
    this.properties = properties;

    capabilities.put("cdmi_capabilities_templates", "true");
    capabilities.put("cdmi_capabilities_exact_inherit", "true");
    capabilities.put("cdmi_data_redundancy", "true");
    capabilities.put("cdmi_geographic_placement", "true");
    capabilities.put("cdmi_latency", "true");
    capabilities.put("cdmi_capabilities_allowed", "true");

    metadata.put("cdmi_data_redundancy", "4");
    metadata.put("cdmi_geographic_placement", new String[] {"DE", "FR", "IT", "RO"});
    metadata.put("cdmi_latency", "100");

    monitoredAttributes.put("cdmi_data_redundancy_provided", "4");
    monitoredAttributes.put("cdmi_geographic_placement_provided",
        new String[] {"DE", "FR", "IT", "RO"});
    monitoredAttributes.put("cdmi_latency_provided", "100");

    BackendCapability containerProfile1 =
        new BackendCapability("profile1", CapabilityType.CONTAINER);
    containerProfile1.setCapabilities(capabilities);
    HashMap<String, Object> metadataContainer1 = new HashMap<String, Object>(metadata);
    metadataContainer1.put("cdmi_capabilities_allowed",
        new String[] {"/cdmi_capabilities/container/profile2"});
    containerProfile1.setMetadata(metadataContainer1);

    BackendCapability dataobjectProfile1 =
        new BackendCapability("profile1", CapabilityType.DATAOBJECT);
    dataobjectProfile1.setCapabilities(capabilities);
    HashMap<String, Object> metadataDataobject1 = new HashMap<String, Object>(metadata);
    metadataDataobject1.put("cdmi_capabilities_allowed",
        new String[] {"/cdmi_capabilities/dataobject/profile2"});
    dataobjectProfile1.setMetadata(metadataDataobject1);

    backendCapabilities.add(containerProfile1);
    backendCapabilities.add(dataobjectProfile1);


    BackendCapability containerProfile2 =
        new BackendCapability("profile2", CapabilityType.CONTAINER);
    containerProfile2.setCapabilities(capabilities);
    HashMap<String, Object> metadataContainer2 = new HashMap<String, Object>(metadata);
    metadataContainer2.put("cdmi_capabilities_allowed",
        new String[] {"/cdmi_capabilities/container/profile1"});
    containerProfile2.setMetadata(metadataContainer2);

    BackendCapability dataobjectProfile2 =
        new BackendCapability("profile2", CapabilityType.DATAOBJECT);
    dataobjectProfile2.setCapabilities(capabilities);
    HashMap<String, Object> metadataDataobject2 = new HashMap<String, Object>(metadata);
    metadataDataobject2.put("cdmi_capabilities_allowed",
        new String[] {"/cdmi_capabilities/dataobject/profile1"});
    dataobjectProfile1.setMetadata(metadataDataobject2);

    backendCapabilities.add(containerProfile2);
    backendCapabilities.add(dataobjectProfile2);
  }

  private boolean isSupportedTargetCapabilitiesUri(String path, String capabilitiesUri) {
    CdmiObjectStatus objectStatus = objectMap.get(path);
    if (objectStatus.getCurrentCapabilitiesUri().equals("/cdmi_capabilities/dataobject/profile1")
        && capabilitiesUri.equals("/cdmi_capabilities/dataobject/profile2")) {
      return true;
    }
    if (objectStatus.getCurrentCapabilitiesUri().equals("/cdmi_capabilities/dataobject/profile2")
        && capabilitiesUri.equals("/cdmi_capabilities/dataobject/profile1")) {
      return true;
    }
    if (objectStatus.getCurrentCapabilitiesUri().equals("/cdmi_capabilities/container/profile1")
        && capabilitiesUri.equals("/cdmi_capabilities/container/profile2")) {
      return true;
    }
    if (objectStatus.getCurrentCapabilitiesUri().equals("/cdmi_capabilities/container/profile2")
        && capabilitiesUri.equals("/cdmi_capabilities/container/profile1")) {
      return true;
    }

    if (objectStatus.getCurrentCapabilitiesUri().equals("/cdmi_capabilities/dataobject/profile1")
        && capabilitiesUri.equals("/cdmi_capabilities/dataobject/profile1")) {
      return true;
    }
    if (objectStatus.getCurrentCapabilitiesUri().equals("/cdmi_capabilities/dataobject/profile2")
        && capabilitiesUri.equals("/cdmi_capabilities/dataobject/profile2")) {
      return true;
    }
    if (objectStatus.getCurrentCapabilitiesUri().equals("/cdmi_capabilities/container/profile1")
        && capabilitiesUri.equals("/cdmi_capabilities/container/profile1")) {
      return true;
    }
    if (objectStatus.getCurrentCapabilitiesUri().equals("/cdmi_capabilities/container/profile2")
        && capabilitiesUri.equals("/cdmi_capabilities/container/profile2")) {
      return true;
    }

    log.warn("target capabilities URI not supported {}", capabilitiesUri);
    return false;
  }

  @Override
  public List<BackendCapability> getCapabilities() {
    return backendCapabilities;
  }

  @Override
  public void updateCdmiObject(String path, String targetCapabilitiesUri) throws BackEndException {
    CdmiObjectStatus objectStatus = getCurrentStatus(path);
    log.debug("current object status {}", objectStatus.toString());

    if (!isSupportedTargetCapabilitiesUri(path, targetCapabilitiesUri)) {
      throw new BackEndException();
    }

    String currentCapabilitiesUri = objectStatus.getCurrentCapabilitiesUri();

    log.debug("Simulate QoS transition for {} from {} to {}", path, currentCapabilitiesUri,
        targetCapabilitiesUri);

    objectMap.put(path,
        new CdmiObjectStatus(monitoredAttributes, currentCapabilitiesUri, targetCapabilitiesUri));

    // simulates a 10 sec transition
    long delay = 10 * 1000;
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        log.debug("Simulated QoS transition for {} from {} to {} finished", path,
            currentCapabilitiesUri, targetCapabilitiesUri);
        try {
          CdmiObjectStatus finishedStatus = getCurrentStatus(path);
          objectMap.put(path, new CdmiObjectStatus(monitoredAttributes,
              finishedStatus.getTargetCapabilitiesUri(), null));

        } catch (BackEndException ex) {
          // TODO Auto-generated catch block
          ex.printStackTrace();
        }
      }
    }, delay);
  }

  private Path getFileSystemPath(String path) {
    String baseDirectory = properties.get("baseDirectory");
    log.debug("Base directory {}", baseDirectory);

    Path returnPath = Paths.get(baseDirectory, path);
    log.debug("Filesystem path {}", returnPath.toString());

    return returnPath;
  }

  @Override
  public CdmiObjectStatus getCurrentStatus(String path) throws BackEndException {

    if (!Files.exists(getFileSystemPath(path))) {
      throw new BackEndException("no such file");
    }

    if (Files.isDirectory(getFileSystemPath(path))) {
      if (!objectMap.containsKey(path)) {
        objectMap.put(path, new CdmiObjectStatus(monitoredAttributes,
            "/cdmi_capabilities/container/profile1", null));
      }
    } else {
      if (!objectMap.containsKey(path)) {
        objectMap.put(path, new CdmiObjectStatus(monitoredAttributes,
            "/cdmi_capabilities/dataobject/profile1", null));
      }
    }
    return objectMap.get(path);
  }
}
