/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.cdmi.filesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import edu.kit.scc.CdmiServerApplication;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.DataObjectDao;
import org.snia.cdmiserver.model.DataObject;
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
@ActiveProfiles("test")
public class DataObjectFilesystemTest {

  static String baseDirectoryName;

  @SuppressWarnings("static-access")
  @Value("${cdmi.data.baseDirectory}")
  private void setBaseDirectory(String baseDirectoryName) {
    this.baseDirectoryName = baseDirectoryName;
  }

  @Value("${cdmi.data.objectIdPrefix}")
  private String objectIdPrefix;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DataObjectFilesystemTest.class);

  @Autowired
  private DataObjectDao dataObjectDao;

  @BeforeClass
  public static void setup() {}

  @Test
  public void testCreateDataObject() {
    String dataObjectName = "dataobject1";

    DataObject dataObjectRequest = DataObject.fromJson(new JSONObject("{}"));

    DataObject dataObject =
        dataObjectDao.createByPath(Paths.get("/", dataObjectName).toString(), dataObjectRequest);

    assertNotNull(dataObject);

    assertTrue(Files.exists(Paths.get(baseDirectoryName, dataObjectName)));

    assertTrue(Files.exists(Paths.get(baseDirectoryName, objectIdPrefix + dataObjectName)));
    assertTrue(
        Files.exists(Paths.get(baseDirectoryName, "cdmi_objectid", dataObject.getObjectId())));
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

    assertTrue(Files.exists(Paths.get(baseDirectoryName, dataObjectName)));
    assertTrue(Files.exists(Paths.get(baseDirectoryName, objectIdPrefix + dataObjectName)));
    assertTrue(
        Files.exists(Paths.get(baseDirectoryName, "cdmi_objectid", dataObject.getObjectId())));

    dataObjectDao.deleteByPath(Paths.get("/", dataObjectName).toString());

    assertTrue(!Files.exists(Paths.get(baseDirectoryName, dataObjectName)));
    assertTrue(!Files.exists(Paths.get(baseDirectoryName, objectIdPrefix + dataObjectName)));
    assertTrue(
        !Files.exists(Paths.get(baseDirectoryName, "cdmi_objectid", dataObject.getObjectId())));
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
