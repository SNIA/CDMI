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
public class ContainerTest {

  private static final Logger log = LoggerFactory.getLogger(ContainerTest.class);

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
  public void testDeleteEmptyContainer() {
    String containerName = "emptyContainer";

    String authString = Base64.encodeBase64String((restUser + ":" + restPassword).getBytes());
    Response response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/cdmi-container").and().body("{}".getBytes()).when()
        .put("/" + containerName + "?query=value").then()
        .statusCode(org.apache.http.HttpStatus.SC_CREATED).extract().response();

    log.debug("Response {}", response.asString());

    response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/cdmi-container").and().when()
        .delete("/" + containerName).then().statusCode(org.apache.http.HttpStatus.SC_NO_CONTENT)
        .extract().response();

    log.debug("Response {}", response.asString());
  }

  @Test
  public void testPutNewContainer() {
    String containerName = "newContainer";

    String authString = Base64.encodeBase64String((restUser + ":" + restPassword).getBytes());
    Response response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/cdmi-container").and().body("{}".getBytes()).when()
        .put("/" + containerName + "?query=value").then()
        .statusCode(org.apache.http.HttpStatus.SC_CREATED).extract().response();

    log.debug("Response {}", response.asString());
  }

  @Test
  public void testGetRootContainer() {
    String authString = Base64.encodeBase64String((restUser + ":" + restPassword).getBytes());
    log.debug(authString);
    Response response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/cdmi-object").when().get("/").then()
        .statusCode(org.apache.http.HttpStatus.SC_OK).extract().response();

    log.debug("Response {}", response.asString());
  }

  @Test
  public void testGetContainerNotFound() {
    String authString = Base64.encodeBase64String((restUser + ":" + restPassword).getBytes());

    Response response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/cdmi-object").when().get("/invalid").then()
        .statusCode(org.apache.http.HttpStatus.SC_NOT_FOUND).extract().response();

    log.debug("Response {}", response.asString());
  }

  @Test
  public void testGetContainerNotAuthorized() {
    String authString = Base64.encodeBase64String((restUser + "1:" + restPassword).getBytes());

    Response response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/cdmi-object").when().get("/").then()
        .statusCode(org.apache.http.HttpStatus.SC_UNAUTHORIZED).extract().response();

    log.debug("Response {}", response.asString());
  }
}
