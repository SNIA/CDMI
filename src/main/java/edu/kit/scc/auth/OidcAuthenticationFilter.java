/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.auth;

import edu.kit.scc.http.HttpClient;
import edu.kit.scc.http.HttpResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class OidcAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(OidcAuthenticationFilter.class);

  @Value("${oidc.tokeninfo}")
  private String tokenInfo;

  @Value("${oidc.userinfo}")
  private String userInfo;

  @Value("${oidc.clientid}")
  private String clientId;

  @Value("${oidc.clientsecret}")
  private String clientSecret;

  @Autowired
  HttpClient httpClient;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String authorizationHeader = request.getHeader("Authorization");
    log.debug("Authorization: {}", authorizationHeader);
    log.debug("from {}", request.getRemoteAddr());

    verifyAuthorization(authorizationHeader);

    filterChain.doFilter(request, response);
  }

  /**
   * Verifies the authorization according to the authorization header.
   * 
   * @param authorizationHeader the authorization header
   * @return true if authorized
   */
  public boolean verifyAuthorization(String authorizationHeader) {
    try {
      String authorizationMethod = authorizationHeader.split(" ")[0];
      String encodedCredentials = authorizationHeader.split(" ")[1];

      if (authorizationMethod.equals("Bearer")) {
        // check for user token
        HttpResponse response = httpClient.makeHttpsGetRequest(encodedCredentials, userInfo);
        if (response != null && response.statusCode == HttpStatus.OK.value()) {
          log.debug("User info {}", response.getResponseString());
          String user = "user";

          JSONObject json = new JSONObject(response.getResponseString());
          user = json.optString("sub", "user");

          SecurityContextHolder.getContext()
              .setAuthentication(new UsernamePasswordAuthenticationToken(user, encodedCredentials,
                  AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER")));

          return true;
        }
        // check for client token
        String body = "token=" + encodedCredentials;
        response = httpClient.makeHttpsPostRequest(clientId, clientSecret, body, tokenInfo);
        if (response.statusCode == HttpStatus.OK.value()) {
          log.debug("Token info {}", response.getResponseString());

          SecurityContextHolder.getContext().setAuthentication(
              new UsernamePasswordAuthenticationToken("client", encodedCredentials,
                  AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_CLIENT")));

          return true;
        }
      }
    } catch (Exception ex) {
      log.error("ERROR {}", ex.toString());
      // ex.printStackTrace();
    }
    return false;
  }
}
