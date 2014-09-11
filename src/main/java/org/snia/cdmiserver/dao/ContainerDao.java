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

import org.snia.cdmiserver.exception.ConflictException;
import org.snia.cdmiserver.exception.NotFoundException;
import org.snia.cdmiserver.model.Container;

/**
 * <p>
 * DAO for manipulating CDMI <em>Container</em> instances.
 * </p>
 */
public interface ContainerDao {

    /**
     * <p>
     * Create a container at the specified path. All intermediate containers must already exist.
     * </p>
     * 
     * @param path
     *            Path to the new {@link Container}
     * 
     * @exception ConflictException
     *                if a container or data object at the specified path already exists
     * @exception IllegalArgumentException
     *                if an intermediate container does not exist
     */
    public Container createByPath(String path, Container containerRequest);

    /**
     * <p>
     * Delete the container at the specified path.
     * </p>
     * 
     * @param path
     *            Path to the requested {@link Container}
     * 
     * @exception NotFoundException
     *                if the specified path does not identify a valid resource
     * @exception IllegalArgumentException
     *                if the specified path identifies a data object instead of a container
     */
    public void deleteByPath(String path);

    /**
     * <p>
     * Find and return a {@link Container} by object id, if any; otherwise, return <code>null</code>
     * .
     * </p>
     * 
     * @param objectId
     *            Object ID of the requested {@link Container}
     */
    public Container findByObjectId(String objectId);

    /**
     * <p>
     * Find and return a {@link Container} by path, if any; otherwise, return <code>null</code>.
     * </p>
     * 
     * @param path
     *            Path to the requested {@link Container}
     * 
     * @exception NotFoundException
     *                if the specified path does not identify a valid resource
     * @exception IllegalArgumentException
     *                if the specified path identifies a data object instead of a container
     */
    public Container findByPath(String path);

    //
    public boolean isContainer(String path);
    //
}
