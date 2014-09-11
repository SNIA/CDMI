/*
 * Copyright (c) 2010, Sun Microsystems, Inc.
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

package org.snia.cdmiserver.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * <p>
 * Convenience base class for providers performing JSON reading.
 * </p>
 */
public abstract class AbstractProvider {

    protected List<String> convertArray(JSONArray entity) throws IOException {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < entity.length(); i++) {
            try {
                list.add(entity.getString(i));
            } catch (JSONException e) {
                throw new IOException(e);
            }
        }
        return list;
    }

    protected Map<String, Object> convertHash(JSONObject entity) throws IOException {
        Map<String, Object> map = new HashMap<String, Object>();
        Iterator names = entity.keys();
        while (names.hasNext()) {
            String name = (String) names.next();
            try {
                JSONObject value = entity.getJSONObject(name);
                if (value.length() > 0) {
                    map.put(name, convertHash(value));
                } else {
                    map.put(name, value.get(name));
                }
            } catch (JSONException e) {
                throw new IOException(e);
            }
        }
        return map;
    }

    protected JSONObject convertJSON(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(in);
        while (true) {
            int ch = reader.read();
            if (ch < 0) {
                break;
            }
            sb.append((char) ch);
        }
        try {
            return new JSONObject(sb.toString());
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    protected Map<String, String> convertMap(JSONObject entity) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        Iterator names = entity.keys();
        while (names.hasNext()) {
            String name = (String) names.next();
            try {
                map.put(name, entity.getString(name));
            } catch (JSONException e) {
                throw new IOException(e);
            }
        }
        return map;
    }

}
