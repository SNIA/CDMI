/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc;

import org.indigo.cdmi.BackEndException;
import org.indigo.cdmi.CdmiObjectStatus;
import org.indigo.cdmi.ConfigurableStorageBackend;
import org.indigo.cdmi.SubjectBasedStorageBackend;
import org.indigo.cdmi.spi.StorageBackend;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.CapabilityDao;
import org.snia.cdmiserver.dao.CdmiObjectDao;
import org.snia.cdmiserver.dao.ContainerDao;
import org.snia.cdmiserver.dao.DataObjectDao;
import org.snia.cdmiserver.model.Capability;
import org.snia.cdmiserver.model.CdmiObject;
import org.snia.cdmiserver.model.Container;
import org.snia.cdmiserver.model.DataObject;
import org.snia.cdmiserver.model.Domain;
import org.snia.cdmiserver.util.MediaTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.nio.file.Paths;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;

@RestController
@ComponentScan(basePackages = {"edu.kit.scc", "org.snia.cdmiserver"})
public class CdmiRestController {

  private static final Logger log = LoggerFactory.getLogger(CdmiRestController.class);

  @Autowired
  private CdmiObjectDao cdmiObjectDao;

  @Autowired
  private CapabilityDao capabilityDao;

  @Autowired
  private ContainerDao containerDao;

  @Autowired
  private DataObjectDao dataObjectDao;

  @Value("${cdmi.data.baseDirectory}")
  private String baseDirectory;

  @Value("${cdmi.qos.backend.type}")
  private String backendType;

  private StorageBackend storageBackend;

  @PostConstruct
  void init() {
    log.debug("Create RestController with storage-backend {}", backendType);
    HashMap<String, String> backendProperties = new HashMap<String, String>();
    backendProperties.put("baseDirectory", baseDirectory);
    try {
      storageBackend =
          ConfigurableStorageBackend.createStorageBackend(backendType, backendProperties);
    } catch (IllegalArgumentException ex) {
      log.warn("ERROR: {}", ex.getMessage());
    }
  }

  /**
   * Domains endpoint.
   * 
   * @param request the {@link HttpServletRequest}
   * @return a JSON serialized {@link Domain} object
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER", "ROLE_CLIENT"})
  @RequestMapping(path = "/cdmi_domains/**", method = RequestMethod.GET)
  public ResponseEntity<?> getDomains(HttpServletRequest request) {

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add("X-CDMI-Specification-Version", "1.1.1");
    responseHeaders.setContentType(new MediaType("application", "cdmi-domain"));

    String path =
        (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

    log.debug("Requested domain path {}", path);

    path = Paths.get(path).normalize().toString();
    log.debug("Normalized domain path {}", path.toString());

    return new ResponseEntity<String>("Domain not found", responseHeaders, HttpStatus.NOT_FOUND);
  }

  /**
   * Capabilities endpoint.
   * 
   * @param request the {@link HttpServletRequest}
   * @return a JSON serialized {@link Capability} object
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER", "ROLE_CLIENT"})
  @RequestMapping(path = "/cdmi_capabilities/**", method = RequestMethod.GET)
  public ResponseEntity<?> getCapabilities(HttpServletRequest request) {

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("application", "cdmi-capability"));
    responseHeaders.add("X-CDMI-Specification-Version", "1.1.1");

    String path =
        (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    log.debug("Requested capabilities path {}", path);

    path = Paths.get(path).normalize().toString();
    log.debug("Normalized capabilities path {}", path);

    String query = request.getQueryString();
    log.debug("Requested capabilities query {}", query);

    Capability capability = capabilityDao.findByPath(path);

    if (capability != null) {
      JSONObject capabilityJson = capability.toJson();

      if (query != null) {
        return new ResponseEntity<String>(filterQueryFields(capabilityJson, query).toString(),
            responseHeaders, HttpStatus.OK);
      } else {
        return new ResponseEntity<String>(capabilityJson.toString(), responseHeaders,
            HttpStatus.OK);
      }
    }

    return new ResponseEntity<String>("Capabilities not found", responseHeaders,
        HttpStatus.NOT_FOUND);
  }

  /**
   * ObjectId endpoint.
   * 
   * @param request the {@link HttpServletRequest}
   * @return a JSON serialized {@link CdmiObject}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER", "ROLE_CLIENT"})
  @RequestMapping(path = "/cdmi_objectid/{objectId}", method = RequestMethod.GET)
  public ResponseEntity<?> getCdmiObjectById(@PathVariable String objectId,
      HttpServletRequest request) {

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add("X-CDMI-Specification-Version", "1.1.1");
    responseHeaders.setContentType(new MediaType("application", "cdmi-object"));

    log.debug("Get objectID {}", objectId);

    String query = request.getQueryString();
    log.debug("Requested object query {}", query);

    CdmiObject cdmiObject = cdmiObjectDao.getCdmiObject(objectId);

    if (cdmiObject != null) {
      String objectString = generateResponse(cdmiObject, query, responseHeaders);

      return new ResponseEntity<String>(objectString, responseHeaders, HttpStatus.OK);
    }
    return new ResponseEntity<String>("Object not found", responseHeaders, HttpStatus.NOT_FOUND);
  }

  /**
   * Get path endpoint.
   * 
   * @param request the {@link HttpServletRequest}
   * @return a JSON serialized {@link Container} or {@link DataObject}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER", "ROLE_CLIENT"})
  @RequestMapping(path = "/**", method = RequestMethod.GET)
  public ResponseEntity<?> getCdmiObjectByPath(HttpServletRequest request) {

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add("X-CDMI-Specification-Version", "1.1.1");
    responseHeaders.setContentType(new MediaType("application", "cdmi-object"));

    String path =
        (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

    log.debug("Get path {}", path);

    path = Paths.get(path).normalize().toString();
    log.debug("Normalized path {}", path);

    String query = request.getQueryString();
    log.debug("Requested object query {}", query);

    CdmiObject cdmiObject = cdmiObjectDao.getCdmiObjectByPath(path);

    if (cdmiObject != null) {
      String objectString = generateResponse(cdmiObject, query, responseHeaders);

      return new ResponseEntity<String>(objectString, responseHeaders, HttpStatus.OK);
    } else {
      // if object exists on storage back-end but has not been created via CDMI
      //
      CdmiObject newCdmiObject = null;
      try {
        setAuthenticatedSubject();
        CdmiObjectStatus cdmiObjectStatus = storageBackend.getCurrentStatus(path);
        log.debug("storage back-end status {}", cdmiObjectStatus.toString());
        String currentCapabilitiesUri = cdmiObjectStatus.getCurrentCapabilitiesUri();

        if (currentCapabilitiesUri.contains("/cdmi_capabilities/container")) {
          log.debug("is storage back-end container ...");
          String body = "{}";
          String contentType = "application/cdmi-container";
          newCdmiObject = updateOrCreate(null, path, body, contentType);
        } else if (currentCapabilitiesUri.contains("/cdmi_capabilities/dataobject")) {
          log.debug("is storage back-end dataobject ...");
          String body = "{}";
          String contentType = "application/cdmi-object";
          newCdmiObject = updateOrCreate(null, path, body, contentType);
        } else {
          return new ResponseEntity<String>("Unsupported CDMI capabilities URI format",
              responseHeaders, HttpStatus.NOT_IMPLEMENTED);
        }

        String objectString = generateResponse(newCdmiObject, query, responseHeaders);

        return new ResponseEntity<String>(objectString, responseHeaders, HttpStatus.OK);
      } catch (BackEndException ex) {
        // ex.printStackTrace();
        log.warn(
            "WARNING: could not get current object status from storage back-end {} for object {}",
            backendType, path);
      }
    }
    return new ResponseEntity<String>("Object not found", responseHeaders, HttpStatus.NOT_FOUND);
  }

  /**
   * Put path endpoint.
   * 
   * @param request the {@link HttpServletRequest}
   * @return a JSON serialized {@link Container} or {@link DataObject}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER", "ROLE_CLIENT"})
  @RequestMapping(path = "/**", method = RequestMethod.PUT,
      consumes = {"application/cdmi-object", "application/cdmi-container", "application/json"})
  public ResponseEntity<?> putCdmiObject(@RequestHeader("Content-Type") String contentType,
      @RequestBody String body, HttpServletRequest request) {

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add("X-CDMI-Specification-Version", "1.1.1");
    responseHeaders.setContentType(new MediaType("application", "cdmi-object"));

    String path =
        (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

    log.debug("Create or update path {} as {}", path, contentType);

    path = Paths.get(path).normalize().toString();
    log.debug("Normalized path {}", path);

    CdmiObject cdmiObject = cdmiObjectDao.getCdmiObjectByPath(path);

    CdmiObject newCdmiObject = updateOrCreate(cdmiObject, path, body, contentType);

    if (newCdmiObject instanceof Container) {
      if (cdmiObject instanceof Container) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
      return new ResponseEntity<String>(((Container) newCdmiObject).toJson().toString(),
          responseHeaders, HttpStatus.CREATED);
    } else if (newCdmiObject instanceof DataObject) {
      if (cdmiObject instanceof DataObject) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
      return new ResponseEntity<String>(((DataObject) newCdmiObject).toJson().toString(),
          responseHeaders, HttpStatus.CREATED);
    }
    if (newCdmiObject == null) {
      return new ResponseEntity<String>("Object could not be created", responseHeaders,
          HttpStatus.CONFLICT);
    }
    return new ResponseEntity<String>("Bad request", responseHeaders, HttpStatus.BAD_REQUEST);
  }

  /**
   * Delete path endpoint.
   * 
   * @param request the {@link HttpServletRequest}
   * @return a {@link ResponseEntity}
   */
  @Secured({"ROLE_ADMIN", "ROLE_USER", "ROLE_CLIENT"})
  @RequestMapping(path = "/**", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteCdmiObject(HttpServletRequest request) {

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add("X-CDMI-Specification-Version", "1.1.1");

    String path =
        (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

    log.debug("Delete path {}", path);

    path = Paths.get(path).normalize().toString();
    log.debug("Normalized path {}", path);

    CdmiObject cdmiObject = cdmiObjectDao.getCdmiObjectByPath(path);

    if (cdmiObject != null) {
      if (cdmiObject instanceof Container) {
        Container container = containerDao.deleteByPath(path);
        if (container != null) {
          return new ResponseEntity<String>("Container deleted", responseHeaders,
              HttpStatus.NO_CONTENT);
        } else {
          return new ResponseEntity<String>("Container could not be deleted", responseHeaders,
              HttpStatus.CONFLICT);
        }
      } else if (cdmiObject instanceof DataObject) {
        DataObject dataObject = dataObjectDao.deleteByPath(path);
        if (dataObject != null) {
          return new ResponseEntity<String>("Data object deleted", responseHeaders,
              HttpStatus.NO_CONTENT);
        } else {
          return new ResponseEntity<String>("Data object could not be deleted", responseHeaders,
              HttpStatus.CONFLICT);
        }
      }
    }
    return new ResponseEntity<String>("Not found", responseHeaders, HttpStatus.NOT_FOUND);
  }

  private String generateResponse(CdmiObject cdmiObject, String query,
      HttpHeaders responseHeaders) {
    String objectString = cdmiObject.toString();
    if (cdmiObject instanceof Container) {
      responseHeaders.setContentType(new MediaType("application", "cdmi-container"));
      Container container = (Container) cdmiObject;

      // storage back-end integration
      getCurrentStatusFromStorageBackend(container);

      if (query != null) {
        objectString = filterQueryFields(container.toJson(), query).toString();
      } else {
        objectString = container.toJson().toString();
      }
    } else if (cdmiObject instanceof DataObject) {
      responseHeaders.setContentType(new MediaType("application", "cdmi-object"));
      DataObject dataObject = (DataObject) cdmiObject;

      // storage back-end integration
      getCurrentStatusFromStorageBackend(dataObject);

      if (query != null) {
        objectString = filterQueryFields(dataObject.toJson(), query).toString();
      } else {
        objectString = dataObject.toJson().toString();
      }
    } else if (cdmiObject instanceof Capability) {
      responseHeaders.setContentType(new MediaType("application", "cdmi-capability"));
      Capability capability = (Capability) cdmiObject;
      if (query != null) {
        objectString = filterQueryFields(capability.toJson(), query).toString();
      } else {
        objectString = capability.toJson().toString();
      }
    } else if (cdmiObject instanceof Domain) {
      responseHeaders.setContentType(new MediaType("application", "cdmi-domain"));
      Domain domain = (Domain) cdmiObject;
      if (query != null) {
        objectString = filterQueryFields(domain.toJson(), query).toString();
      } else {
        objectString = domain.toJson().toString();
      }
    }
    return objectString;
  }

  private void setAuthenticatedSubject() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Set<Principal> principals = new HashSet<>();
    principals.add(authentication);
    Set<Object> credentials = new HashSet<>();
    credentials.add(authentication.getCredentials());
    log.debug("Authentication: {}", authentication.toString());

    try {
      ((SubjectBasedStorageBackend) storageBackend)
          .setSubject(new Subject(true, principals, credentials, credentials));
    } catch (Exception e) {
      log.warn("Couldn't set authenticated subject, {}", e.getClass().getName());
    }
  }

  private CdmiObject updateOrCreate(CdmiObject cdmiObject, String path, String body,
      String contentType) {
    // create or update container
    if (contentType.contains(MediaTypes.CONTAINER)) {
      if (cdmiObject != null && (cdmiObject instanceof Container)) {
        log.debug("Update container...");
        Container existingContainer = (Container) cdmiObject;
        // update allowed for "metadata" and "capabilitiesURI"
        JSONObject updateJson = new JSONObject(body);
        if (updateJson.has("metadata")) {
          existingContainer.setMetadata(updateJson.getJSONObject("metadata"));
        }
        if (updateJson.has("capabilitiesURI")) {
          // Change of QoS
          try {
            setAuthenticatedSubject();
            storageBackend.updateCdmiObject(path, updateJson.getString("capabilitiesURI"));
            existingContainer.setCapabilitiesUri(updateJson.getString("capabilitiesURI"));
          } catch (Exception ex) {
            ex.printStackTrace();
            log.warn("WARNING: could not trigger QoS change for configured storage back-end {}",
                backendType);
          }
        }
        Container updatedContainer = (Container) cdmiObjectDao.updateCdmiObject(existingContainer);
        return updatedContainer;
      } else {
        log.debug("Create container...");
        Container containerRequest = Container.fromJson(new JSONObject(body));
        Container createdContainer = containerDao.createByPath(path, containerRequest);
        return createdContainer;
      }
    }
    // create or update data object
    if (contentType.contains(MediaTypes.DATA_OBJECT)) {
      if (cdmiObject != null && (cdmiObject instanceof DataObject)) {
        log.debug("Update data object...");
        DataObject existingDataObject = (DataObject) cdmiObject;
        // update allowed for "value", "metadata" and "capabilitiesURI"
        JSONObject updateJson = new JSONObject(body);
        if (updateJson.has("metadata")) {
          existingDataObject.setMetadata(updateJson.getJSONObject("metadata"));
        }
        if (updateJson.has("capabilitiesURI")) {
          // Change of QoS
          try {
            setAuthenticatedSubject();
            storageBackend.updateCdmiObject(path, updateJson.getString("capabilitiesURI"));
            existingDataObject.setCapabilitiesUri(updateJson.getString("capabilitiesURI"));
          } catch (Exception ex) {
            ex.printStackTrace();
            log.warn("WARNING: could not trigger QoS change for configured storage back-end {}",
                backendType);
          }
        }
        if (updateJson.has("value")) {
          // Change of content
          dataObjectDao.updateContent(path, updateJson.getString("value").getBytes());
        }
        DataObject updatedDataObject =
            (DataObject) cdmiObjectDao.updateCdmiObject(existingDataObject);
        return updatedDataObject;
      } else {
        log.debug("Create data object...");
        DataObject dataObjectRequest = DataObject.fromJson(new JSONObject(body));
        DataObject createdObject = dataObjectDao.createByPath(path, dataObjectRequest);
        return createdObject;
      }
    }
    return null;
  }

  private void getCurrentStatusFromStorageBackend(DataObject dataObject) {
    // add information from storage back-end
    try {
      if (storageBackend != null) {
        String path = Paths.get(dataObject.getParentUri(), dataObject.getObjectName()).toString();
        setAuthenticatedSubject();
        CdmiObjectStatus status = storageBackend.getCurrentStatus(path);
        // update monitored attributes
        for (Entry<String, Object> entry : status.getMonitoredAttributes().entrySet()) {
          dataObject.getMetadata().put(entry.getKey(), entry.getValue());
        }
        // update capabilities URI
        dataObject.setCapabilitiesUri(status.getCurrentCapabilitiesUri());
        // update QoS transition information
        if (status.getTargetCapabilitiesUri() != null) {
          dataObject.getMetadata().put("cdmi_capabilities_target",
              status.getTargetCapabilitiesUri());
        }
      }
    } catch (BackEndException ex) {
      log.warn("ERROR: {}", ex.getMessage());
    }
  }

  private void getCurrentStatusFromStorageBackend(Container container) {
    // add information from storage back-end
    try {
      if (storageBackend != null) {
        String path = Paths.get(container.getParentUri(), container.getObjectName()).toString();
        setAuthenticatedSubject();
        CdmiObjectStatus status = storageBackend.getCurrentStatus(path);
        // update monitored attributes
        for (Entry<String, Object> entry : status.getMonitoredAttributes().entrySet()) {
          container.getMetadata().put(entry.getKey(), entry.getValue());
        }
        // update export attributes
        if (status.getExportAttributes() != null) {
          container.setExports(new JSONObject(status.getExportAttributes()));
        }
        // update capabilities URI
        container.setCapabilitiesUri(status.getCurrentCapabilitiesUri());
        // update QoS transition information
        if (status.getTargetCapabilitiesUri() != null) {
          container.getMetadata().put("cdmi_capabilities_target",
              status.getTargetCapabilitiesUri());
        }
      }
    } catch (BackEndException ex) {
      log.warn("ERROR: {}", ex.getMessage());
    }
  }

  /**
   * Filters the requested JSON object according to the query parameters.
   * 
   * @param json the requested {@link JSONObject}
   * @param query the given query parameters
   * @return the filtered {@link JSONObject}
   */
  public JSONObject filterQueryFields(JSONObject json, String query) {
    String[] queryFields = query.split(";");
    List<String> queryList = Arrays.asList(queryFields);

    JSONArray names = json.names();
    JSONArray children = json.optJSONArray("children");

    for (int i = 0; i < names.length(); i++) {
      String name = names.getString(i);
      if (!queryList.contains(name)) {
        json.remove(name);
      }
    }

    if (children != null) {
      for (String queryField : queryList) {
        if (queryField.contains("children:")) {
          String[] childrenRange = queryField.split(":");
          String range = childrenRange[1];
          String[] rangeValues = range.split("-");

          JSONArray returnChildren = new JSONArray();
          int rangeStart = Integer.valueOf(rangeValues[0]);

          if (rangeValues.length > 1) {
            int rangeStop = Integer.valueOf(rangeValues[1]);
            for (int i = rangeStart; i <= rangeStop; i++) {
              try {
                returnChildren.put(children.get(i));
              } catch (JSONException ex) {
                log.warn("Requested range out of bounds, {}", i);
              }
            }
          } else {
            try {
              returnChildren.put(children.get(rangeStart));
            } catch (JSONException ex) {
              log.warn("Requested range out of bounds, {}", rangeStart);
            }
          }
          json.put("children", returnChildren);
          break;
        }
      }
    }
    return json;
  }
}
