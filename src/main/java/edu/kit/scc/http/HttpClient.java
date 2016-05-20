/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.http;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;

/**
 * HTTP client implementation.
 * 
 * @author benjamin
 *
 */
@Component
public class HttpClient {

  private static final Logger log = LoggerFactory.getLogger(HttpClient.class);

  /**
   * Makes a HTTP GET request.
   * 
   * @param url the URL for the request
   * @return a {@link edu.kit.scc.http.HttpResponse} with the request's response code and response
   *         stream as {@link byte[]}
   * 
   */
  public HttpResponse makeHttpGetRequest(String url) {
    return makeHttpGetRequest(null, null, url);
  }

  /**
   * Makes a HTTP GET request with basic authorization.
   * 
   * @param user the user for basic HTTP authorization
   * @param password the user's password for basic HTTP authorization
   * @param url the URL for the request
   * @return a {@link edu.kit.scc.http.HttpResponse} with the request's response code and response
   *         stream as {@link byte[]}
   */
  public HttpResponse makeHttpGetRequest(String user, String password, String url) {
    return makeRequest(CustomUrlConnection.getHttpConnection(url), user, password, null,
        RequestMethod.GET);
  }

  /**
   * Makes a HTTP POST request.
   * 
   * @param body the body for the HTTP POST request
   * @param url the URL for the request
   * @return a {@link edu.kit.scc.http.HttpResponse} with the request's response code and response
   *         stream as {@link byte[]}
   */
  public HttpResponse makeHttpPostRequest(String body, String url) {
    return makeHttpPostRequest(null, null, body, url);
  }

  /**
   * Makes a HTTP POST request with basic authorization.
   * 
   * @param user the user for basic HTTP authorization
   * @param password the user's password for basic HTTP authorization
   * @param body the body for the HTTP POST request
   * @param url the URL for the request
   * @return a {@link edu.kit.scc.http.HttpResponse} with the request's response code and response
   *         stream as {@link byte[]}
   */
  public HttpResponse makeHttpPostRequest(String user, String password, String body, String url) {
    return makeRequest(CustomUrlConnection.getHttpConnection(url), user, password, body,
        RequestMethod.POST);
  }

  /**
   * Makes a HTTPS GET request.
   * 
   * @param url the URL for the request
   * @return a {@link edu.kit.scc.http.HttpResponse} with the request's response code and response
   *         stream as {@link byte[]}
   */
  public HttpResponse makeHttpsGetRequest(String url) {
    return makeHttpsGetRequest(null, null, url);
  }

  /**
   * Makes a HTTPS GET request with basic authorization.
   * 
   * @param user the user for basic HTTP authorization
   * @param password the user's password for basic HTTP authorization
   * @param url the URL for the request
   * @return a {@link edu.kit.scc.http.HttpResponse} with the request's response code and response
   *         stream as {@link byte[]}
   */
  public HttpResponse makeHttpsGetRequest(String user, String password, String url) {
    return makeRequest(CustomUrlConnection.getSecureHttpConnection(false, url), user, password,
        null, RequestMethod.GET);
  }

  /**
   * Makes a HTTPS GET request with bearer authorization.
   * 
   * @param token the token for bearer HTTP authorization
   * @param url the URL for the request
   * @return a {@link edu.kit.scc.http.HttpResponse} with the request's response code and response
   *         stream as {@link byte[]}
   */
  public HttpResponse makeHttpsGetRequest(String token, String url) {
    return makeRequest(CustomUrlConnection.getSecureHttpConnection(false, url), token, null, null,
        RequestMethod.GET);
  }

  /**
   * Makes a HTTPS POST request.
   * 
   * @param body the body for the HTTP POST request
   * @param url the URL for the request
   * @return a {@link edu.kit.scc.http.HttpResponse} with the request's response code and response
   *         stream as {@link byte[]}
   */
  public HttpResponse makeHttpsPostRequest(String body, String url) {
    return makeHttpsPostRequest(null, null, body, url);
  }

  /**
   * Makes a HTTPS POST request with basic authorization.
   * 
   * @param user the user for basic HTTP authorization
   * @param password the user's password for basic HTTP authorization
   * @param body the body for the HTTP POST request
   * @param url the URL for the request
   * @return a {@link edu.kit.scc.http.HttpResponse} with the request's response code and response
   *         stream as {@link byte[]}
   */
  public HttpResponse makeHttpsPostRequest(String user, String password, String body, String url) {
    return makeRequest(CustomUrlConnection.getSecureHttpConnection(false, url), user, password,
        body, RequestMethod.POST);
  }

  private HttpResponse makeRequest(HttpURLConnection urlConnection, String user, String password,
      String body, RequestMethod method) {
    HttpResponse response = null;
    OutputStream out = null;
    InputStream in = null;
    BufferedReader buffReader = null;

    try {
      urlConnection.setRequestMethod(method.toString());
      urlConnection.setRequestProperty("Accept", "*/*");

      if (user != null && !user.isEmpty()) {
        if (password != null && !password.isEmpty()) {
          String value = Base64.encodeBase64String((user + ":" + password).getBytes());
          log.debug("Authorization: Basic {}", value);
          urlConnection.setRequestProperty("Authorization", "Basic " + value);
        } else {
          log.debug("Authorization: Bearer {}", user);
          urlConnection.setRequestProperty("Authorization", "Bearer " + user);
        }
      }

      if (body != null) {
        urlConnection.setDoOutput(true);

        byte[] bodyBytes = body.getBytes("UTF-8");
        out = urlConnection.getOutputStream();
        out.write(bodyBytes);
      }

      urlConnection.connect();
      in = urlConnection.getInputStream();
      buffReader = new BufferedReader(new InputStreamReader(in));
      StringBuffer stringBuffer = new StringBuffer();
      String inputLine;
      while ((inputLine = buffReader.readLine()) != null) {
        stringBuffer.append(inputLine);
      }
      response = new HttpResponse(urlConnection.getResponseCode(), stringBuffer.toString());

    } catch (IOException e) {
      // e.printStackTrace();
      log.error("ERROR {}", e.getMessage());
    } catch (Exception e) {
      // e.printStackTrace();
      log.error("ERROR {}", e.getMessage());
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          // e.printStackTrace();
          log.error("ERROR {}", e.getMessage());
        }
      }
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          // e.printStackTrace();
          log.error("ERROR {}", e.getMessage());
        }
      }
      if (buffReader != null) {
        try {
          buffReader.close();
        } catch (IOException e) {
          // e.printStackTrace();
          log.error("ERROR {}", e.getMessage());
        }
      }
    }
    return response;
  }
}
