/*
 * Copyright (c) 2016, Deutsches Elektronen-Synchrotron (DESY)
 * Copyright (c) 2016, The Storage Networking Industry Association.
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.snia.cdmiclient.CDMIClient;

import static org.snia.cdmiserver.Matchers.*;

import java.net.URISyntaxException;

import static org.junit.Assert.assertThat;
import static org.snia.cdmiclient.Request.Method.DELETE;
import static org.snia.cdmiclient.Request.Method.PUT;
import static org.snia.cdmiserver.ServerContext.given;

/**
 *  These tests check various container operations.
 */
public class ContainerTests
{
    private CDMIClient client;
    private ServerContext server;

    @Before
    public void setup() throws URISyntaxException
    {
        client = new CDMIClient("http://localhost:8080/");
        client.setRequestVersion("1.0.2");
        client.addRequestObserver(new Eraser());
        server = new ServerContext(client);
    }

    @After
    public void tearDown()
    {
        client.close();
    }

    @Test
    public void shouldCreateContainer() throws Exception
    {
        HttpResponse response = client.request(PUT, "/TestContainer")
                .withContentType("application/cdmi-container")
                .withEntity("{ \"metadata\" : { } }")
                .send();


        assertThat(response.getStatusLine(), hasStatusCode(201));

        Header[] headers = response.getAllHeaders();
        assertThat(headers, hasHeader("X-CDMI-Specification-Version", "1.0.2"));
        assertThat(headers, hasHeader("Location", client.buildURI("/TestContainer")));
        assertThat(headers, hasHeader("Content-Type", "application/cdmi-container"));

        HttpEntity entity = response.getEntity();
        assertThat(entity, hasJsonStringAt("$.objectID"));
        assertThat(entity, hasJsonObjectAt("$.exports"));
        assertThat(entity, hasJsonValueAt("$.objectType").of("application/cdmi-container"));
        assertThat(entity, hasJsonListAt("$.children"));
        assertThat(entity, hasJsonValueAt("$.parentURI").of("/"));
        assertThat(entity, hasJsonValueAt("$.completionStatus").of("Complete"));
    }

    @Test
    public void shouldUpdateContainer() throws Exception
    {
        given(server.hasContainer("/TestContainer/"));


        HttpResponse response = client.request(PUT, "/TestContainer/")
                .withContentType("application/cdmi-container")
                .withEntity("{ \"metadata\" : { } }")
                .send();


        assertThat(response.getStatusLine(), hasStatusCode(201));

        Header[] headers = response.getAllHeaders();
        assertThat(headers, hasHeader("X-CDMI-Specification-Version", "1.0.2"));
        assertThat(headers, hasHeader("Location", client.buildURI("/TestContainer/")));
        assertThat(headers, hasHeader("Content-Type", "application/cdmi-container"));

        HttpEntity entity = response.getEntity();
        assertThat(entity, hasJsonStringAt("$.objectID"));
        assertThat(entity, hasJsonWithNullAt("$.capabilitiesURI"));
        assertThat(entity, hasJsonEmptyObjectAt("$.exports"));
        assertThat(entity, hasJsonValueAt("$.objectType").of("application/cdmi-container"));
        assertThat(entity, hasJsonValueAt("$.parentURI").of("/"));
        assertThat(entity, hasJsonEmptyListAt("$.children"));
        assertThat(entity, hasJsonValueAt("$.completionStatus").of("Complete"));
    }


    @Test
    public void shouldDeleteContainer() throws Exception
    {
        given(server.hasContainer("/TestContainer/"));


        HttpResponse response = client.request(DELETE, "/TestContainer/")
                .withContentType("application/cdmi-container")
                .send();


        assertThat(response.getStatusLine(), hasStatusCode(200));

        Header[] headers = response.getAllHeaders();
        assertThat(headers, hasHeader("Content-Length", "0"));
        assertThat(headers, hasHeader("X-CDMI-Specification-Version", "1.0.2"));
    }
}
