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
public class DomainTest {

  private static final Logger log = LoggerFactory.getLogger(DomainTest.class);

  @Value("${rest.user}")
  private String restUser;

  @Value("${rest.pass}")
  private String restPassword;

  @Before
  public void setUpEach() {
    RestAssured.baseURI = "http://localhost:8080";
  }

  @Test
  public void testGetDomainNotFound() {
    String authString = Base64.encodeBase64String((restUser + ":" + restPassword).getBytes());

    Response response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/cdmi-domain").when().get("/cdmi_domains").then()
        .statusCode(org.apache.http.HttpStatus.SC_NOT_FOUND).extract().response();

    log.debug("Response {}", response.asString());
  }

  @Test
  public void testGetDomainNotAuthorized() {
    String authString = Base64.encodeBase64String(("invalid").getBytes());

    Response response = given().header("Authorization", "Basic " + authString).and()
        .header("Content-Type", "application/cdmi-domain").when().get("/cdmi_domains").then()
        .statusCode(org.apache.http.HttpStatus.SC_UNAUTHORIZED).extract().response();

    log.debug("Response {}", response.asString());
  }

}
