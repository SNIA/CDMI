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

package org.snia.cdmiserver.model;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.snia.cdmiserver.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Representation of a CDMI <em>DataObject</em>.
 * </p>
 */
public class DataObject {
    private static final Logger LOG = LoggerFactory.getLogger(DataObject.class);

    // DataObject creation fields
    private String mimetype;
    private Map<String, String> metadata = new HashMap<String, String>();
    private String deserialize;
    private String serialize;
    private String copy;
    private String move;
    private String reference;
    private String value;
    private byte[] binaryValue;
    // DataObject representation fields
    private String objectType;
    private String objectID;
    private String parentURI;
    private String accountURI;
    private String capabilitiesURI;
    private String completionStatus;
    private Integer percentComplete; // FIXME - Specification says String but that does not make
                                     // sense
    private String valuerange;

    // Representation also includes "mimetype", "metadata", and "value" from creation fields
    //

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(String key, String val) {
        metadata.put(key, val);
    }

    public String getDeserialize() {
        return deserialize;
    }

    public void setDeserialize(String deserialize) {
        this.deserialize = deserialize;
    }

    public String getSerialize() {
        return serialize;
    }

    public void setSerialize(String serialize) {
        this.serialize = serialize;
    }

    public String getCopy() {
        return copy;
    }

    public void setCopy(String copy) {
        this.copy = copy;
    }

    public String getMove() {
        return move;
    }

    public void setMove(String move) {
        this.move = move;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectURI) {
        this.objectType = objectURI;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getParentURI() {
        return parentURI;
    }

    public void setParentURI(String parentURI) {
        this.parentURI = parentURI;
    }

    public String getAccountURI() {
        return accountURI;
    }

    public void setAccountURI(String accountURI) {
        this.accountURI = accountURI;
    }

    public String getCapabilitiesURI() {
        return capabilitiesURI;
    }

    public void setCapabilitiesURI(String capabilitiesURI) {
        this.capabilitiesURI = capabilitiesURI;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }

    public Integer getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(Integer percentComplete) {
        this.percentComplete = percentComplete;
    }

    public String getValuerange() {
        return valuerange;
    }

    public void setValuerange(String valuerange) {
        this.valuerange = valuerange;
    }

    public String toJson() throws Exception {
        //
        StringWriter outBuffer = new StringWriter();
        try {
            JsonFactory f = new JsonFactory();
            JsonGenerator g = f.createJsonGenerator(outBuffer);
            g.useDefaultPrettyPrinter();
            g.writeStartObject();
            //
            if (objectType != null)
                g.writeStringField("objectType", objectType);
            if (capabilitiesURI != null)
                g.writeStringField("capabilitiesURI", capabilitiesURI);
            if (objectID != null)
                g.writeStringField("objectID", objectID);
            if (mimetype != null)
                g.writeStringField("mimetype", mimetype);
            //
            g.writeObjectFieldStart("metadata");
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                g.writeStringField(entry.getKey(), entry.getValue());
            }
            g.writeEndObject();
            //
            if (value != null)
                g.writeStringField("valueRange", value.length() + "");
            if (value != null)
                g.writeStringField("value", value);
            //
            g.writeEndObject();
            g.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
            // return ("Error : " + ex);
        }
        //
        return outBuffer.toString();
    }

    public String metadataToJson() throws Exception {
        //
        StringWriter outBuffer = new StringWriter();
        try {
            JsonFactory f = new JsonFactory();
            JsonGenerator g = f.createJsonGenerator(outBuffer);
            g.useDefaultPrettyPrinter();
            g.writeStartObject();
            // get top level metadata
            if (objectType != null)
                g.writeStringField("objectType", objectType);
            if (capabilitiesURI != null)
                g.writeStringField("capabilitiesURI", capabilitiesURI);
            if (objectID != null)
                g.writeStringField("objectID", objectID);
            if (mimetype != null)
                g.writeStringField("mimetype", mimetype);
            //
            g.writeObjectFieldStart("metadata");
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                g.writeStringField(entry.getKey(), entry.getValue());
            }
            g.writeEndObject();
            //
            g.writeEndObject();
            g.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
            // return ("Error : " + ex);
        }
        //
        return outBuffer.toString();
    }

    public void fromJson(InputStream jsonIs, boolean fromFile) throws Exception {
        JsonFactory f = new JsonFactory();
        JsonParser jp = f.createJsonParser(jsonIs);
        fromJson(jp, fromFile);
    }

    public void fromJson(byte[] jsonBytes, boolean fromFile) throws Exception {
        JsonFactory f = new JsonFactory();
        JsonParser jp = f.createJsonParser(jsonBytes);
        fromJson(jp, fromFile);
    }

    private void fromJson(JsonParser jp, boolean fromFile) throws Exception {
        JsonToken tolkein;
        tolkein = jp.nextToken();// START_OBJECT
        while ((tolkein = jp.nextToken()) != JsonToken.END_OBJECT) {
            String key = jp.getCurrentName();
            if ("metadata".equals(key)) {// process metadata
                tolkein = jp.nextToken();
                while ((tolkein = jp.nextToken()) != JsonToken.END_OBJECT) {
                    key = jp.getCurrentName();
                    tolkein = jp.nextToken();
                    String value = jp.getText();
                    LOG.trace("   Key = {} : Value = {}", key, value);
                    this.setMetadata(key, value);
                    // jp.nextToken();
                }// while
            } else if ("value".equals(key)) { // process value
                jp.nextToken();
                String value1 = jp.getText();
                LOG.trace("Key : {} Val : {}", key, value1);
                this.setValue(value1);
            } else if ("mimetype".equals(key)) { // process mimetype
                jp.nextToken();
                String value2 = jp.getText();
                LOG.trace("Key : {} Val : {}", key, value2);
                this.setMimetype(value2);
            } else {
                if (fromFile) { // accept rest of key-values
                    if ("objectType".equals(key)) {
                        jp.nextToken();
                        String value2 = jp.getText();
                        LOG.trace("Key : {} Val : {}", key, value2);
                        this.setObjectType(value2);
                    } else if ("capabilitiesURI".equals(key)) {
                        jp.nextToken();
                        String value2 = jp.getText();
                        LOG.trace("Key : {} Val : {}", key, value2);
                        this.setCapabilitiesURI(value2);
                    } else if ("objectID".equals(key)) { // process value
                        jp.nextToken();
                        String value2 = jp.getText();
                        LOG.trace("Key : {} Val : {}", key, value2);
                        this.setObjectID(value2);
                    } else if ("valueRange".equals(key)) { // process value
                        jp.nextToken();
                        String value2 = jp.getText();
                        LOG.trace("Key : {} Val : {}", key, value2);
                        this.setValuerange(value2);
                    } else {
                        LOG.warn("Invalid Key : {}", key);
                        throw new BadRequestException("Invalid Key : " + key);
                    } // inner if
                } else {
                    LOG.warn("Invalid Key : {}", key);
                    throw new BadRequestException("Invalid Key : " + key);
                }
            }
        }
    }


    public void setValue(byte[] bytes) {
        this.value = new String(bytes);
    }
}
