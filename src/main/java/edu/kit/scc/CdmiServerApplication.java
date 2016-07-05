/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class CdmiServerApplication {

  private static final Logger log = LoggerFactory.getLogger(CdmiServerApplication.class);

  public static void main(String[] args) {
    log.debug("Starting Spring Boot Application Server");
    SpringApplication.run(CdmiServerApplication.class, args);
  }
}
