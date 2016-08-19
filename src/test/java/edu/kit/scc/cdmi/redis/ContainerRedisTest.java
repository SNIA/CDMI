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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import edu.kit.scc.CdmiServerApplication;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.ContainerDao;
import org.snia.cdmiserver.model.Container;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.file.Paths;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
@ActiveProfiles("redis-embedded")
public class ContainerRedisTest {

  @Autowired
  ContainerDao containerDao;

  private static final Logger log = LoggerFactory.getLogger(ContainerRedisTest.class);

  @Test
  public void testCreateContainer() {
    String containerName = "container1";

    Container containerRequest = Container.fromJson(new JSONObject("{}"));
    log.debug(containerRequest.toString());

    Container container =
        containerDao.createByPath(Paths.get("/", containerName).toString(), containerRequest);

    assertNotNull(container);

  }

  @Test
  public void testCreateContainerIfExists() {
    String containerName = "existingContainer";

    Container containerRequest = Container.fromJson(new JSONObject("{}"));
    Container container1 =
        containerDao.createByPath(Paths.get("/", containerName).toString(), containerRequest);

    assertNotNull(container1);

    Container container2 =
        containerDao.createByPath(Paths.get("/", containerName).toString(), containerRequest);

    assertEquals(container1.toString(), container2.toString());
  }

  @Test
  public void testCreateRootContainer() {
    Container containerRequest = Container.fromJson(new JSONObject("{}"));
    Container container = containerDao.createByPath(null, containerRequest);

    assertTrue(container == null);

    container = containerDao.createByPath("/", null);

    //assertNotNull(container);
  }

  @Test
  public void testParentMetadataAfterCreateContainer() {
    String containerName = "metadataTest";
    String childContainerName = "childContainer";

    Container containerRequest = Container.fromJson(new JSONObject("{}"));

    Container container =
        containerDao.createByPath(Paths.get("/", containerName).toString(), containerRequest);

    assertNotNull(container);

    Container childContainer = containerDao.createByPath(
        Paths.get("/", containerName, childContainerName).toString(), containerRequest);

    assertNotNull(childContainer);

    String parentObjectId = childContainer.getParentId();
    log.debug("Parent id {}", childContainer.getParentId());
    Container parentContainer = containerDao.findByObjectId(parentObjectId);
    assertNotNull(parentContainer);
    log.debug("Parent container {}", parentContainer.toJson());
    assertTrue(parentContainer.getObjectId().equals(childContainer.getParentId()));
    assertTrue(parentContainer.getChildrenrange().equals("0"));
    assertTrue(parentContainer.getChildren().get(0).equals(childContainer.getObjectName()));
  }

  @Test
  public void testFindByObjectId() {
    String containerName = "getContainer";

    Container containerRequest = Container.fromJson(new JSONObject("{}"));
    Container container =
        containerDao.createByPath(Paths.get("/", containerName).toString(), containerRequest);

    assertNotNull(container);

    Container foundContainer = containerDao.findByObjectId(container.getObjectId());
    assertNotNull(foundContainer);
  }

  @Test
  public void testFindByPath() {
    String containerName = "getContainerByPath";

    Container containerRequest = Container.fromJson(new JSONObject("{}"));
    Container container =
        containerDao.createByPath(Paths.get("/", containerName).toString(), containerRequest);

    assertNotNull(container);

    Container foundContainer = containerDao.findByPath(Paths.get("/", containerName).toString());
    assertNotNull(foundContainer);
  }

  @Test
  public void testIsContainer() {
    String containerName = "isContainer";

    Container containerRequest = Container.fromJson(new JSONObject("{}"));
    Container container =
        containerDao.createByPath(Paths.get("/", containerName).toString(), containerRequest);

    assertNotNull(container);

    assertTrue(containerDao.isContainer(Paths.get("/", containerName).toString()));

    assertFalse(containerDao.isContainer(Paths.get("/", containerName, "invalid").toString()));

  }

  @Test
  public void testDeleteContainer() {
    String containerName = "deleteContainer";

    Container containerRequest = Container.fromJson(new JSONObject("{}"));
    Container container =
        containerDao.createByPath(Paths.get("/", containerName).toString(), containerRequest);

    assertNotNull(container);

    containerDao.deleteByPath(Paths.get("/", containerName).toString());
  }

  @Test
  public void testCreateContainerFailsForInvalidPath() {
    String containerName = "invalidContainer";

    Container container = containerDao.createByPath(Paths.get("/", containerName).toString(), null);

    assertNull(container);
  }
}
