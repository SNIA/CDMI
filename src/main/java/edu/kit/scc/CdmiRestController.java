/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.filesystem.CapabilityDaoImpl;
import org.snia.cdmiserver.dao.filesystem.CdmiObjectDaoImpl;
import org.snia.cdmiserver.dao.filesystem.ContainerDaoImpl;
import org.snia.cdmiserver.dao.filesystem.DataObjectDaoImpl;
import org.snia.cdmiserver.exception.BadRequestException;
import org.snia.cdmiserver.exception.NotFoundException;
import org.snia.cdmiserver.model.Capability;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.Container;
import org.snia.cdmiserver.model.DataObject;
import org.snia.cdmiserver.util.MediaTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

@RestController
@ComponentScan(basePackages = { "edu.kit.scc", "org.snia.cdmiserver" })
public class CdmiRestController {

	private static final Logger log = LoggerFactory.getLogger(CdmiRestController.class);

	@Autowired
	private CapabilityDaoImpl capabilityDaoImpl;

	@Autowired
	private ContainerDaoImpl containerDaoImpl;

	@Autowired
	private DataObjectDaoImpl dataObjectDaoImpl;

	@Autowired
	private CdmiObjectDaoImpl cdmiObjectDaoImpl;

	@RequestMapping(path = "/cdmi_capabilities/**", produces = "application/cdmi-capability+json")
	public Capability capabilities(HttpServletRequest request, HttpServletResponse response) {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		log.debug("Capabilities path {}", path);
		Capability capability = capabilityDaoImpl.findByPath(path);

		response.addHeader("X-CDMI-Specification-Version", "1.1.1");
		return capability;
	}

	@RequestMapping(path = "/cdmi_objectid/{objectId}", method = RequestMethod.GET)
	public String getCdmiObjectByID(@PathVariable String objectId, HttpServletRequest request,
			HttpServletResponse response) {
		log.debug("Get objectID {}", objectId);

		try {
			CdmiObject container = containerDaoImpl.findByObjectId(objectId);
			if (container != null)
				return container.toJson().toString();
		} catch (org.snia.cdmiserver.exception.NotFoundException | java.lang.ClassCastException e) {
			DataObject dataObject = dataObjectDaoImpl.findByObjectId(objectId);
			if (dataObject != null) {
				String range = request.getHeader("Range");
				if (range != null) {
					byte[] content = dataObject.getValue().getBytes();
					String[] ranges = range.split("-");
					try {
						content = Arrays.copyOfRange(content, Integer.valueOf(ranges[0].trim()),
								Integer.valueOf(ranges[1].trim()));
						dataObject.setValue(new String(content));
					} catch (NumberFormatException e1) {
						throw new BadRequestException("bad range");
					}
				}
				return dataObject.toJson().toString();
			}
		}
		throw new NotFoundException("object not found");
	}

	@RequestMapping(path = "/**", method = RequestMethod.GET)
	public String getCdmiObjectByPath(HttpServletRequest request, HttpServletResponse response) {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		log.debug("Get path {}", path);

		try {
			CdmiObject container = containerDaoImpl.findByPath(path);
			if (container != null)
				return container.toJson().toString();
		} catch (org.snia.cdmiserver.exception.NotFoundException | java.lang.ClassCastException e) {
			DataObject dataObject = dataObjectDaoImpl.findByPath(path);
			if (dataObject != null) {
				String range = request.getHeader("Range");
				if (range != null) {
					byte[] content = dataObject.getValue().getBytes();
					String[] ranges = range.split("-");
					try {
						content = Arrays.copyOfRange(content, Integer.valueOf(ranges[0].trim()),
								Integer.valueOf(ranges[1].trim()));
						dataObject.setValue(new String(content));
					} catch (NumberFormatException e1) {
						throw new BadRequestException("bad range");
					}
				}
				return dataObject.toJson().toString();
			}
		}
		throw new NotFoundException("object not found");
	}

	@RequestMapping(path = "/**", method = RequestMethod.PUT)
	@ResponseStatus(value = HttpStatus.CREATED)
	public String putCdmiObject(@RequestHeader("Content-Type") String contentType, @RequestBody String body,
			HttpServletRequest request, HttpServletResponse response) {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

		log.debug("Create path {} as {}", path, contentType);

		if (contentType.equals(MediaTypes.CONTAINER)) {
			JSONObject json = new JSONObject(body);
			CdmiObject container = containerDaoImpl.createByPath(path, new Container(json));
			return container.toJson().toString();
		}

		if (contentType.equals(MediaTypes.DATA_OBJECT)) {
			JSONObject json = new JSONObject(body);
			DataObject dataObject = dataObjectDaoImpl.createByPath(path, new DataObject(json));
			return dataObject.toJson().toString();
		}

		throw new org.snia.cdmiserver.exception.BadRequestException("bad content-type");
	}

	@RequestMapping(path = "/**", method = RequestMethod.DELETE)
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void deleteCdmiObject(HttpServletRequest request, HttpServletResponse response) {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

		log.debug("Delete path {}", path);

		response.addHeader("X-CDMI-Specification-Version", "1.1.1");
		try {
			dataObjectDaoImpl.deleteByPath(path);
		} catch (org.snia.cdmiserver.exception.NotFoundException | java.lang.ClassCastException e) {
			containerDaoImpl.deleteByPath(path);
		}
	}

}
