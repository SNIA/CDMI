/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CapabilityDao;
import org.snia.cdmiserver.dao.CdmiObjectDao;
import org.snia.cdmiserver.model.Capability;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.Container;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

import javax.annotation.PostConstruct;

@Component
@Profile({"redis"})
public class RedisConfiguration {

  private static final Logger log = LoggerFactory.getLogger(RedisConfiguration.class);

  @Autowired
  private CdmiObjectDao cdmiObjectDao;

  @Autowired
  private CapabilityDao capabilityDao;

  /**
   * Configuration for CDMI redis version.
   * 
   */
  @PostConstruct
  public void init() {
    log.debug("Set-up root container...");

    CdmiObject rootObject = new CdmiObject();
    Container rootContainer = new Container("/", "/", rootObject.getObjectId());
    rootContainer.setObjectId(rootObject.getObjectId());

    rootContainer = (Container) cdmiObjectDao.createCdmiObject(rootContainer, "/");
    log.debug("root container created {}", rootContainer.toString());

    rootObject = cdmiObjectDao.getCdmiObjectByPath("/");

    Capability rootCapability = new Capability("cdmi_capabilities", "/", rootObject.getObjectId());
    rootCapability =
        (Capability) cdmiObjectDao.createCdmiObject(rootCapability, "/cdmi_capabilities");
    log.debug("root capability created {}", rootCapability.toString());

    Capability containerCapability =
        new Capability("container", "/cdmi_capabilities", rootCapability.getObjectId());
    capabilityDao.createByPath(Paths.get("cdmi_capabilities", "container").toString(),
        containerCapability);

    Capability dataObjectCapability =
        new Capability("dataobject", "/cdmi_capabilities", rootCapability.getObjectId());
    capabilityDao.createByPath(Paths.get("cdmi_capabilities", "dataobject").toString(),
        dataObjectCapability);

    Capability defaultContainerCapability =
        capabilityDao.findByPath(Paths.get("cdmi_capabilities", "container").toString());
    log.debug(defaultContainerCapability.toString());

    Capability defaultDataObjectCapability =
        capabilityDao.findByPath(Paths.get("cdmi_capabilities", "dataobject").toString());
    log.debug(defaultDataObjectCapability.toString());
  }
}
