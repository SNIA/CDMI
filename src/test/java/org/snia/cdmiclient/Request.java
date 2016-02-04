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

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

/**
 * Represents a single CDMI request using a fluent style.
 */
public class Request
{
    private final CDMIClient client;
    private final Method method;

    private String path;
    private String version;
    private String contentType;
    private String accept;
    private byte[] entity;

    public enum Method {
        HEAD(false),
        GET(false),
        PUT(true),
        POST(true),
        DELETE(false);

        private final boolean hasEntity;

        Method(boolean hasEntity)
        {
            this.hasEntity = hasEntity;
        }

        public boolean hasEntity()
        {
            return hasEntity;
        }
    }

    Request(CDMIClient client, Method method, String path)
    {
        if (client == null || method == null || path == null) {
            throw new NullPointerException();
        }
        this.client = client;
        this.method = method;
        this.path = path;
    }

    public Request withAccept(String accept)
    {
        this.accept = accept;
        return this;
    }

    public Request withCDMIVersion(String version)
    {
        this.version = version;
        return this;
    }

    public Request withContentType(String type)
    {
        contentType = type;
        return this;
    }

    public Request withEntity(String entity)
    {
        if (!method.hasEntity()) {
            throw new IllegalArgumentException(method + " requests do not support an entity");
        }

        try {
            this.entity = entity.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported by JVM");
        }
        return this;
    }

    private HttpUriRequest buildRequest()
    {
        if (path == null) {
            throw new IllegalArgumentException("path not specified");
        }

        URI uri = client.buildURI(path);

        switch (method) {
        case HEAD:
            return new HttpHead(uri);
        case DELETE:
            return new HttpDelete(uri);
        case GET:
            return new HttpGet(uri);
        case PUT:
            return new HttpPut(uri);
        case POST:
            return new HttpPost(uri);
        }

        throw new IllegalArgumentException("Unknown method " + method);
    }

    public HttpResponse send() throws IOException
    {
        HttpUriRequest request = buildRequest();
        if (version != null) {
            request.setHeader("X-CDMI-Specification-Version", version);
        }
        if (contentType != null) {
            request.setHeader("Content-Type", contentType);
        }
        if (accept != null) {
            request.setHeader("Accept", accept);
        }
        if (entity != null) {
            ((HttpEntityEnclosingRequest)request).setEntity(new ByteArrayEntity(entity));
        }

        return client.send(request);
    }
}
