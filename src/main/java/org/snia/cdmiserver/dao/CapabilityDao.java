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

package org.snia.cdmiserver.dao;

import org.snia.cdmiserver.exception.ConflictException;
import org.snia.cdmiserver.model.Capability;

/**
 * <p>
 * DAO for manipulating CDMI <em>Capability</em> instances.
 * </p>
 */
public interface CapabilityDao {

  /**
   * <p>
   * Find and return a {@link Capability} by object id, if any; otherwise, return <code>null</code>.
   * </p>
   */
  public Capability findByObjectId(String objectId);

  /**
   * <p>
   * Find and return a {@link Capability} by path, if any; otherwise, return <code>null</code>.
   * </p>
   * 
   * @param path Path to the requested {@link Capability}
   */
  public Capability findByPath(String path);

  /**
   * <p>
   * Create a capability at the specified path. All intermediate capability must already exist.
   * </p>
   * 
   * @param path Path to the new {@link Capability}
   * 
   * @exception ConflictException if a capability at the specified path already exists
   * @exception IllegalArgumentException if an intermediate capability does not exist
   */
  public Capability createByPath(String path, Capability capabilityRequest);
}
