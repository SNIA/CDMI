/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.cdmi.redis;

import static org.junit.Assert.assertNotNull;

import edu.kit.scc.CdmiServerApplication;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CapabilityDao;
import org.snia.cdmiserver.model.Capability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.file.Paths;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
@ActiveProfiles("redis-embedded")
public class CapabilityRedisTest {

  private static final Logger log = LoggerFactory.getLogger(CapabilityRedisTest.class);

  @Autowired
  private CapabilityDao capabilityDao;

  @Test
  public void testCreateCapability() {
    String capabilityName = "testCreateCapability";
    Capability capabilityRequest = Capability.fromJson(new JSONObject("{}"));
    Capability capability = capabilityDao.createByPath(
        Paths.get("/cdmi_capabilities", capabilityName).toString(), capabilityRequest);

    assertNotNull(capability);
  }

  @Test
  public void testFindCapabilityById() {
    String capabilityName = "testFindCapabilityById";
    Capability capabilityRequest = Capability.fromJson(new JSONObject("{}"));
    Capability capability = capabilityDao.createByPath(
        Paths.get("/cdmi_capabilities", capabilityName).toString(), capabilityRequest);

    assertNotNull(capability);
    log.debug("Capability {}", capability.toJson());
    log.debug("Search for {}", capability.getObjectId());

    Capability getCapability = capabilityDao.findByObjectId(capability.getObjectId());

    assertNotNull(getCapability);
    log.debug("Capability {}", getCapability.toJson());
  }

  @Test
  public void testFindCapabilityByPath() {
    String capabilityName = "testFindCapabilityByPath";
    Capability capabilityRequest = Capability.fromJson(new JSONObject("{}"));
    Capability capability = capabilityDao.createByPath(
        Paths.get("/cdmi_capabilities", capabilityName).toString(), capabilityRequest);

    assertNotNull(capability);

    Capability getCapability =
        capabilityDao.findByPath(Paths.get("/cdmi_capabilities", capabilityName).toString());

    assertNotNull(getCapability);
  }
}
