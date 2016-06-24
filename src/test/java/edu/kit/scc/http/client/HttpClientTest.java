/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.http.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import edu.kit.scc.CdmiServerApplication;
import edu.kit.scc.http.HttpClient;
import edu.kit.scc.http.HttpResponse;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.charset.StandardCharsets;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
public class HttpClientTest {

  @Autowired
  HttpClient client;

  @Test
  public void testMakeHttpGetRequestWrongUrl() {
    String url = "invalid";

    HttpResponse response = client.makeHttpGetRequest(url);

    assertNull(response);
  }

  @Test
  public void testMakeHttpGetRequest() {
    String url = "http://www.kit.edu";

    HttpResponse response = client.makeHttpGetRequest(url);

    assertNotNull(response);
    assertTrue(response.getStatusCode() == 200);
  }

  @Test
  public void testMakeHttpGetRequestWithAuthorization() {
    String url = "http://www.kit.edu";
    String restUser = "test";
    String restPassword = "test";
    String auth = restUser + ":" + restPassword;
    byte[] authZheader = auth.getBytes();
    String authorizationHeader =
        "Basic " + new String(Base64.encodeBase64(authZheader), StandardCharsets.UTF_8);

    HttpResponse response = client.makeHttpGetRequest(restUser, restPassword, url);

    assertNotNull(response);
    assertTrue(response.getStatusCode() == 200);
  }

  @Test
  public void testMakeHttpsGetRequest() {
    String url = "https://api.duckduckgo.com/?q=KIT&format=json&pretty=1";

    HttpResponse response = client.makeHttpsGetRequest(url);

    // assertNotNull(response);
    // assertTrue(response.getStatusCode() == 200);
  }

  @Test
  public void testMakeHttpsGetRequestWithAuthorization() {
    String url = "https://api.duckduckgo.com/?q=KIT&format=json&pretty=1";
    String restUser = "test";
    String restPassword = "test";
    String auth = restUser + ":" + restPassword;
    byte[] authZheader = auth.getBytes();
    String authorizationHeader =
        "Basic " + new String(Base64.encodeBase64(authZheader), StandardCharsets.UTF_8);

    HttpResponse response = client.makeHttpsGetRequest(restUser, restPassword, url);

    // assertNotNull(response);
    // assertTrue(response.getStatusCode() == 200);
  }

  @Test
  public void testMakeHttpPostRequest() {
    String url = "http://www.kit.edu";

    HttpResponse response = client.makeHttpPostRequest(null, url);

    assertNotNull(response);
    // assertTrue(response.getStatusCode() == 200);
  }

  @Test
  public void testMakeHttpPostRequestWithAuthorization() {
    String url = "http://www.kit.edu";
    String restUser = "test";
    String restPassword = "test";
    String auth = restUser + ":" + restPassword;
    byte[] authZheader = auth.getBytes();
    String authorizationHeader =
        "Basic " + new String(Base64.encodeBase64(authZheader), StandardCharsets.UTF_8);

    HttpResponse response = client.makeHttpPostRequest(restUser, restPassword, null, url);

    assertNotNull(response);
    assertTrue(response.getStatusCode() == 200);
  }

  @Test
  public void testMakeHttpsPostRequest() {
    String url = "https://api.duckduckgo.com/?q=KIT&format=json&pretty=1";

    HttpResponse response = client.makeHttpPostRequest(null, url);

    // assertNotNull(response);
    // assertTrue(response.getStatusCode() == 200);
  }

  @Test
  public void testMakeHttpsPostRequestWithAuthorization() {
    String url = "https://api.duckduckgo.com/?q=KIT&format=json&pretty=1";
    String restUser = "test";
    String restPassword = "test";
    String auth = restUser + ":" + restPassword;
    byte[] authZheader = auth.getBytes();
    String authorizationHeader =
        "Basic " + new String(Base64.encodeBase64(authZheader), StandardCharsets.UTF_8);

    HttpResponse response = client.makeHttpsPostRequest(restUser, restPassword, null, url);

    // assertNotNull(response);
    // assertTrue(response.getStatusCode() == 200);
  }
}
