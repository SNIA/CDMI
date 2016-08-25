/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.cdmi.filesystem;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import edu.kit.scc.CdmiServerApplication;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CdmiObjectDao;
import org.snia.cdmiserver.model.CdmiObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
@ActiveProfiles("filesystem-test")
public class CdmiObjectFilesystemTest {

  @Value("${cdmi.data.objectIdPrefix}")
  String objectIdPrefix;

  static String baseDirectoryName;

  @SuppressWarnings("static-access")
  @Value("${cdmi.data.baseDirectory}")
  private void setBaseDirectory(String baseDirectoryName) {
    this.baseDirectoryName = baseDirectoryName;
  }

  @Autowired
  private CdmiObjectDao cdmiObjectDao;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(CdmiObjectFilesystemTest.class);

  @BeforeClass
  public static void setup() {}

  @Test
  public void testCreateCdmiObject() {
    CdmiObject cdmiObject = new CdmiObject();
    CdmiObject createdObject = cdmiObjectDao.createCdmiObject(cdmiObject);

    assertNotNull(createdObject);
    assertTrue(
        Files.exists(Paths.get(baseDirectoryName, "cdmi_objectid", createdObject.getObjectId())));
  }

  @Test
  public void testCreateCdmiObjectFails() {
    CdmiObject createdObject = cdmiObjectDao.createCdmiObject(null);

    assertNull(createdObject);
  }

  @Test
  public void testCreateCdmiObjectByPath() {
    String objectName = "testCreateCdmiObjectByPath";

    CdmiObject cdmiObject = new CdmiObject();
    CdmiObject createdObject = cdmiObjectDao.createCdmiObject(cdmiObject, objectName);

    assertNotNull(createdObject);
    assertTrue(
        Files.exists(Paths.get(baseDirectoryName, "cdmi_objectid", createdObject.getObjectId())));
    assertTrue(Files.exists(Paths.get(baseDirectoryName, ".cdmi_" + objectName)));
  }

  @Test
  public void testCreateCdmiObjectByPathFails() {
    String objectName = null;

    CdmiObject cdmiObject = new CdmiObject();
    CdmiObject createdObject = cdmiObjectDao.createCdmiObject(cdmiObject, objectName);

    assertNull(createdObject);
    assertTrue(
        !Files.exists(Paths.get(baseDirectoryName, "cdmi_objectid", cdmiObject.getObjectId())));
  }

  @Test
  public void testUpdateCdmiObjectByPath() {
    String objectName = "testUpdateCdmiObjectByPath";

    CdmiObject cdmiObject = new CdmiObject();
    CdmiObject createdObject = cdmiObjectDao.createCdmiObject(cdmiObject, objectName);

    assertNotNull(createdObject);

    CdmiObject updatedObject = new CdmiObject("myId");

    updatedObject = cdmiObjectDao.updateCdmiObject(updatedObject, objectName);

    assertNotNull(updatedObject);
    assertTrue(updatedObject.getObjectId().equals("myId"));
  }

  @Test
  public void testUpdateCdmiObject() {
    CdmiObject cdmiObject = new CdmiObject();
    CdmiObject createdObject = cdmiObjectDao.createCdmiObject(cdmiObject);

    assertNotNull(createdObject);

    CdmiObject updatedObject = cdmiObjectDao.updateCdmiObject(createdObject);

    assertNotNull(updatedObject);
  }

  @Test
  public void testDeleteCdmiObjectByPath() {
    String objectName = "testDeleteCdmiObjectByPath";

    CdmiObject cdmiObject = new CdmiObject();
    CdmiObject createdObject = cdmiObjectDao.createCdmiObject(cdmiObject, objectName);
    assertNotNull(createdObject);
    assertTrue(
        Files.exists(Paths.get(baseDirectoryName, "cdmi_objectid", createdObject.getObjectId())));
    assertTrue(Files.exists(Paths.get(baseDirectoryName, ".cdmi_" + objectName)));

    CdmiObject deletedObject = cdmiObjectDao.deleteCdmiObjectByPath(objectName);
    assertNotNull(deletedObject);
    assertTrue(
        !Files.exists(Paths.get(baseDirectoryName, "cdmi_objectid", createdObject.getObjectId())));
    assertTrue(!Files.exists(Paths.get(baseDirectoryName, ".cdmi_" + objectName)));
  }

  @Test
  public void testDeleteCdmiObject() {
    CdmiObject cdmiObject = new CdmiObject();
    CdmiObject createdObject = cdmiObjectDao.createCdmiObject(cdmiObject);

    assertNotNull(createdObject);
    assertTrue(
        Files.exists(Paths.get(baseDirectoryName, "cdmi_objectid", createdObject.getObjectId())));

    CdmiObject deletedObject = cdmiObjectDao.deleteCdmiObject(createdObject.getObjectId());

    assertNotNull(deletedObject);
    assertTrue(
        !Files.exists(Paths.get(baseDirectoryName, "cdmi_objectid", createdObject.getObjectId())));
  }

  @Test
  public void testGetCdmiObjectByPath() {
    String objectName = "testGetCdmiObjectByPath";

    CdmiObject cdmiObject = new CdmiObject();
    CdmiObject createdObject = cdmiObjectDao.createCdmiObject(cdmiObject, objectName);
    assertNotNull(createdObject);

    CdmiObject getObject = cdmiObjectDao.getCdmiObjectByPath(objectName);

    assertNotNull(getObject);
    assertTrue(createdObject.getObjectId().equals(getObject.getObjectId()));
  }

  @Test
  public void testGetCdmiObject() {
    CdmiObject cdmiObject = new CdmiObject();
    CdmiObject createdObject = cdmiObjectDao.createCdmiObject(cdmiObject);
    assertNotNull(createdObject);

    CdmiObject getObject = cdmiObjectDao.getCdmiObject(createdObject.getObjectId());
    assertNotNull(getObject);
    assertTrue(createdObject.getObjectId().equals(getObject.getObjectId()));
  }

  // @AfterClass
  public static void destroy() throws IOException {
    Path start = Paths.get(baseDirectoryName);
    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException {
        if (ex == null) {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        } else {
          // directory iteration failed
          throw ex;
        }
      }
    });
  }

}
