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
import org.indigo.cdmi.Status;
import org.indigo.cdmi.spi.StorageBackend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FilesystemBackend implements StorageBackend {

  private static final Logger log = LoggerFactory.getLogger(FilesystemBackend.class);

  // simulates monitored attributes
  private HashMap<String, String> monitoredAttributes = new HashMap<>();

  // simulates transitions of QoS
  private HashMap<String, CdmiObjectStatus> transitionMap = new HashMap<>();

  // simulates storage back-end capabilities
  private HashMap<String, String> capabilities = new HashMap<>();
  HashMap<String, String> metadata = new HashMap<>();

  /**
   * Constructs a dummy file-system module with dummy values.
   */
  public FilesystemBackend() {

    capabilities.put("cdmi_capabilities_templates", "true");
    capabilities.put("cdmi_capabilities_exact_inherit", "true");
    capabilities.put("cdmi_data_redundancy", "true");
    capabilities.put("cdmi_geographic_placement", "true");
    capabilities.put("cdmi_latency", "true");
    capabilities.put("cdmi_capabilities_allowed", "profile1 profile2");

    metadata.put("cdmi_data_redundancy", "4");
    metadata.put("cdmi_geographic_placement", "[DE, FR]");
    metadata.put("cdmi_latency", "100");

    monitoredAttributes.put("cdmi_data_redundancy_provided", "4");
    monitoredAttributes.put("cdmi_geographic_placement_provided", "[DE, FR]");
    monitoredAttributes.put("cdmi_latency_provided", "100");

  }

  @Override
  public List<BackendCapability> getCapabilities() {
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
  public void updateCdmiObject(String path, String targetCapabilitiesUri) throws BackEndException {
    if (!transitionMap.containsKey(path)) {
      transitionMap.put(path,
          new CdmiObjectStatus(Status.OK, monitoredAttributes, "profile1", null));
    }

    String currentCapabilitiesUri = transitionMap.get(path).getCurrentCapabilitiesUri();

    log.debug("Simulate QoS transition for {} from {} to {}", path, currentCapabilitiesUri,
        targetCapabilitiesUri);
    transitionMap.put(path, new CdmiObjectStatus(Status.TRANSITION, monitoredAttributes,
        currentCapabilitiesUri, targetCapabilitiesUri));

    // simulates a 10 sec transition
    long delay = 10 * 1000;
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        log.debug("Simulated QoS transition for {} from {} to {} finished", path,
            currentCapabilitiesUri, targetCapabilitiesUri);
        transitionMap.put(path,
            new CdmiObjectStatus(Status.OK, monitoredAttributes, targetCapabilitiesUri, null));
      }
    }, delay);
  }

  @Override
  public CdmiObjectStatus getCurrentStatus(String path) {
    if (transitionMap.containsKey(path)) {
      return transitionMap.get(path);
    }
    return new CdmiObjectStatus(Status.OK, monitoredAttributes, "profile1", null);
  }
}
