/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.cdmi.rest;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

@RunWith(Suite.class)
@Suite.SuiteClasses({AuthorizationTest.class, CapabilitiesTest.class, CdmiObjectTest.class,
    ContainerTest.class, DataObjectTest.class, DomainTest.class})
public class RestTestSuite {

  @AfterClass
  public static void destroy() throws IOException {
    Properties props = new Properties();
    InputStream is = ClassLoader.getSystemResourceAsStream("application.properties");
    try {
      props.load(is);
    } catch (IOException e) {
      e.printStackTrace();
    }

    String baseDirectoryName = props.getProperty("cdmi.data.baseDirectory");

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
