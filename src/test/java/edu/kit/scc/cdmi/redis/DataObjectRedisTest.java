/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.cdmi.redis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import edu.kit.scc.CdmiServerApplication;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.snia.cdmiserver.dao.DataObjectDao;
import org.snia.cdmiserver.model.DataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.file.Paths;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
@ActiveProfiles("redis-embedded")
public class DataObjectRedisTest {

  @Autowired
  private DataObjectDao dataObjectDao;

  @Test
  public void testCreateDataObject() {
    String dataObjectName = "dataobject1";

    DataObject dataObjectRequest = DataObject.fromJson(new JSONObject("{}"));

    DataObject dataObject =
        dataObjectDao.createByPath(Paths.get("/", dataObjectName).toString(), dataObjectRequest);

    assertNotNull(dataObject);
  }

  @Test
  public void testCreateNonCdmiException() {
    boolean correctException = false;

    try {
      dataObjectDao.createNonCdmiByPath(null, null, null);
    } catch (UnsupportedOperationException ex) {
      correctException = true;
    } catch (Exception ex) {
      correctException = false;
    }
    assertTrue(correctException);
  }

  @Test
  public void testCreateByIdException() {
    boolean correctException = false;

    try {
      dataObjectDao.createById(null, null);
    } catch (UnsupportedOperationException ex) {
      correctException = true;
    } catch (Exception ex) {
      correctException = false;
    }
    assertTrue(correctException);
  }

  @Test
  public void testDeleteDataObject() {
    String dataObjectName = "deleteDataObject";

    DataObject dataObjectRequest = DataObject.fromJson(new JSONObject("{}"));

    DataObject dataObject =
        dataObjectDao.createByPath(Paths.get("/", dataObjectName).toString(), dataObjectRequest);

    assertNotNull(dataObject);

    dataObjectDao.deleteByPath(Paths.get("/", dataObjectName).toString());

  }

  @Test
  public void testFindByPath() {
    String dataObjectName = "findByPath";

    DataObject dataObjectRequest = DataObject.fromJson(new JSONObject("{}"));

    DataObject dataObject =
        dataObjectDao.createByPath(Paths.get("/", dataObjectName).toString(), dataObjectRequest);

    assertNotNull(dataObject);

    DataObject foundObject = dataObjectDao.findByPath(Paths.get("/", dataObjectName).toString());

    assertNotNull(foundObject);
    assertTrue(dataObject.getObjectId().equals(foundObject.getObjectId()));

  }

  @Test
  public void testFindById() {
    String dataObjectName = "findById";

    DataObject dataObjectRequest = DataObject.fromJson(new JSONObject("{}"));

    DataObject dataObject =
        dataObjectDao.createByPath(Paths.get("/", dataObjectName).toString(), dataObjectRequest);

    assertNotNull(dataObject);

    DataObject foundObject = dataObjectDao.findByObjectId(dataObject.getObjectId());

    assertNotNull(foundObject);
    assertTrue(dataObject.getObjectId().equals(foundObject.getObjectId()));
  }

  @Test
  public void testCreateDataObjectIfAlreadyExists() {
    String dataObjectName = "existing";

    DataObject dataObjectRequest = DataObject.fromJson(new JSONObject("{}"));

    DataObject dataObject1 =
        dataObjectDao.createByPath(Paths.get("/", dataObjectName).toString(), dataObjectRequest);

    assertNotNull(dataObject1);

    DataObject dataObject2 =
        dataObjectDao.createByPath(Paths.get("/", dataObjectName).toString(), dataObjectRequest);

    assertEquals(dataObject1.toString(), dataObject2.toString());
  }
}
