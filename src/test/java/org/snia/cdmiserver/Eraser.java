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
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.snia.cdmiclient.CDMIClient;
import org.snia.cdmiclient.ClientAware;
import org.snia.cdmiclient.RequestObserver;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.snia.cdmiclient.Request.Method.DELETE;

/**
 * This class observes CDMI creation and deletion operations and can
 * remove any left over content.
 */
public class Eraser implements RequestObserver, ClientAware
{
    private final SortedMap<String,String> items = new TreeMap<String,String>(new Comparator<String>(){
                @Override
                public int compare(String o1, String o2)
                {
                    if (o1.equals(o2)) {
                        return 0;
                    }
                    if (o1.startsWith(o2)) {
                        return -1;
                    }
                    if (o2.startsWith(o1)) {
                        return 1;
                    }
                    return o1.compareTo(o2);
                }
            });

    private CDMIClient client;
    private boolean removing;

    @Override
    public void setClient(CDMIClient client)
    {
        this.client = client;
    }

    @Override
    public void acceptActivity(HttpUriRequest request, HttpResponse response)
    {
        int code = response.getStatusLine().getStatusCode();

        if (!removing && (code == 200 || code == 201)) {
            String path = client.relativize(request.getURI());

            String method = request.getMethod();
            if (method.equals("DELETE")) {
                acceptDeleteActivity(path, getHeader(request, "Content-Type"));
            } else if (method.equals("PUT")) {
                acceptPutActivity(path, getHeader(request, "Content-Type"));
            }
        }
    }

    private String getHeader(HttpUriRequest request, String name)
    {
        Header header = request.getLastHeader(name);
        if (header == null) {
            throw new IllegalArgumentException("Missing " + name + " + in " + request.getMethod());
        }
        return header.getValue();
    }

    private void acceptDeleteActivity(String path, String type)
    {
        String storedType = items.remove(path);
        if (storedType == null) {
            System.out.println("Successful DELETE of unknown path: " + path);
        }
        if (!type.equals(storedType)) {
            System.out.println("Successful DELETE of different type: " +
                    type + " != " + storedType);
        }
    }

    private void acceptPutActivity(String path, String type)
    {
        items.put(path, type);
    }

    @Override
    public void noFurtherActivity()
    {
        removing = true;

        for (Map.Entry<String,String> item : items.entrySet()) {
            String path = item.getKey();
            String type = item.getValue();
            try {
                client.request(DELETE, path).withContentType(type).send();
            } catch (IOException e) {
                System.out.println("Failed to delete " + type + " object " +
                        path + ": " + e.toString());
            }
        }

        removing = false;
        items.clear();
    }
}
