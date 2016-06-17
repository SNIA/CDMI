/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.cdmi.rest;

import static org.junit.Assert.assertTrue;

import edu.kit.scc.CdmiRestController;
import edu.kit.scc.CdmiServerApplication;

import org.apache.commons.codec.binary.Base64;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.charset.StandardCharsets;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
public class AuthorizationTest {

  @Autowired
  private CdmiRestController controller;

  @Value("${rest.user}")
  private String restUser;

  @Value("${rest.pass}")
  private String restPassword;

  @Test
  public void testBasicAuthorization() {
    String auth = restUser + ":" + restPassword;
    byte[] authZheader = auth.getBytes();
    String authorizationHeader =
        "Basic " + new String(Base64.encodeBase64(authZheader), StandardCharsets.UTF_8);

    assertTrue(controller.verifyAuthorization(authorizationHeader));
  }

  @Ignore
  @Test
  public void testUserBearerAuthorization() {
    String token = "eyJraWQiOiJyc2ExIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiI1NGQ3NWJmZi03YWUzLTRkNjUtODFkYi04MWM0NTYwMjA2NTUiLCJpc3MiOiJodHRwczpcL1wvaWFtLXRlc3QuaW5kaWdvLWRhdGFjbG91ZC5ldVwvIiwiZXhwIjoxNDYyOTc1ODk0LCJpYXQiOjE0NjI5NzIyOTQsImp0aSI6ImYzOTcyZmJkLWM0NmItNDc1Ni04MTg5LTQ1Mjc0ODRkZTMzNSJ9.PUx_Yf5Haz9YgjI-VjY5nR0Im-A8L9EVC-QOUiCM0xkC11KI65Kf3dmmf2W2mwQag5VeK3OUwfeKLkxKux_OkYB59kN06wYAJKRvjcfiiivxroUjugyY1-R3IUIpCZRPRTq0l9FyoqIOs1DXcu1o8zS-5lvzLPfdcxVTx3Pn9Lc";
    String authorizationHeader = "Bearer " + token;

    assertTrue(controller.verifyAuthorization(authorizationHeader));
  }

  @Test
  public void testClientBearerAuthorization() {
    String token =
        "eyJraWQiOiJyc2ExIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJjNTI2M2NkZS05NDY3LTQ5YzYtYWU1Mi05ZGQ5YzVkMjQzMGEiLCJpc3MiOiJodHRwczpcL1wvaWFtLXRlc3QuaW5kaWdvLWRhdGFjbG91ZC5ldVwvIiwiaWF0IjoxNDYyOTY4OTg1LCJqdGkiOiIwODJkNjQyNi05MDI5LTQ2NTgtOThhMy01ZGMxZGZlZWUzNWEifQ.XJZc6rQ0b0bb6iMY3sT9FbTTuaiY5jf3TA91VQP2yMTPr29zclm8pOXrBIdtHdWkfx3TzL1BSEvfFNwbcRCZdt7xGD-WegO80Yud5rhfl14ZBayXGlCcxFGrlAi_ASqhTrMveaMPxPo2NlaLctkEweWMmFCzOd2cAaaY8elzAyY";
    String authorizationHeader = "Bearer " + token;

    assertTrue(controller.verifyAuthorization(authorizationHeader));
  }
}
