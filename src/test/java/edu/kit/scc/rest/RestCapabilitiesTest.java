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
import org.apache.http.client.methods.HttpGet;
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
public class RestCapabilitiesTest {

  private HttpClient client = new DefaultHttpClient();
  private String url = "http://localhost:8080/";

  @Test
  public void A_system() throws Exception {
    HttpGet request = new HttpGet(url + "cdmi_capabilities/");

    // add request header

    HttpResponse response = client.execute(request);

    assertThat(response.getStatusLine(), hasStatusCode(200));

    Header[] headers = response.getAllHeaders();
    assertThat(headers,
        hasHeader("Content-Type", "application/cdmi-capability+json;charset=UTF-8"));
    assertThat(headers, hasHeader("X-CDMI-Specification-Version", "1.1.1"));

    HttpEntity entity = response.getEntity();
    String content = EntityUtils.toString(entity);
    assertTrue(content.contains("\"objectID\""));
    assertTrue(content.contains("\"parentID\""));
    assertTrue(content.contains("\"objectName\":\"cdmi_capabilities\""));
    assertTrue(content.contains("\"objectType\":\"application/cdmi-capability\""));
    assertTrue(content.contains("\"parentURI\":\"/\""));
    assertTrue(content.contains("\"childrenrange\""));
    assertTrue(content.contains("\"children\":[\"container\",\"dataobject\"]"));

    assertTrue(content.contains("\"capabilities\":{"));
    assertTrue(content.contains("\"cdmi_metadata_maxitems\":"));
    assertTrue(content.contains("\"cdmi_metadata_maxsize\":"));
    assertTrue(content.contains("\"cdmi_export_occi_iscsi\":"));
    assertTrue(content.contains("\"cdmi_domains\":"));

  }

}
