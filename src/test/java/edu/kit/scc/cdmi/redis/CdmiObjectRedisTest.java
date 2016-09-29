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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CdmiObjectDao;
import org.snia.cdmiserver.model.CdmiObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
@ActiveProfiles("redis-embedded")
public class CdmiObjectRedisTest {

  private static final Logger log = LoggerFactory.getLogger(CdmiObjectRedisTest.class);

  @Autowired
  CdmiObjectDao cdmiObjectDao;

  @Test
  public void redisSetupTest() {

  }

  @Test
  public void createCdmiObjectByIdTest() {
    CdmiObject cdmiObject = new CdmiObject();

    CdmiObject createdObject = cdmiObjectDao.createCdmiObject(cdmiObject);

    assertNotNull(createdObject);

    log.debug(createdObject.toJson().toString());
  }

}
