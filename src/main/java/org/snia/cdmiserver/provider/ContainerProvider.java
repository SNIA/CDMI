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
import org.snia.cdmiserver.model.Container;

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
 * JSON Serialization/Deserialization for {@link Container} instances.
 * </p>
 * 
 * @author craigmcc
 */
@Deprecated
public class ContainerProvider extends AbstractProvider
    implements MessageBodyReader<Container>, MessageBodyWriter<Container> {

  // ----------------------------------------------- MessageBodyReader Methods

  @Override
  public boolean isReadable(Class<?> clazz, Type type, Annotation[] annotations,
      MediaType mediaType) {
    return Container.class.isAssignableFrom(clazz);
  }

  @Override
  public Container readFrom(Class<Container> clazz, Type type, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> headers, InputStream in)
      throws IOException, WebApplicationException {
    JSONObject entity = convertJson(in);
    Container container = Container.fromJson(new org.json.JSONObject("{}"));
    try {
      if (entity.has("copy")) {
        container.setCopy(entity.getString("copy"));
      }
      if (entity.has("move")) {
        container.setMove(entity.getString("move"));
      }
      if (entity.has("reference")) {
        container.setReference(entity.getString("reference"));
      }
    } catch (JSONException e) {
      throw new IOException(e);
    }
    throw new UnsupportedOperationException("Not supported yet.");
  }

  // ----------------------------------------------- MessageBodyWriter Methods

  @Override
  public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations,
      MediaType mediaType) {
    return Container.class.isAssignableFrom(clazz);
  }

  @Override
  public long getSize(Container entity, Class<?> clazz, Type type, Annotation[] annotations,
      MediaType mediaTYpe) {
    return -1;
  }

  @Override
  public void writeTo(Container container, Class<?> clazz, Type type, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> headers, OutputStream out)
      throws IOException, WebApplicationException {
    JSONObject entity = new JSONObject();
    try {
      if (container.getObjectId() != null) {
        entity.put("objectID", container.getObjectId());
      }
      if (container.getParentUri() != null) {
        entity.put("parentURI", container.getParentUri());
      }
      if (container.getDomainUri() != null) {
        entity.put("domainURI", container.getDomainUri());
      }
      if (container.getCapabilitiesUri() != null) {
        entity.put("capabilitiesURI", container.getCapabilitiesUri());
      }
      if (container.getCompletionStatus() != null) {
        entity.put("completionStatus", container.getCompletionStatus());
      }
      if (container.getPercentComplete() != null) {
        entity.put("percentComplete", container.getPercentComplete());
      }
      if (container.getMetadata().length() > 0) {
        entity.put("metadata", container.getMetadata());
      }
      if (container.getExports().length() > 0) {
        entity.put("exports", container.getExports());
      }
      if (container.getChildrenrange() != null) {
        entity.put("childrenrange", container.getChildrenrange());
      }
      if (container.getChildren().length() > 0) {
        entity.put("children", container.getChildren());
      }
      Writer writer = new OutputStreamWriter(out);
      entity.write(writer);
      writer.flush();
    } catch (JSONException e) {
      throw new IOException(e);
    }
  }

}
