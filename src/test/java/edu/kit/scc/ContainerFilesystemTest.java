/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.filesystem.ContainerDaoImpl;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.Container;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
public class ContainerFilesystemTest {

	private static final Logger log = LoggerFactory.getLogger(ContainerFilesystemTest.class);

	@Autowired
	private ContainerDaoImpl containerDaoImpl;

	@Test
	public void crudContainerTest() {
		String json = "{\"metadata\": {\"profile\": \"qos:2015\"}}";
		Container testObjectId = new Container(new JSONObject(json));
		assertNotNull(testObjectId);

		String containerPath = "/container";
		CdmiObject container = containerDaoImpl.createByPath(containerPath, testObjectId);

		assertNotNull(container);
		log.debug(container.toString());

		String myContainerPath = "/container/MyContainer";
		CdmiObject myContainer = containerDaoImpl.createByPath(myContainerPath, testObjectId);

		assertNotNull(myContainer);
		log.debug(myContainer.toString());

		assertTrue(containerDaoImpl.isContainer(containerPath));
		assertTrue(containerDaoImpl.isContainer(myContainerPath));

		CdmiObject objectByPath = containerDaoImpl.findByPath(myContainerPath);
		log.debug(objectByPath.toString());

		CdmiObject objectById = containerDaoImpl.findByObjectId(myContainer.getObjectId());
		log.debug(objectById.toString());

		assertEquals(objectByPath.toString(), objectById.toString());

		containerDaoImpl.deleteByPath(myContainerPath);
		containerDaoImpl.deleteByPath(containerPath);

	}

	@Test
	public void crudContainerNoMetadataTest() {
		String json = "{}";
		Container testObjectId = new Container(new JSONObject(json));
		assertNotNull(testObjectId);

		String containerPath = "/container";
		CdmiObject container = containerDaoImpl.createByPath(containerPath, testObjectId);

		assertNotNull(container);
		log.debug(container.toString());

		String myContainerPath = "/container/MyContainer";
		CdmiObject myContainer = containerDaoImpl.createByPath(myContainerPath, testObjectId);

		assertNotNull(myContainer);
		log.debug(myContainer.toString());

		assertTrue(containerDaoImpl.isContainer(containerPath));
		assertTrue(containerDaoImpl.isContainer(myContainerPath));

		CdmiObject objectByPath = containerDaoImpl.findByPath(myContainerPath);
		log.debug(objectByPath.toString());

		CdmiObject objectById = containerDaoImpl.findByObjectId(myContainer.getObjectId());
		log.debug(objectById.toString());

		assertEquals(objectByPath.toString(), objectById.toString());

		containerDaoImpl.deleteByPath(myContainerPath);
		containerDaoImpl.deleteByPath(containerPath);

	}
}
