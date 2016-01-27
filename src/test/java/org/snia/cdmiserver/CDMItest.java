/*
 * Copyright (c) 2010, Oracle
 * Copyright (c) 2010, The Storage Networking Industry Association.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of The Storage Networking Industry Association (SNIA) nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 *  THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.snia.cdmiserver;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Mark A. Carlson
 */
public class CDMItest
{
    private HttpClient httpclient;

    @Before
    public void setup()
    {
        httpclient = new DefaultHttpClient();
    }

    @Test
    public void testCapabilities() throws Exception
    {
        // Create the request
        HttpResponse response = null;
        HttpGet httpget = new HttpGet("http://localhost:8080/cdmi_capabilities");
        httpget.setHeader("Accept", "application/cdmi-capability");
        httpget.setHeader("X-CDMI-Specification-Version", "1.0.2");
        response = httpclient.execute(httpget);

        Header[] hdr = response.getAllHeaders();
        System.out.println("Headers : " + hdr.length);
        for (int i = 0; i < hdr.length; i++) {
            System.out.println(hdr[i]);
        }
        System.out.println("---------");
        System.out.println(response.getProtocolVersion());
        System.out.println(response.getStatusLine().getStatusCode());
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());

        System.out.println(response.getStatusLine().getReasonPhrase());
        System.out.println(response.getStatusLine().toString());
        System.out.println("---------");
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            long len = entity.getContentLength();
            if (len != -1 && len < 2048) {
                System.out.println(EntityUtils.toString(entity));
            }
        }
    }

    @Test
    public void testContainerCreate() throws Exception
    {
        // Create the request
        HttpResponse response = null;
        HttpPut httpput = new HttpPut("http://localhost:8080/TestContainer-a/");
        httpput.setHeader("Content-Type", "application/cdmi-container");
        httpput.setHeader("X-CDMI-Specification-Version", "1.0.2");
        httpput.setEntity(new StringEntity("{ \"metadata\" : { } }"));
        response = httpclient.execute(httpput);

        Header[] hdr = response.getAllHeaders();
        System.out.println("Headers : " + hdr.length);
        for (int i = 0; i < hdr.length; i++) {
            System.out.println(hdr[i]);
        }
        System.out.println("---------");
        System.out.println(response.getProtocolVersion());
        System.out.println(response.getStatusLine().getStatusCode());
        Assert.assertEquals(201, response.getStatusLine().getStatusCode());

        System.out.println(response.getStatusLine().getReasonPhrase());
        System.out.println(response.getStatusLine().toString());
        System.out.println("---------");
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            long len = entity.getContentLength();
            if (len != -1 && len < 2048) {
                System.out.println(EntityUtils.toString(entity));
            }
        }

        silentlyDeleteContainer("/TestContainer-a");
    }

    @Test
    public void testContainerUpdate() throws Exception
    {
        createContainer("/TestContainer-b/");

        // Create the request
        HttpResponse response = null;
        HttpPut httpput = new HttpPut("http://localhost:8080/TestContainer-b/");
        httpput.setHeader("Content-Type", "application/cdmi-container");
        httpput.setHeader("X-CDMI-Specification-Version", "1.0.2");
        httpput.setEntity(new StringEntity("{ \"metadata\" : { } }"));
        response = httpclient.execute(httpput);

        Header[] hdr = response.getAllHeaders();
        System.out.println("Headers : " + hdr.length);
        for (int i = 0; i < hdr.length; i++) {
            System.out.println(hdr[i]);
        }
        System.out.println("---------");
        System.out.println(response.getProtocolVersion());
        System.out.println(response.getStatusLine().getStatusCode());
        Assert.assertEquals(201, response.getStatusLine().getStatusCode());

        System.out.println(response.getStatusLine().getReasonPhrase());
        System.out.println(response.getStatusLine().toString());
        System.out.println("---------");
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            long len = entity.getContentLength();
            if (len != -1 && len < 2048) {
                System.out.println(EntityUtils.toString(entity));
            }
        }

        silentlyDeleteContainer("/TestContainer-b");
    }

    @Test
    public void testObjectCreate() throws Exception
    {
        createContainer("/TestContainer-c/");

        // Create the request
        HttpResponse response = null;

        HttpPut httpput = new HttpPut(
                "http://localhost:8080/TestContainer-c/TestObject.txt");
        httpput.setHeader("Content-Type", "application/cdmi-object");
        httpput.setHeader("X-CDMI-Specification-Version", "1.0.2");
        String respStr = "{\n";
        respStr = respStr + "\"mimetype\" : \"" + "text/plain" + "\",\n";
        respStr = respStr + "\"value\" : \"" + "This is a test" + "\"\n";
        respStr = respStr + "}\n";
        System.out.println(respStr);
        StringEntity entity = new StringEntity(respStr);
        httpput.setEntity(entity);
        response = httpclient.execute(httpput);
        response.getEntity().consumeContent();

        Header[] hdr = response.getAllHeaders();
        System.out.println("Headers : " + hdr.length);
        for (int i = 0; i < hdr.length; i++) {
            System.out.println(hdr[i]);
        }
        System.out.println("---------");
        System.out.println(response.getProtocolVersion());
        System.out.println(response.getStatusLine().getStatusCode());
        Assert.assertEquals(201, response.getStatusLine().getStatusCode());

        System.out.println(response.getStatusLine().getReasonPhrase());
        System.out.println(response.getStatusLine().toString());
        System.out.println("---------");

        silentlyDeleteDataObject("/TestContainer-c/TestObject.txt");
        silentlyDeleteContainer("/TestContainer-c");
    }

    @Test
    public void testObjectUpdate() throws Exception
    {
        createContainer("/TestContainer-d/");
        createDataObject("/TestContainer-d/TestObject.txt");

        // Create the request
        HttpResponse response = null;

        HttpPut httpput = new HttpPut(
                "http://localhost:8080/TestContainer-d/TestObject.txt");
        httpput.setHeader("Content-Type", "application/cdmi-object");
        httpput.setHeader("X-CDMI-Specification-Version", "1.0.2");
        String respStr = "{\n";
        respStr = respStr + "\"mimetype\" : \"" + "text/plain" + "\",\n";
        respStr = respStr + "\"value\" : \"" + "This is a new test" + "\"\n";
        respStr = respStr + "}\n";
        System.out.println(respStr);
        StringEntity entity = new StringEntity(respStr);
        httpput.setEntity(entity);
        response = httpclient.execute(httpput);
        response.getEntity().consumeContent();

        Header[] hdr = response.getAllHeaders();
        System.out.println("Headers : " + hdr.length);
        for (int i = 0; i < hdr.length; i++) {
            System.out.println(hdr[i]);
        }
        System.out.println("---------");
        System.out.println(response.getProtocolVersion());
        System.out.println(response.getStatusLine().getStatusCode());
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());

        System.out.println(response.getStatusLine().getReasonPhrase());
        System.out.println(response.getStatusLine().toString());
        System.out.println("---------");

        silentlyDeleteDataObject("/TestContainer-d/TestObject.txt");
        silentlyDeleteContainer("/TestContainer-d");
    }

    @Test
    public void testObjectDelete() throws Exception
    {
        createContainer("/TestContainer-e/");
        createDataObject("/TestContainer-e/TestObject.txt");

        // Create the request
        HttpResponse response = null;

        HttpDelete httpdelete = new HttpDelete(
                "http://localhost:8080/TestContainer-e/TestObject.txt");
        httpdelete.setHeader("Content-Type", "application/cdmi-object");
        httpdelete.setHeader("X-CDMI-Specification-Version", "1.0.2");
        response = httpclient.execute(httpdelete);
        response.getEntity().consumeContent();

        Header[] hdr = response.getAllHeaders();
        System.out.println("Headers : " + hdr.length);
        for (int i = 0; i < hdr.length; i++) {
            System.out.println(hdr[i]);
        }
        System.out.println("---------");
        System.out.println(response.getProtocolVersion());
        System.out.println(response.getStatusLine().getStatusCode());
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());

        System.out.println(response.getStatusLine().getReasonPhrase());
        System.out.println(response.getStatusLine().toString());
        System.out.println("---------");

        silentlyDeleteContainer("/TestContainer-e");
    }

    @Test
    public void testContainerDelete() throws Exception
    {
        createContainer("/TestContainer-f/");

        // Create the request
        HttpResponse response = null;

        HttpDelete httpdelete = new HttpDelete(
                "http://localhost:8080/TestContainer-f");
        httpdelete.setHeader("Content-Type", "application/cdmi-container");
        httpdelete.setHeader("X-CDMI-Specification-Version", "1.0.2");
        response = httpclient.execute(httpdelete);

        Header[] hdr = response.getAllHeaders();
        System.out.println("Headers : " + hdr.length);
        for (int i = 0; i < hdr.length; i++) {
            System.out.println(hdr[i]);
        }
        System.out.println("---------");
        System.out.println(response.getProtocolVersion());
        System.out.println(response.getStatusLine().getStatusCode());
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());

        System.out.println(response.getStatusLine().getReasonPhrase());
        System.out.println(response.getStatusLine().toString());
        System.out.println("---------");
    }

    private void createContainer(String path) throws UnsupportedEncodingException, IOException
    {
        HttpPut request = new HttpPut("http://localhost:8080" + path);
        request.setHeader("Content-Type", "application/cdmi-container");
        request.setHeader("X-CDMI-Specification-Version", "1.0.2");
        request.setEntity(new StringEntity("{ \"metadata\" : { } }"));
        HttpResponse createContainerResponse = httpclient.execute(request);
        createContainerResponse.getEntity().consumeContent();

        assertThat(createContainerResponse.getStatusLine().getStatusCode(), is(equalTo(201)));
    }

    private void createDataObject(String path) throws UnsupportedEncodingException, IOException
    {
        HttpPut request = new HttpPut("http://localhost:8080" + path);
        request.setHeader("Content-Type", "application/cdmi-object");
        request.setHeader("X-CDMI-Specification-Version", "1.0.2");
        request.setEntity(new StringEntity("{\n" +
                "\"mimetype\" : \"text/plain\",\n" +
                "\"value\" : \"This is a test\"\n" +
                "}\n"));
        HttpResponse createObjectResponse = httpclient.execute(request);
        createObjectResponse.getEntity().consumeContent();

        assertThat(createObjectResponse.getStatusLine().getStatusCode(), is(equalTo(201)));
    }


    private void silentlyDeleteContainer(String path)
    {
        silentlyDelete(path, "application/cdmi-container");
    }

    private void silentlyDeleteDataObject(String path)
    {
        silentlyDelete(path, "application/cdmi-dataobject");
    }

    private void silentlyDelete(String path, String type)
    {
        HttpDelete request = new HttpDelete("http://localhost:8080" + path);
        request.setHeader("Content-Type", type);
        request.setHeader("X-CDMI-Specification-Version", "1.0.2");
        try {
            httpclient.execute(request).getEntity().consumeContent();
        } catch (IOException e) {
            // Ignore any problems: this is only best-effort at deleting content.
        }
    }
}
