/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.filesystem.CapabilityDaoImpl;
import org.snia.cdmiserver.dao.filesystem.ContainerDaoImpl;
import org.snia.cdmiserver.dao.filesystem.DataObjectDaoImpl;
import org.snia.cdmiserver.dao.filesystem.DomainDaoImpl;
import org.snia.cdmiserver.exception.BadRequestException;
import org.snia.cdmiserver.exception.NotFoundException;
import org.snia.cdmiserver.model.Capability;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.Container;
import org.snia.cdmiserver.model.DataObject;
import org.snia.cdmiserver.model.Domain;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@ComponentScan(basePackages = {"edu.kit.scc", "org.snia.cdmiserver"})
public class CdmiRestController {

  private static final Logger log = LoggerFactory.getLogger(CdmiRestController.class);

  @Autowired
  private CapabilityDaoImpl capabilityDaoImpl;

  @Autowired
  private ContainerDaoImpl containerDaoImpl;

  @Autowired
  private DataObjectDaoImpl dataObjectDaoImpl;

  @Autowired
  private DomainDaoImpl domainDaoImpl;

  @RequestMapping(path = "/cdmi_domains/**", method = RequestMethod.GET,
      produces = "application/cdmi-domain+json")
  public String getDomainByPath(HttpServletRequest request, HttpServletResponse response) {
    String path =
        (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

    log.debug("Get path {}", path);
    String[] requestedFields = parseFields(request);
    CdmiObject domain = domainDaoImpl.findByPath(path);
    if (domain != null)
      if (requestedFields == null)
        return domain.toJson().toString();
      else
        return getRequestedJson(domain.toJson(), requestedFields).toString();

    throw new NotFoundException("object not found");
  }

  @RequestMapping(path = "/cdmi_capabilities/**", produces = "application/cdmi-capability+json")
  public String capabilities(HttpServletRequest request, HttpServletResponse response) {
    String path =
        (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    log.debug("Capabilities path {}", path);

    Capability capability = capabilityDaoImpl.findByPath(path);
    response.addHeader("X-CDMI-Specification-Version", "1.1.1");
    String[] requestedFields = parseFields(request);
    if (requestedFields == null)
      return capability.toJson().toString();
    JSONObject json = capability.toJson();
    return getRequestedJson(json, requestedFields).toString();
  }

  @RequestMapping(path = "/cdmi_objectid/{objectId}", method = RequestMethod.GET,
      produces = "application/cdmi-object+json")
  public String getCdmiObjectByID(@PathVariable String objectId, HttpServletRequest request,
      HttpServletResponse response) {
    log.debug("Get objectID {}", objectId);
    String[] requestedFields = parseFields(request);
    try {
      CdmiObject container = containerDaoImpl.findByObjectId(objectId);
      if (container != null) {
        response.setContentType("application/cdmi-container+json");

        if (requestedFields == null) {
          return container.toJson().toString();
        } else {
          return getRequestedJson(container.toJson(), requestedFields).toString();
        }
      }
    } catch (org.snia.cdmiserver.exception.NotFoundException | java.lang.ClassCastException e) {
      try {
        DataObject dataObject = dataObjectDaoImpl.findByObjectId(objectId);
        if (dataObject != null) {
          response.setContentType("application/cdmi-object+json");
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
          if (requestedFields == null) {
            return dataObject.toJson().toString();
          } else {
            return getRequestedJson(dataObject.toJson(), requestedFields).toString();
          }
        }
      } catch (java.lang.ClassCastException e1) {
        CdmiObject domain = domainDaoImpl.findByObjectId(objectId);
        if (domain != null)
          if (requestedFields == null)
            return domain.toJson().toString();
          else
            return getRequestedJson(domain.toJson(), requestedFields).toString();
      }
    }


    throw new NotFoundException("object not found");
  }

  @RequestMapping(path = "/**", method = RequestMethod.GET,
      produces = "application/cdmi-object+json")
  public String getCdmiObjectByPath(HttpServletRequest request, HttpServletResponse response) {
    String path =
        (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    log.debug("Get path {}", path);
    String[] requestedFields = parseFields(request);
    try {
      CdmiObject container = containerDaoImpl.findByPath(path);
      if (container != null) {
        response.setContentType("application/cdmi-container+json");

        if (requestedFields == null) {
          return container.toJson().toString();
        } else {
          return getRequestedJson(container.toJson(), requestedFields).toString();
        }
      }
    } catch (org.snia.cdmiserver.exception.NotFoundException | java.lang.ClassCastException e) {
      DataObject dataObject = dataObjectDaoImpl.findByPath(path);
      if (dataObject != null) {
        response.setContentType("application/cdmi-object+json");

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
        if (requestedFields == null) {
          return dataObject.toJson().toString();
        } else {
          return getRequestedJson(dataObject.toJson(), requestedFields).toString();
        }
      }
    }
    throw new NotFoundException("object not found");
  }



  @RequestMapping(path = "/**", method = RequestMethod.PUT)
  @ResponseStatus(value = HttpStatus.CREATED)
  public String putCdmiObject(@RequestHeader("Content-Type") String contentType,
      @RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
    String path =
        (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

    log.debug("Create path {} as {}", path, contentType);

    String[] requestedFields = parseFields(request);
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

    if (contentType.equals(MediaTypes.ACCOUNT)) {

      JSONObject json = new JSONObject(body);
      CdmiObject domain;
      if (requestedFields == null)
        domain = domainDaoImpl.createByPath(path, new Domain(json));
      else
        domain = domainDaoImpl.updateByPath(path, new Domain(json), requestedFields);
      return domain.toJson().toString();
    }

    throw new org.snia.cdmiserver.exception.BadRequestException("bad content-type");
  }

  @RequestMapping(path = "/**", method = RequestMethod.DELETE)
  @ResponseStatus(value = HttpStatus.NO_CONTENT)
  public void deleteCdmiObject(HttpServletRequest request, HttpServletResponse response) {
    String path =
        (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

    log.debug("Delete path {}", path);

    response.addHeader("X-CDMI-Specification-Version", "1.1.1");
    try {
      dataObjectDaoImpl.deleteByPath(path);
    } catch (org.snia.cdmiserver.exception.NotFoundException | java.lang.ClassCastException e) {
      try {
        containerDaoImpl.deleteByPath(path);
      } catch (org.snia.cdmiserver.exception.NotFoundException | java.lang.ClassCastException e1) {
        domainDaoImpl.deleteByPath(path);
      }
    }
  }

  private String[] parseFields(HttpServletRequest request) {
    Enumeration<String> attributes = request.getParameterNames();
    String[] requestedFields = null;
    while (attributes.hasMoreElements()) {
      String attributeName = attributes.nextElement();
      requestedFields = attributeName.split(";");
    }
    return requestedFields;
  }

  private JSONObject getRequestedJson(JSONObject object, String[] requestedFields) {
    JSONObject requestedJson = new JSONObject();
    try {
      for (int i = 0; i < requestedFields.length; i++) {
        String field = requestedFields[i];
        if (!field.contains(":"))
          requestedJson.put(field, object.get(field));
        else {
          String[] fieldsplit = field.split(":");
          if (object.get(fieldsplit[0]) instanceof JSONObject) {
            JSONObject fieldObject = new JSONObject();
            String prefix = fieldsplit[1];
            String fieldname = fieldsplit[0];
            if (requestedJson.has(fieldname))
              fieldObject = requestedJson.getJSONObject(fieldname);
            Iterator<?> keys = object.getJSONObject(fieldname).keys();
            while (keys.hasNext()) {
              String key = (String) keys.next();
              if (key.startsWith(prefix))
                fieldObject.put(key, object.getJSONObject(fieldname).get(key));
            }
            if (fieldObject.length() != 0)
              requestedJson.put(fieldname, fieldObject);
          } else if (field.startsWith("children:")) {
            String range = field.split("children:")[1];
            String[] rangeSplit = range.split("-");
            List<String> requestedChildren = new ArrayList<String>();
            JSONArray children = object.getJSONArray("children");
            int startIndex = Integer.valueOf(rangeSplit[0]);
            if (rangeSplit.length > 1) {
              int endIndex = Integer.valueOf(rangeSplit[1]);
              for (int j = startIndex; j <= endIndex; j++)
                requestedChildren.add(children.getString(j));
            } else {
              requestedChildren.add(children.getString(startIndex));
            }
            requestedJson.put("children", requestedChildren);
          } else if (field.startsWith("value:")) {
            String range = field.split("value:")[1];
            String[] rangeSplit = range.split("-");
            requestedJson
                .put("value",
                    new String(Arrays.copyOfRange(object.getString("value").getBytes(),
                        Integer.valueOf(rangeSplit[0].trim()),
                        Integer.valueOf(rangeSplit[1].trim()))));
          } else
            throw new BadRequestException("Bad prefix");

        }
      }
      if (requestedJson.has("childrenrange") && requestedJson.has("children")) {
        requestedJson.put("childrenrange",
            "0-" + String.valueOf(requestedJson.getJSONArray("children").length() - 1));
      }
    } catch (JSONException e) {
      throw new BadRequestException("bad field");
    } catch (IndexOutOfBoundsException | NumberFormatException e) {
      throw new BadRequestException("bad range");
    }
    return requestedJson;
  }

}
