/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package org.snia.cdmiserver.dao;

import org.snia.cdmiserver.model.CdmiObject;

public interface CdmiObjectDao {

  /**
   * Creates a new CDMI object.
   * 
   * @param cdmiObject the {@link CdmiObject}
   * @return the created {@link CdmiObject}
   */
  public CdmiObject createCdmiObject(CdmiObject cdmiObject);

  public CdmiObject createCdmiObject(CdmiObject cdmiObject, String string);

  public CdmiObject createCdmiObject(CdmiObject cdmiObject, String string, Boolean override);

  /**
   * Gets the CDMI object identified by it's id.
   * 
   * @param objectId the object's id
   * @return the {@link CdmiObject}
   */
  public CdmiObject getCdmiObject(String objectId);

  /**
   * Updates the given CDMI object.
   * 
   * @param cdmiObject the {@link CdmiObject}
   * @return the updated {@link CdmiObject}
   */
  public CdmiObject updateCdmiObject(CdmiObject cdmiObject);

  public CdmiObject updateCdmiObject(CdmiObject object, String string);

  /**
   * Deletes the CdmiObject identified by it's id.
   * 
   * @param objectId the object's id
   * @return the deleted {@link CdmiObject}
   */
  public CdmiObject deleteCdmiObject(String objectId);

  public CdmiObject getCdmiObjectByPath(String string);

  public CdmiObject deleteCdmiObjectByPath(String string);
}
