/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package org.snia.cdmiserver.dao;

import org.snia.cdmiserver.exception.ConflictException;
import org.snia.cdmiserver.exception.NotFoundException;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.Domain;

public interface DomainDao {

  /**
   * <p>
   * Create a Domain at the specified path. All intermediate Domains must already exist.
   * </p>
   * 
   * @param path Path to the new {@link Domain}
   * 
   * @exception ConflictException if a Domain or data object at the specified path already exists
   * @exception IllegalArgumentException if an intermediate Domain does not exist
   */
  public CdmiObject createByPath(String path, Domain domainRequest);

  /**
   * <p>
   * Delete the Domain at the specified path.
   * </p>
   * 
   * @param path Path to the requested {@link Domain}
   * 
   * @exception NotFoundException if the specified path does not identify a valid resource
   * @exception IllegalArgumentException if the specified path identifies a data object instead of a
   *            Domain
   */
  public void deleteByPath(String path);

  /**
   * <p>
   * Find and return a {@link Domain} by object id, if any; otherwise, return <code>null</code> .
   * </p>
   * 
   * @param objectId Object ID of the requested {@link Domain}
   */
  public CdmiObject findByObjectId(String objectId);

  /**
   * <p>
   * Find and return a {@link Domain} by path, if any; otherwise, return <code>null</code>.
   * </p>
   * 
   * @param path Path to the requested {@link Domain}
   * 
   * @exception NotFoundException if the specified path does not identify a valid resource
   * @exception IllegalArgumentException if the specified path identifies a data object instead of a
   *            Domain
   */
  public CdmiObject findByPath(String path);

  /**
   * <p>
   * Update a Domain at the specified path.
   * </p>
   * 
   * @param path Path to the new {@link Domain}
   * @param domain {@link Domain}
   * @param requestedFields requested fields
   */
  public CdmiObject updateByPath(String path, Domain domain, String[] requestedFields);
}
