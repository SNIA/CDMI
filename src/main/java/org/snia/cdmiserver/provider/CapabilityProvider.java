/*
 * Copyright (c) 2010, Sun Microsystems, Inc. Copyright (c) 2010, The Storage Networking Industry
 * Association.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 * 
 * Neither the name of The Storage Networking Industry Association (SNIA) nor the names of its
 * contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.snia.cdmiserver.provider;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.snia.cdmiserver.model.Capability;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * <p>
 * JSON Serialization/Deserialization for {@link Capability} instances.
 * </p>
 * 
 * @author craigmcc
 */
@Deprecated
public class CapabilityProvider extends AbstractProvider
    implements MessageBodyReader<Capability>, MessageBodyWriter<Capability> {

  // ----------------------------------------------- MessageBodyReader Methods

  @Override
  public boolean isReadable(Class<?> clazz, Type type, Annotation[] annotations,
      MediaType mediaType) {
    return Capability.class.isAssignableFrom(clazz);
  }

  @Override
  public Capability readFrom(Class<Capability> clazz, Type type, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> headers, InputStream in)
      throws IOException, WebApplicationException {
    Capability capabilty = Capability.fromJson(new org.json.JSONObject("{}"));
    try {
      capabilty.getCapabilities().put("cdmi_capability", true);

    } catch (Exception ex) {
      throw new IOException(ex);
    }
    throw new UnsupportedOperationException("Not supported yet.");
  }

  // ----------------------------------------------- MessageBodyWriter Methods

  @Override
  public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations,
      MediaType mediaType) {
    return Capability.class.isAssignableFrom(clazz);
  }

  @Override
  public long getSize(Capability entity, Class<?> clazz, Type type, Annotation[] annotations,
      MediaType mediaTYpe) {
    return -1;
  }

  @Override
  public void writeTo(Capability capability, Class<?> clazz, Type type, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> headers, OutputStream out)
      throws IOException, WebApplicationException {
    JSONObject entity = new JSONObject();
    try {
      if (capability.getObjectType() != null) {
        entity.put("objectType", capability.getObjectType());
      }
      if (capability.getObjectId() != null) {
        entity.put("objectID", capability.getObjectId());
      }
      if (capability.getParentUri() != null) {
        entity.put("parentURI", capability.getParentUri());
      }
      if (capability.getParentId() != null) {
        entity.put("parentID", capability.getParentId());
      }
      if (capability.getCapabilities().length() > 0) {
        entity.put("capabilities", capability.getCapabilities());
      }
      if (capability.getChildren().length() > 0) {
        entity.put("children", capability.getChildren());
      }
      Writer writer = new OutputStreamWriter(out);
      entity.write(writer);
      writer.flush();
    } catch (JSONException e) {
      throw new IOException(e);
    }
  }

}
