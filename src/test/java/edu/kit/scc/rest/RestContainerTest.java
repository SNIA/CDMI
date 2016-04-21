/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.rest;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.snia.cdmiserver.Matchers.hasHeader;
import static org.snia.cdmiserver.Matchers.hasStatusCode;

import edu.kit.scc.CdmiServerApplication;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestContainerTest {

  private HttpClient client = new DefaultHttpClient();
  private String url = "http://localhost:8080/";

  @Test
  public void A_Create_Container() throws Exception {
    HttpPut request = new HttpPut(url + "testContainer");

    // add request header
    request.addHeader("Accept", "application/cdmi-container");
    request.addHeader("Content-Type", "application/cdmi-container");
    request.setEntity(new StringEntity(
        "{ \"value\":{}, \"metadata\" : { created: by test, color:yellow } }", "UTF-8"));

    HttpResponse response = client.execute(request);

    assertThat(response.getStatusLine(), hasStatusCode(201));

    Header[] headers = response.getAllHeaders();
    assertThat(headers, hasHeader("Content-Type", "application/cdmi-container;charset=UTF-8"));

    HttpEntity entity = response.getEntity();
    String content = EntityUtils.toString(entity);
    assertTrue(content.contains("\"objectID\""));
    assertTrue(content.contains("\"parentID\""));
    assertTrue(content.contains("\"objectName\""));
    assertTrue(content.contains("\"capabilitiesURI\":\"/cdmi_capabilities\""));
    assertTrue(content.contains("\"objectType\":\"application/cdmi-container\""));
    assertTrue(content.contains("\"parentURI\":\"/\""));
    assertTrue(content.contains("\"completionStatus\":\"Complete\""));
    assertTrue(content.contains("\"domainURI\":\"/cdmi_domain\""));

  }

  @Test
  public void B_Get_Container() throws Exception {
    HttpGet request = new HttpGet(url + "testContainer");

    // add request header
    request.addHeader("Accept", "application/cdmi-container");

    HttpResponse response = client.execute(request);

    assertThat(response.getStatusLine(), hasStatusCode(200));

    Header[] headers = response.getAllHeaders();
    assertThat(headers, hasHeader("Content-Type", "application/cdmi-container;charset=UTF-8"));

    HttpEntity entity = response.getEntity();
    String content = EntityUtils.toString(entity);
    assertTrue(content.contains("\"objectID\""));
    assertTrue(content.contains("\"parentID\""));
    assertTrue(content.contains("\"objectName\""));
    assertTrue(content.contains("\"capabilitiesURI\":\"/cdmi_capabilities\""));
    assertTrue(content.contains("\"objectType\":\"application/cdmi-container\""));
    assertTrue(content.contains("\"parentURI\":\"/\""));
    assertTrue(content.contains("\"completionStatus\":\"Complete\""));
    assertTrue(content.contains("\"domainURI\":\"/cdmi_domain\""));
    // assertTrue(content.contains("\"children\""));

  }

  @Test
  public void B_Get_ContainerWithFields() throws Exception {
    HttpGet request = new HttpGet(url + "testContainer/?objectName");
    request.addHeader("Accept", "application/cdmi-container");
    HttpResponse response = client.execute(request);
    assertThat(response.getStatusLine(), hasStatusCode(200));
    Header[] headers = response.getAllHeaders();
    assertThat(headers, hasHeader("Content-Type", "application/cdmi-container;charset=UTF-8"));
    HttpEntity entity = response.getEntity();
    String content = EntityUtils.toString(entity);
    assertTrue(content.equals("{\"objectName\":\"testContainer\"}"));

    request = new HttpGet(url + "testContainer/?objectName;metadata");
    request.addHeader("Accept", "application/cdmi-container");
    response = client.execute(request);
    assertThat(response.getStatusLine(), hasStatusCode(200));
    headers = response.getAllHeaders();
    assertThat(headers, hasHeader("Content-Type", "application/cdmi-container;charset=UTF-8"));
    entity = response.getEntity();
    content = EntityUtils.toString(entity);
    assertTrue(content.contains("\"objectName\":\"testContainer\""));
    assertTrue(content.contains("\"metadata\":{\"color\":\"yellow\",\"created\":\"by test\"}")
        || content.contains("\"metadata\":{\"created\":\"by test\",\"color\":\"yellow\"}"));

    request = new HttpGet(url + "testContainer/?metadata:color");
    request.addHeader("Accept", "application/cdmi-container");
    response = client.execute(request);
    assertThat(response.getStatusLine(), hasStatusCode(200));
    headers = response.getAllHeaders();
    assertThat(headers, hasHeader("Content-Type", "application/cdmi-container;charset=UTF-8"));
    entity = response.getEntity();
    content = EntityUtils.toString(entity);
    assertTrue(content.equals("{\"metadata\":{\"color\":\"yellow\"}}"));
  }

  @Test
  public void B_Get_ContainerById() throws Exception {
    HttpGet request =
        new HttpGet(url + "cdmi_objectid/00000008001867BC65353164333663382D313038662D3431");

    // add request header
    request.addHeader("Accept", "application/cdmi-container");

    HttpResponse response = client.execute(request);

    assertThat(response.getStatusLine(), hasStatusCode(200));

    Header[] headers = response.getAllHeaders();
    assertThat(headers, hasHeader("Content-Type", "application/cdmi-container;charset=UTF-8"));

    HttpEntity entity = response.getEntity();
    String content = EntityUtils.toString(entity);
    assertTrue(content.contains("\"objectID\""));
    assertTrue(content.contains("\"parentID\""));
    assertTrue(content.contains("\"objectName\""));
    assertTrue(content.contains("\"capabilitiesURI\":\"/cdmi_capabilities\""));
    assertTrue(content.contains("\"objectType\":\"application/cdmi-container\""));
    assertTrue(content.contains("\"parentURI\":\"/\""));
    assertTrue(content.contains("\"completionStatus\":\"Complete\""));
    assertTrue(content.contains("\"domainURI\":\"/cdmi_domain\""));
    // assertTrue(content.contains("\"children\""));

  }



  @Test
  public void C_Delete_Container() throws Exception {
    HttpDelete request = new HttpDelete(url + "testContainer");

    // add request header
    request.addHeader("Content-Type", "application/cdmi-container");

    HttpResponse response = client.execute(request);

    assertThat(response.getStatusLine(), hasStatusCode(204));
    Header[] headers = response.getAllHeaders();
    assertThat(headers, hasHeader("X-CDMI-Specification-Version", "1.1.1"));

  }

}
