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

  public CdmiObject createCdmiObject(CdmiObject cdmiObject);

  public CdmiObject getCdmiObject(String objectId);

  public CdmiObject updateCdmiObject(CdmiObject cdmiObject);

  public CdmiObject deleteCdmiObject(String objectId);

}
