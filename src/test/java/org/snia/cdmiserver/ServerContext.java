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

import org.apache.http.HttpResponse;
import org.snia.cdmiclient.CDMIClient;
import org.springframework.util.Assert;

import java.io.IOException;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.snia.cdmiclient.Request.Method.PUT;
import static org.snia.cdmiserver.Matchers.hasStatusCode;

/**
 * A class to support establishing the context necessary for a
 * test.
 */
public class ServerContext
{
    private final CDMIClient client;

    public ServerContext(CDMIClient client)
    {
        this.client = client;
    }

    public boolean hasContainer(String path) throws IOException
    {
        boolean isSuccessful = true;

        StringBuilder sb = new StringBuilder().append('/');
        for (String element : path.split("/")) {
            if (element.isEmpty()) {
                continue;
            }
            sb.append(element).append('/');

            HttpResponse response = client.request(PUT, sb.toString())
                .withContentType("application/cdmi-container")
                .withEntity("{ \"metadata\" : { } }")
                .send();
            boolean success = response.getStatusLine().getStatusCode() == 201;
            isSuccessful &= success;
        }

        return isSuccessful;
    }

    public boolean hasDataObject(String path, String value) throws IOException
    {
        boolean isSuccessful = true;

        StringBuilder sb = new StringBuilder().append('/');
        for (String element : path.split("/")) {
            if (element.isEmpty()) {
                continue;
            }

            sb.append(element);
            String thisPath = sb.toString();

            boolean success;
            if (thisPath.equals(path)) {
                HttpResponse response = client.request(PUT, path)
                        .withContentType("application/cdmi-object")
                        .withEntity("{\n" +
                                        "\"mimetype\" : \"text/plain\",\n" +
                                        "\"value\" : \"" + value + "\"\n" +
                                    "}\n")
                        .send();
                 success = response.getStatusLine().getStatusCode() == 201;
            } else {
                sb.append('/');
                HttpResponse response = client.request(PUT, sb.toString())
                    .withContentType("application/cdmi-container")
                    .withEntity("{ \"metadata\" : { } }")
                    .send();
                 success = response.getStatusLine().getStatusCode() == 201;
            }

            isSuccessful &= success;
        }

        return isSuccessful;
    }

    boolean hasUpdatedDataObject(String path, String value) throws IOException
    {
        HttpResponse update = client.request(PUT, path)
                .withContentType("application/cdmi-object")
                .withEntity("{\n" +
                                "\"mimetype\" : \"text/plain\",\n" +
                                "\"value\" : \"" + value + "\"\n" +
                            "}\n")
                .send();

        return update.getStatusLine().getStatusCode() == 200;
    }

    public static void given(boolean successful)
    {
        assertTrue("Failed to establishing context for test", successful);
    }
}
