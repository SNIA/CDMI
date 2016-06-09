/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Custom SSL context generation class.
 * 
 * @author benjamin
 *
 */
public final class CustomSslContext {

  private CustomSslContext() {}

  /**
   * Initializes a SSL context with the given certificate.
   * 
   * @param cert the X.509 certificate for the context
   * @return a {@link SSLContext}
   */
  public static SSLContext initSslContextWithCertificate(String cert) {

    SSLContext sslContext = null;
    InputStream in = null;
    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");

      in = new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8));
      // in = new FileInputStream("saml-delegation.data.kit.edu");
      Certificate ca = cf.generateCertificate(in);

      String keyStoreType = KeyStore.getDefaultType();
      KeyStore keystore = KeyStore.getInstance(keyStoreType);
      keystore.load(null, null);
      keystore.setCertificateEntry("ca", ca);

      String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
      tmf.init(keystore);

      sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, tmf.getTrustManagers(), null);

    } catch (IOException e) {
      e.printStackTrace();
    } catch (CertificateException e) {
      e.printStackTrace();
    } catch (KeyStoreException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (KeyManagementException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return sslContext;
  }

  /**
   * Initializes an empty SSL context.
   * 
   * @return a {@link SSLContext}
   */
  public static SSLContext initEmptySslContext() {
    SSLContext sslContext = null;
    try {
      sslContext = SSLContext.getInstance("TLS");

      sslContext.init(null, new TrustManager[] {new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {}

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {}
      } }, null);
    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (KeyManagementException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return sslContext;
  }
}
