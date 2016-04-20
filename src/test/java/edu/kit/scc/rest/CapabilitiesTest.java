package edu.kit.scc.rest;

import edu.kit.scc.CdmiRestController;
import edu.kit.scc.CdmiServerApplication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.HandlerMapping;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
public class CapabilitiesTest {

  @Autowired
  private CdmiRestController controller;

  @Test
  public void test() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/cdmi_capabilities/container/");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_capabilities/container/");
    request.setMethod("GET");


    controller.capabilities(request, response);
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


    controller.capabilities(request, response);
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


    controller.capabilities(request, response);
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


    controller.capabilities(request, response);
  }

  @Test
  public void testDeepCapabilities() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI(
        "/cdmi_capabilities/dataobjectdefault/tape/disk/?capabilities:cdmi_read_value;capabilities:cdmi_read_metadata");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_capabilities/dataobject/default/tape/");
    request.setParameter("capabilities:cdmi_read_value;capabilities:cdmi_read_metadata", "");
    request.setMethod("GET");


    controller.capabilities(request, response);
  }
}
