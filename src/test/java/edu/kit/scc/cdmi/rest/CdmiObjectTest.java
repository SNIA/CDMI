/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.cdmi.rest;

import static com.jayway.restassured.RestAssured.given;

import edu.kit.scc.CdmiServerApplication;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
@WebIntegrationTest
@ActiveProfiles("test")
public class CdmiObjectTest {
  private static final Logger log = LoggerFactory.getLogger(CdmiObjectTest.class);

  @Value("${rest.user}")
  private String restUser;

  @Value("${rest.pass}")
  private String restPassword;

  @Value("${server.port}")
  private int serverPort;

  @Before
  public void setUpEach() {
    RestAssured.baseURI = "http://localhost:" + String.valueOf(serverPort);
  }

  @Test
  public void testGetObjectByIdNotFound() {
    String authString = Base64.encodeBase64String((restUser + ":" + restPassword).getBytes());

    Response response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/cdmi-object").when().get("/cdmi_objectid/invalid")
        .then().statusCode(org.apache.http.HttpStatus.SC_NOT_FOUND).extract().response();

    log.debug("Response {}", response.asString());
  }

  @Test
  public void testGetObjectByIdNotAuthorized() {
    String authString = Base64.encodeBase64String((restUser + "1:" + restPassword).getBytes());

    Response response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/cdmi-object").when().get("/cdmi_objectid/invalid")
        .then().statusCode(org.apache.http.HttpStatus.SC_UNAUTHORIZED).extract().response();

    log.debug("Response {}", response.asString());
  }
}
