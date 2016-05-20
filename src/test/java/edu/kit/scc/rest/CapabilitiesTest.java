/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.rest;

import edu.kit.scc.CdmiRestController;
import edu.kit.scc.CdmiServerApplication;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.HandlerMapping;

import java.nio.charset.StandardCharsets;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
public class CapabilitiesTest {

  @Autowired
  private CdmiRestController controller;

  @Value("${rest.user}")
  private String restUser;

  @Value("${rest.pass}")
  private String restPassword;

  @Test
  public void test() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/cdmi_capabilities/container/");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_capabilities/container/");
    request.setMethod("GET");

    String auth = restUser + ":" + restPassword;
    byte[] authZheader = auth.getBytes();
    String authorization =
        "Basic " + new String(Base64.encodeBase64(authZheader), StandardCharsets.UTF_8);

    controller.capabilities(authorization, request, response);
  }

  @Test
  public void testFields() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request
        .setRequestURI("/cdmi_capabilities/container/?children:0;childrenrange;objectID;parentURI");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_capabilities/container/");
    request.setParameter("children:0;childrenrange;objectID;parentURI", "");
    request.setMethod("GET");

    String auth = restUser + ":" + restPassword;
    byte[] authZheader = auth.getBytes();
    String authorization =
        "Basic " + new String(Base64.encodeBase64(authZheader), StandardCharsets.UTF_8);

    controller.capabilities(authorization, request, response);
  }

  @Test
  public void testCapabilities() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI(
        "/cdmi_capabilities/dataobject/disk/?capabilities:cdmi_read_value;capabilities:cdmi_read_metadata");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_capabilities/dataobject/disk/");
    request.setParameter("capabilities:cdmi_read_value;capabilities:cdmi_read_metadata", "");
    request.setMethod("GET");

    String auth = restUser + ":" + restPassword;
    byte[] authZheader = auth.getBytes();
    String authorization =
        "Basic " + new String(Base64.encodeBase64(authZheader), StandardCharsets.UTF_8);

    controller.capabilities(authorization, request, response);
  }

  @Test
  public void testSystemCapabilities() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/cdmi_capabilities/?children:0-1");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_capabilities/");
    request.setParameter("children:0-1", "");
    request.setMethod("GET");

    String auth = restUser + ":" + restPassword;
    byte[] authZheader = auth.getBytes();
    String authorization =
        "Basic " + new String(Base64.encodeBase64(authZheader), StandardCharsets.UTF_8);

    controller.capabilities(authorization, request, response);
  }

  @Test
  public void testDeepCapabilities() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI(
        "/cdmi_capabilities/dataobject/default/?capabilities:cdmi_read_value;capabilities:cdmi_read_metadata");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_capabilities/dataobject/default/");
    request.setParameter("capabilities:cdmi_read_value;capabilities:cdmi_read_metadata", "");
    request.setMethod("GET");

    String auth = restUser + ":" + restPassword;
    byte[] authZheader = auth.getBytes();
    String authorization =
        "Basic " + new String(Base64.encodeBase64(authZheader), StandardCharsets.UTF_8);

    controller.capabilities(authorization, request, response);
  }
}
