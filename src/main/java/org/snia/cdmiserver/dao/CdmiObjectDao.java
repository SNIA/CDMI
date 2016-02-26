package org.snia.cdmiserver.dao;

import org.snia.cdmiserver.model.CdmiObject;

public interface CdmiObjectDao {

	public CdmiObject createCdmiObject(CdmiObject cdmiObject);

	public CdmiObject getCdmiObject(String objectId);

	public CdmiObject updateCdmiObject(CdmiObject cdmiObject);

	public CdmiObject deleteCdmiObject(String objectId);

}
