/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.filesystem.CdmiObjectDaoImpl;
import org.snia.cdmiserver.model.CdmiObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
public class CdmiObjectFilesystemTest {

  private static final Logger log = LoggerFactory.getLogger(CdmiObjectFilesystemTest.class);

  @Autowired
  private CdmiObjectDaoImpl objectIdDaoImpl;

  private static CdmiObject testObjectId;

  @Before
  public void setup() {
    String json =
        "{\"objectType\": \"application/cdmi-container\",\"objectID\": \"00007ED900104E1D14771DC67C27BF8B\",\"objectName\": \"MyContainer/\",\"parentURI\": \"/\",\"parentID\": \"00007E7F0010128E42D87EE34F5A6560\",\"domainURI\": \"/cdmi_domains/MyDomain/\",\"capabilitiesURI\": \"/cdmi_capabilities/container/\",\"completionStatus\": \"Complete\",\"metadata\": {\"meta\":\"data\"},\"childrenrange\": \"\",\"children\": []}";
    testObjectId = new CdmiObject(new JSONObject(json));
  }

  @Test
  public void toJsonTest() {
    CdmiObject object = new CdmiObject(testObjectId.toJson());

    log.debug(testObjectId.toString());
    log.debug(object.toString());

    log.debug(testObjectId.toJson().toString());
    log.debug(object.toJson().toString());

    assertEquals(testObjectId.toString(), object.toString());
  }

  @Test
  public void crudObjectIdTest() {

    assertNotNull(testObjectId);
    log.debug(testObjectId.toString());

    objectIdDaoImpl.createCdmiObject(testObjectId);

    testObjectId.getAttributeMap().put("childrenrange", "0-9");

    objectIdDaoImpl.updateCdmiObject(testObjectId);

    CdmiObject objectId = objectIdDaoImpl.getCdmiObject(testObjectId.getObjectId());

    assertNotNull(objectId);
    log.debug(objectId.toString());

    objectIdDaoImpl.deleteCdmiObject(objectId.getObjectId());
  }
}
