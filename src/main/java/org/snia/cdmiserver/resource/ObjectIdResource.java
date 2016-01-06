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

package org.snia.cdmiserver.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.snia.cdmiserver.dao.DataObjectDao;
import org.snia.cdmiserver.model.DataObject;
import org.snia.cdmiserver.util.MediaTypes;

/**
 * <p>
 * Access to objects by object Id.
 * </p>
 */
// @Path("/cdmi_objectid/{path}")
@Path("cdmi_objectid/{objectId}")
// How will URL get here ? TBD
public class ObjectIdResource {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectIdResource.class);

    private DataObjectDao dObjDao;// = new DataObjectDaoImpl();

    //
    public void setDataObjectDao(DataObjectDao dataObjectDao) {
        this.dObjDao = dataObjectDao;
    }

    /**
     * <p>
     * [7.5.8] Get Container By Object Id
     * </p>
     *
     * @param objectId
     *            Object ID of the requested {@link Container}
     */


    /*
     * @GET
     *
     * @Produces(MediaTypes.CONTAINER) public Response getContainer(@PathParam("objectId") String
     * objectId) { throw new UnsupportedOperationException("ObjectIdResource.getContainer()"); }
     */

    /**
     * <p>
     * [7.4.8] Get Data Object By Object Id
     * </p>
     *
     * @param objectId
     *            Object ID of the requested {@link DataObject}
     */
    @GET
    @Consumes(MediaTypes.DATA_OBJECT)
    @Produces(MediaTypes.DATA_OBJECT)
    public Response getDataObjectByID(
            @PathParam("objectId") String objectId,
            @Context HttpHeaders headers) {
        // print headers for debug
        if (LOG.isDebugEnabled()) {
            for (String hdr : headers.getRequestHeaders().keySet()) {
                LOG.debug("{} - {}", hdr, headers.getRequestHeader(hdr));
            }
        }
        LOG.debug("Get Object ID = {}", objectId);

        String path = "object_id/" + objectId;
        PathResource pathResource = new PathResource();
        return pathResource.getContainerOrDataObject(path,headers);
    }

    @PUT
    // @Consumes("application/json")
    @Consumes(MediaTypes.DATA_OBJECT)
    @Produces(MediaTypes.DATA_OBJECT)
    public Response updateDataObject(
            @Context HttpHeaders headers,
            @PathParam("objectId") String objectId,
            byte[] bytes) {
        // print headers for debug
        if (LOG.isDebugEnabled()) {
            for (String hdr : headers.getRequestHeaders().keySet()) {
                LOG.debug("{} - {}", hdr, headers.getRequestHeader(hdr));
            }

            String inBuffer = new String(bytes);
            LOG.debug("Object Id = {} {}", objectId, inBuffer);
        }
        PathResource pathResource = new PathResource();
        String objectPath = "object_id" + "/" + objectId;
        Response resp = pathResource.putDataObject(headers,objectPath,bytes);
        return resp;
    }

    @POST
    // @Consumes("application/json")
    @Consumes(MediaTypes.DATA_OBJECT)
    @Produces(MediaTypes.DATA_OBJECT)
    public Response createDataObject(
            @Context HttpHeaders headers,
            @PathParam("objectId") String objectId,
            byte[] bytes) {
        // print headers for debug
        if (LOG.isDebugEnabled()) {
            for (String hdr : headers.getRequestHeaders().keySet()) {
                LOG.debug("{} - {}", hdr, headers.getRequestHeader(hdr));
            }
            String inBuffer = new String(bytes);
            LOG.debug("Object Id = {} {}", objectId, inBuffer);
        }
        PathResource pathResource = new PathResource();
        String objectPath = "object_id" + "/" + objectId;
        Response resp = pathResource.postDataObject(objectPath,bytes);
        return resp;
  }
}
