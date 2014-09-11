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
package org.snia.cdmiserver.dao;

import org.snia.cdmiserver.model.DataObject;

/**
 * <p>
 * DAO for manipulating CDMI <em>DataObject</em> instances.
 * </p>
 */
public interface DataObjectDao {

    /**
     * <p>
     * Create a data object at the specified path. All intermediate containers must already exist.
     * </p>
     * 
     * @param path
     *            Path to the new {@link DataObject}
     * 
     * @exception ConflictException
     *                if a container or data object at the specified path already exists
     * @exception IllegalArgumentException
     *                if an intermediate container does not exist
     */
    public DataObject createByPath(String path, DataObject dObj) throws Exception;

    public DataObject createNonCDMIByPath(String path, String contentType, DataObject dObj) throws Exception;

    public DataObject createById(String objectId, DataObject dObj);

    /**
     * <p>
     * Delete the data object at the specified path.
     * </p>
     * 
     * @param path
     *            Path to the requested {@link DataObject}
     * 
     * @exception NotFoundException
     *                if the specified path does not identify a valid resource
     * @exception IllegalArgumentException
     *                if the specified path identifies a container instead of a data object
     */
    public void deleteByPath(String path);

    /**
     * <p>
     * Find and return a {@link DataObject} by object id, if any; otherwise, return
     * <code>null</code>.
     * </p>
     * 
     * @param objectId
     *            Object ID of the requested {@link DataObject}
     */
    public DataObject findByObjectId(String objectId);

    /**
     * <p>
     * Find and return a {@link DataObject} by path, if any; otherwise, return <code>null</code>.
     * </p>
     * 
     * @param path
     *            Path to the requested {@link DataObject}
     */
    public DataObject findByPath(String path);

}
