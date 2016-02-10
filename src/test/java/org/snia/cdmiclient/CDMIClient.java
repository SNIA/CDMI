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
package org.snia.cdmiclient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.snia.cdmiclient.Request.Method.PUT;
import static org.snia.cdmiserver.Matchers.hasStatusCode;


/**
 * This class provides minimal support for a CDMI client to allow
 * functional testing.
 */
public class CDMIClient implements Closeable
{
    private final HttpClient httpclient = new DefaultHttpClient();

    private final URI endpoint;
    private final List<RequestObserver> observers = new ArrayList<RequestObserver>();

    private String version;

    private HttpResponse currentResponse;

    public CDMIClient(String endpoint) throws URISyntaxException
    {
        this.endpoint = new URI(endpoint);
    }

    public URI getEndpoint()
    {
        return endpoint;
    }

    public void setRequestVersion(String version)
    {
        this.version = version;
    }

    public void addRequestObserver(RequestObserver observer)
    {
        if (observer instanceof ClientAware) {
            ((ClientAware)observer).setClient(this);
        }

        observers.add(observer);
    }

    public Request request(Request.Method method, String path)
    {
        return new Request(this, method, path).
                withCDMIVersion(version);
    }

    HttpResponse send(HttpUriRequest request) throws IOException
    {
        finaliseCurrentResponse();
        HttpResponse r = httpclient.execute(request);
        HttpEntity wrapped = new BufferedHttpEntity(r.getEntity()) {
            @Override
            public String toString()
            {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                try {
                    this.writeTo(os);
                } catch (IOException e) {
                    return e.toString();
                }
                try {
                    return os.toString("UTF8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("UTF-8 not supported in JVM");
                }
            }
        };
        r.setEntity(wrapped);
        for (RequestObserver observer : observers) {
            observer.acceptActivity(request, r);
        }
        currentResponse = r;
        return r;
    }

    public URI buildURI(String path)
    {
        try {
            String resolvedPath;

            if (endpoint.getPath().equals("/")) {
                resolvedPath = path;
            } else {
                String prefix = endpoint.getPath();
                if (prefix.endsWith("/")) {
                    prefix = prefix.substring(0, prefix.length()-1);
                }
                if (path.startsWith("/")) {
                    resolvedPath = prefix + path;
                } else {
                    resolvedPath = prefix + "/" + path;
                }
            }

            return new URI(endpoint.getScheme(), null, endpoint.getHost(),
                    endpoint.getPort(), resolvedPath, null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Broken URI: " + e.getMessage());
        }
    }

    public String relativize(URI request)
    {
        return "/" + endpoint.relativize(request).getPath();
    }


    private void finaliseCurrentResponse()
    {
        if (currentResponse != null) {
            try {
                currentResponse.getEntity().consumeContent();
            } catch (IOException e) {
                // Ignore, although likely next request will also fail.
            }
        }
        currentResponse = null;
    }

    @Override
    public void close()
    {
        for (RequestObserver observer : observers) {
            observer.noFurtherActivity();
        }

        finaliseCurrentResponse();
        httpclient.getConnectionManager().shutdown();
    }
}
