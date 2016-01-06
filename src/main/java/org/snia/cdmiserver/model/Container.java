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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.filesystem.ContainerDaoImpl;
import org.snia.cdmiserver.exception.BadRequestException;

/**
 * <p>
 * Representation of a CDMI <em>Container</em>.
 * </p>
 */
public class Container {
    private static final Logger LOG = LoggerFactory.getLogger(Container.class);

    // Container creation fields
    private Map<String, String> metadata = new HashMap<String, String>();
    private Map<String, Object> exports = new HashMap<String, Object>();
    private String copy;
    private String move;
    private String reference;
    private String snapshot; // To create a snapshot via the "update" operation

    // Container representation fields
    private String objectType;
    private String objectID;
    private String parentURI;
    private String domainURI;
    private String capabilitiesURI;
    private String completionStatus;
    private Integer percentComplete; // FIXME - Specification says String but that does not make
                                     // sense
    private List<String> snapshots = new ArrayList<String>();
    private String childrenrange;
    private List<String> children = new ArrayList<String>();

    // Representation also includes "metadata", "exports"
    // Representation also includes "mimetype", "metadata", and "value" from creation fields

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public Map<String, Object> getExports() {
        return exports;
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

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
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

    public String getDomainURI() {
        return domainURI;
    }

    public void setDomainURI(String domainURI) {
        this.domainURI = domainURI;
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

    public List<String> getSnapshots() {
        return snapshots;
    }

    public String getChildrenrange() {
        return childrenrange;
    }

    public void setChildrenrange(String childrenrange) {
        this.childrenrange = childrenrange;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setMetaData(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String toJson(boolean toFile) {
        //
        StringWriter outBuffer = new StringWriter();
        try {
            JsonFactory f = new JsonFactory();
            JsonGenerator g = f.createJsonGenerator(outBuffer);
            g.useDefaultPrettyPrinter();
            g.writeStartObject();

            g.writeStringField("objectID", objectID);

            g.writeStringField("capabilitiesURI", capabilitiesURI);
            g.writeStringField("domainURI", domainURI);

            g.writeObjectFieldStart("metadata");
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                g.writeStringField(entry.getKey(), entry.getValue());
            }
            g.writeEndObject();

            g.writeObjectFieldStart("exports");
            for (Map.Entry<String, Object> entry : exports.entrySet()) {
                g.writeObjectFieldStart(entry.getKey());
                g.writeEndObject();
            }
            g.writeEndObject();

            if (!toFile) {
                g.writeStringField("objectType", objectType);
                g.writeStringField("parentURI", parentURI);
                g.writeArrayFieldStart("children");
                ListIterator<String> it = children.listIterator();
                while (it.hasNext()) {
                    g.writeString((String) it.next());
                }
                g.writeEndArray();
                g.writeStringField("childrenrange", childrenrange);
                if (completionStatus != null)
                    g.writeStringField("completionStatus", completionStatus);
            }

            g.writeEndObject();
            g.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ("Error : " + ex);
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
                    this.getMetadata().put(key, value);
                    // jp.nextToken();
                }// while
            } else if ("exports".equals(key)) {// process exports
                tolkein = jp.nextToken();
                while ((tolkein = jp.nextToken()) != JsonToken.END_OBJECT) {
                    key = jp.getCurrentName();
                    tolkein = jp.nextToken(); // Start
                    tolkein = jp.nextToken(); // End
                    this.getExports().put(key, null); // jp.nextToken();
                }// while
            } else if ("capabilitiesURI".equals(key)) {// process capabilitiesURI
                jp.nextToken();
                String value2 = jp.getText();
                LOG.trace("Key : {} Val : {}", key, value2);
                this.setCapabilitiesURI(value2);
            } else if ("domainURI".equals(key)) {// process domainURI
                jp.nextToken();
                String value2 = jp.getText();
                LOG.trace("Key : {} Val : {}", key, value2);
                this.setDomainURI(value2);
            } else if ("move".equals(key)) {// process move
                jp.nextToken();
                String value2 = jp.getText();
                LOG.trace("Key : {} Val : {}", key, value2);
                this.setMove(value2);
            } else {
                if (fromFile) { // accept rest of key-values
                    if ("objectID".equals(key)) { // process value
                        jp.nextToken();
                        String value2 = jp.getText();
                        LOG.trace("Key : {} Val : {}", key, value2);
                        this.setObjectID(value2);
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

    public Object getObjectURI() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
