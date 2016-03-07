/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.filesystem.DataObjectDaoImpl;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.DataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
public class DataObjectFilesystemTest {

	private static final Logger log = LoggerFactory.getLogger(DataObjectFilesystemTest.class);

	@Autowired
	private DataObjectDaoImpl dataObjectDaoImpl;

	@Test
	public void crudDataObjectTest() {
		String json = "{\"value\": \"Hello, CDMI!\", \"metadata\": {\"profile\": \"qos:2015\"}}";
		DataObject testObject = new DataObject(new JSONObject(json));

		assertNotNull(testObject);

		String path = "/container1/myContainer/myFile";
		CdmiObject object = dataObjectDaoImpl.createByPath(path, testObject);

		assertNotNull(object);
		log.debug(object.toString());
		
		DataObject objectByPath = dataObjectDaoImpl.findByPath(path);
		assertNotNull(objectByPath);
		log.debug(objectByPath.toString());
		
		DataObject objectById = dataObjectDaoImpl.findByObjectId(object.getObjectId());
		assertNotNull(objectById);
		log.debug(objectById.toString());
		
		assertEquals(objectById.toString(), objectByPath.toString());
		
		dataObjectDaoImpl.deleteByPath(path);
	}
}
