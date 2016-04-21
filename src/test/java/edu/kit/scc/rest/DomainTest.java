package edu.kit.scc.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import edu.kit.scc.CdmiRestController;
import edu.kit.scc.CdmiServerApplication;

import org.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.HandlerMapping;
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
public class DomainTest {

  @Autowired
  private CdmiRestController controller;

  private String movingId;

  @Test
  public void A_create() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/cdmi_domains/testDomain");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_domains/testDomain/");
    request.addHeader("Accept", "application/cdmi-domain");
    request.addHeader("Content-Type", "application/cdmi-domain");
    request.setContent("{\"metadata\" : { created: by test, color:red } }".getBytes());
    request.setMethod("PUT");


    ResponseEntity<?> answer = controller.putCdmiObject(request.getContentType(),
        "{\"metadata\" : { created: by test, color:red } }", request, response);

    assertNotNull(answer);

  }

  @Test
  public void A_update() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/cdmi_domains/testDomain");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_domains/testDomain/");
    request.addHeader("Accept", "application/cdmi-domain");
    request.addHeader("Content-Type", "application/cdmi-domain");
    request.setContent("{\"metadata\" : { created: by test, color:yellow } }".getBytes());
    request.setMethod("PUT");


    ResponseEntity<?> answer = controller.putCdmiObject(request.getContentType(),
        "{\"metadata\" : { created: by test, color:yellow } }", request, response);

    assertNotNull(answer);
  }

  @Test
  public void A_updateField() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/cdmi_domains/testDomain");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_domains/testDomain/");
    request.addHeader("Accept", "application/cdmi-domain");
    request.addHeader("Content-Type", "application/cdmi-domain");
    request.setContent("{\"metadata\" : { created: updatet by test } }".getBytes());
    request.setParameter("metadata:created", "");
    request.setMethod("PUT");


    ResponseEntity<?> answer = controller.putCdmiObject(request.getContentType(),
        "{\"metadata\" : { created: updatet by test } }", request, response);

    assertNotNull(answer);
  }

  @Test
  public void BA_copy() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/cdmi_domains/testDomain2");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_domains/testDomain2/");
    request.addHeader("Accept", "application/cdmi-domain");
    request.addHeader("Content-Type", "application/cdmi-domain");
    request.setContent("{copy:\"/cdmi_domains/testDomain\"}".getBytes());
    request.setMethod("PUT");


    ResponseEntity<?> answer = controller.putCdmiObject(request.getContentType(),
        "{copy:\"/cdmi_domains/testDomain\"}", request, response);

    JSONObject json = new JSONObject((String) answer.getBody());
    System.out.println(answer.getBody());
    System.out.println(json.toString());
    movingId = json.getString("objectID");
    assertNotNull(movingId);
    System.out.println(movingId);
    BB_move(movingId);
  }


  public void BB_move(String movingId) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/cdmi_domains/testDomain/testSub");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_domains/testDomain/testSub/");
    request.addHeader("Accept", "application/cdmi-domain");
    request.addHeader("Content-Type", "application/cdmi-domain");
    request.setContent("{move:\"/cdmi_domains/testDomain2\"}".getBytes());
    request.setMethod("PUT");


    ResponseEntity<?> answer = controller.putCdmiObject(request.getContentType(),
        "{move:\"/cdmi_domains/testDomain2\"}", request, response);

    assertNotNull(answer);
    assertNotNull(movingId);
    System.out.println(movingId);
    C_check_moved(movingId);
  }

  @Test
  public void C_get() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/cdmi_domains/testDomain/");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_domains/testDomain/");
    request.setMethod("GET");


    ResponseEntity<?> content = controller.getDomainByPath(request, response);
    assertNotNull(content);
    JSONObject answer = new JSONObject((String) content.getBody());
    String objectId = answer.getString("objectID");

    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/cdmi_objectid/" + objectId);
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_objectid/" + objectId);
    request.setMethod("GET");
    content = controller.getCdmiObjectByID(objectId, request, response);


  }


  public void C_check_moved(String movingId) {

      MockHttpServletRequest request = new MockHttpServletRequest();
      MockHttpServletResponse response = new MockHttpServletResponse();
      request.setServerName("localhost:8080");
      request.setRequestURI("/cdmi_domains/testDomain2/");
      request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
          "/cdmi_domains/testDomain2/");
      request.setMethod("GET");


      ResponseEntity<?> content = controller.getDomainByPath(request, response);
    System.out.println(content.getStatusCode());
    assertTrue(content.getStatusCode().equals(HttpStatus.NOT_FOUND));


    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
      request.setServerName("localhost:8080");
      request.setRequestURI("/cdmi_domains/testDomain/testSub");
      request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
          "/cdmi_domains/testDomain/testSub");
      request.setMethod("GET");

    content = controller.getDomainByPath(request, response);
    JSONObject json = new JSONObject((String) content.getBody());
    assertTrue(movingId.equals(json.getString("objectID")));


      request = new MockHttpServletRequest();
      response = new MockHttpServletResponse();
      request.setServerName("localhost:8080");
      request.setRequestURI("/cdmi_objectid/" + movingId);
      request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
          "/cdmi_objectid/" + movingId);
      request.setMethod("GET");
      assertNotNull(movingId);
      content = controller.getCdmiObjectByID(movingId, request, response);


  }

  @Test
  public void C_get_fields() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/cdmi_domains/testDomain/?metadata:color");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_domains/testDomain/");
    request.setParameter("metadata:color", "");
    request.setMethod("GET");


    ResponseEntity<?> answer = controller.getDomainByPath(request, response);
    assertTrue(answer.getBody().equals("{\"metadata\":{\"color\":\"yellow\"}}"));

  }

  @Test
  public void D_delete() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    // request.setServerName("localhost:8080");
    // request.setRequestURI("/cdmi_domains/testDomain/testSub/testSub");
    // request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
    // "/cdmi_domains/testDomain/testSub/testSub");
    // request.addHeader("Content-Type", "application/cdmi-domain");
    // request.setMethod("DELETE");


    // controller.deleteCdmiObject(request, response);

    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/cdmi_domains/testDomain/testSub");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_domains/testDomain/testSub");
    request.addHeader("Content-Type", "application/cdmi-domain");
    request.setMethod("DELETE");


    controller.deleteCdmiObject(request, response);

    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    request.setServerName("localhost:8080");
    request.setRequestURI("/cdmi_domains/testDomain/");
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/cdmi_domains/testDomain/");
    request.addHeader("Content-Type", "application/cdmi-domain");
    request.setMethod("DELETE");


    controller.deleteCdmiObject(request, response);

    // request = new MockHttpServletRequest();
    // response = new MockHttpServletResponse();
    // request.setServerName("localhost:8080");
    // request.setRequestURI("/cdmi_domains/testDomain2/");
    // request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
    // "/cdmi_domains/testDomain/");
    // request.addHeader("Content-Type", "application/cdmi-domain");
    // request.setMethod("DELETE");
    //
    //
    // controller.deleteCdmiObject(request, response);

  }

}
