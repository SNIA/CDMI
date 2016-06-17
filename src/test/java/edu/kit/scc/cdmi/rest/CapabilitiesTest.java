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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
@WebIntegrationTest
public class CapabilitiesTest {

  private static final Logger log = LoggerFactory.getLogger(CapabilitiesTest.class);

  @Value("${rest.user}")
  private String restUser;

  @Value("${rest.pass}")
  private String restPassword;

  @Before
  public void setUpEach() {
    RestAssured.baseURI = "http://localhost:8080";
  }

  @Test
  public void testGetRootCapabilities() {
    String authString = Base64.encodeBase64String((restUser + ":" + restPassword).getBytes());

    Response response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/cdmi-capability").when().get("/cdmi_capabilities")
        .then().statusCode(org.apache.http.HttpStatus.SC_OK).extract().response();

    log.debug("Response {}", response.asString());
  }

  @Test
  public void testGetContainerCapabilities() {
    String authString = Base64.encodeBase64String((restUser + ":" + restPassword).getBytes());

    Response response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/cdmi-capability").when()
        .get("/cdmi_capabilities/container").then().statusCode(org.apache.http.HttpStatus.SC_OK)
        .extract().response();

    log.debug("Response {}", response.asString());
  }

  @Test
  public void testGetDataObjectCapabilities() {
    String authString = Base64.encodeBase64String((restUser + ":" + restPassword).getBytes());

    Response response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/cdmi-capability").when()
        .get("/cdmi_capabilities/dataobject").then().statusCode(org.apache.http.HttpStatus.SC_OK)
        .extract().response();

    log.debug("Response {}", response.asString());
  }

  @Test
  public void testGetCapabilitiesNotAuthorized() {
    String authString = Base64.encodeBase64String((restUser + "1:" + restPassword).getBytes());

    Response response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/cdmi-capability").when().get("/cdmi_capabilities")
        .then().statusCode(org.apache.http.HttpStatus.SC_UNAUTHORIZED).extract().response();

    log.debug("Response {}", response.asString());
  }
}
