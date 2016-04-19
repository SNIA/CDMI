package edu.kit.scc.rest;

import edu.kit.scc.CdmiRestController;
import edu.kit.scc.CdmiServerApplication;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.HandlerMapping;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
public class ContainerTest {

  @Autowired
  private CdmiRestController controller;

  @Test
  public void A_create() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/containerTest/");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, "/containerTest/");
    request.addHeader("Accept", "application/cdmi-container");
    request.addHeader("Content-Type", "application/cdmi-container");
    request.setContent(
        "{ \"value\":{}, \"metadata\" : { created: by test, color:yellow } }".getBytes());
    request.setMethod("PUT");


    controller.putCdmiObject(request.getContentType(),
        "{ \"value\":{}, \"metadata\" : { created: by test, color:yellow } }", request, response);
  }

  @Test
  public void B_get() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/containerTest/");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, "/containerTest/");
    request.setMethod("GET");


    String content = controller.getCdmiObjectByPath(request, response);
    String objectId = content.split("objectID\":\"")[1].split("\"")[0];

    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/cdmi_objectid/" + objectId);
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_objectid/" + objectId);
    request.setMethod("GET");
    controller.getCdmiObjectByID(objectId, request, response);
  }

  @Test
  public void C_get_fields() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/containerTest/?metadata:color");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, "/containerTest/");
    request.setParameter("metadata:color", "");
    request.setMethod("GET");


    controller.getCdmiObjectByPath(request, response);

  }

  @Test
  public void D_delete() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/containerTest/");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, "/containerTest/");
    request.addHeader("Content-Type", "application/cdmi-container");
    request.setMethod("DELETE");


    controller.deleteCdmiObject(request, response);

  }


}
