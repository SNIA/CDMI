/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.auth;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import edu.kit.scc.http.HttpClient;
import edu.kit.scc.http.HttpResponse;

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

    String authorizationMethod = authorizationHeader.split(" ")[0];
    String encodedCredentials = authorizationHeader.split(" ")[1];

    if (!authorizationMethod.equals("Bearer")) {
      // not a token
      return false;
    }

    try {

      Jwt jsonWebToken = JwtHelper.decode(encodedCredentials);
      log.debug("jsonWebToken: {}", jsonWebToken);

      JSONObject claims = new JSONObject(jsonWebToken.getClaims());

      Date expiration = new Date(claims.getLong("exp") * 1000); // exp is in seconds
      log.debug("Expiration date: {}", expiration);

      if (expiration.before(new Date())) {
        log.info("Token expired at {}", expiration);
        return false;
      }

      String principal = claims.getString("sub");
      AbstractAuthenticationToken auth = null;
      JSONObject authDetails = new JSONObject();
      Collection<? extends GrantedAuthority> authorities = null;

      // get introspection endpoint data
      String body = "token=" + encodedCredentials;
      HttpResponse response =
          httpClient.makeHttpsPostRequest(clientId, clientSecret, body, tokenInfo);

      if (response == null) {
        log.error("Null response received from {}", tokenInfo);
        return false;
      }

      if (response.getStatusCode() == HttpStatus.OK.value()) {

        log.debug("Token info {}", response.getResponseString());
        authDetails.put("tokeninfo", new JSONObject(response.getResponseString()));

        authorities = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_CLIENT");

      } else {

        log.info("Introspection endpoint response: {}", response.getResponseString());
        return false;
      }

      // get user info
      response = httpClient.makeHttpsGetRequest(encodedCredentials, userInfo);

      if (response == null) {
        log.error("Null response received from {}", userInfo);
        return false;
      }

      if (response.statusCode == HttpStatus.OK.value()) {

        log.debug("User info {}", response.getResponseString());
        authDetails.put("userinfo", new JSONObject(response.getResponseString()));

        authorities = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER");

      }

      auth = new UsernamePasswordAuthenticationToken(principal, encodedCredentials, authorities);
      auth.setDetails(authDetails);

      SecurityContextHolder.getContext().setAuthentication(auth);

    } catch (Throwable ex) {

      log.error("ERROR {}", ex.toString());
      return false;
    }

    return true;
  }
}
