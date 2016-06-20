/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.cdmi.rest;

import static org.junit.Assert.assertTrue;

import edu.kit.scc.CdmiRestController;
import edu.kit.scc.CdmiServerApplication;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.model.Capability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
public class FilterJsonTest {

  private static final Logger log = LoggerFactory.getLogger(FilterJsonTest.class);

  @Autowired
  private CdmiRestController controller;

  @Test
  public void testQueryNoParamter() {
    String query = "";
    Capability capability = new Capability("newProfile", "/cdmi_capabilities", "parentId");

    log.debug("Test filter JSON with {}", capability.toJson().toString());
    JSONObject filteredJson = controller.filterQueryFields(capability.toJson(), query);
    log.debug("Filtered JSON {}", filteredJson.toString());

    assertTrue(filteredJson.length() == 0);
  }

  @Test
  public void testQueryOneParameter() {
    String query = "objectID";
    Capability capability = new Capability("newProfile", "/cdmi_capabilities", "parentId");

    log.debug("Test filter JSON with {}", capability.toJson().toString());
    JSONObject filteredJson = controller.filterQueryFields(capability.toJson(), query);
    log.debug("Filtered JSON {}", filteredJson.toString());

    assertTrue(filteredJson.has("objectID"));
  }

  @Test
  public void testQueryMultipleParameter() {
    String query = "objectID;metadata;objectName";
    Capability capability = new Capability("newProfile", "/cdmi_capabilities", "parentId");

    log.debug("Test filter JSON with {}", capability.toJson().toString());
    JSONObject filteredJson = controller.filterQueryFields(capability.toJson(), query);
    log.debug("Filtered JSON {}", filteredJson.toString());

    assertTrue(filteredJson.has("objectID"));
    assertTrue(filteredJson.has("metadata"));
    assertTrue(filteredJson.has("objectName"));
  }

  @Test
  public void testQueryOneWrongParameter() {
    String query = "objectId";
    Capability capability = new Capability("newProfile", "/cdmi_capabilities", "parentId");

    log.debug("Test filter JSON with {}", capability.toJson().toString());
    JSONObject filteredJson = controller.filterQueryFields(capability.toJson(), query);
    log.debug("Filtered JSON {}", filteredJson.toString());

    assertTrue(filteredJson.length() == 0);
  }

  @Test
  public void testQueryMixedWrongParameter() {
    String query = "objectID;invalid";
    Capability capability = new Capability("newProfile", "/cdmi_capabilities", "parentId");

    log.debug("Test filter JSON with {}", capability.toJson().toString());
    JSONObject filteredJson = controller.filterQueryFields(capability.toJson(), query);
    log.debug("Filtered JSON {}", filteredJson.toString());

    assertTrue(filteredJson.has("objectID"));
  }

  @Test
  public void testQueryChildren() {
    String query = "children";
    JSONArray children = new JSONArray();
    children.put("child1");
    children.put("child2");

    Capability capability = new Capability("newProfile", "/cdmi_capabilities", "parentId");
    capability.setChildren(children);

    log.debug("Test filter JSON with {}", capability.toJson().toString());
    JSONObject filteredJson = controller.filterQueryFields(capability.toJson(), query);
    log.debug("Filtered JSON {}", filteredJson.toString());

    assertTrue(filteredJson.has("children"));
  }

  @Test
  public void testQueryMixedChildren() {
    String query = "children;objectID";
    JSONArray children = new JSONArray();
    children.put("child1");
    children.put("child2");

    Capability capability = new Capability("newProfile", "/cdmi_capabilities", "parentId");
    capability.setChildren(children);

    log.debug("Test filter JSON with {}", capability.toJson().toString());
    JSONObject filteredJson = controller.filterQueryFields(capability.toJson(), query);
    log.debug("Filtered JSON {}", filteredJson.toString());

    assertTrue(filteredJson.has("objectID"));
    assertTrue(filteredJson.has("children"));
  }

  @Test
  public void testQueryChildrenRange() {
    String query = "children:0-1";
    JSONArray children = new JSONArray();
    children.put("child1");
    children.put("child2");

    Capability capability = new Capability("newProfile", "/cdmi_capabilities", "parentId");
    capability.setChildren(children);

    log.debug("Test filter JSON with {}", capability.toJson().toString());
    JSONObject filteredJson = controller.filterQueryFields(capability.toJson(), query);
    log.debug("Filtered JSON {}", filteredJson.toString());

    assertTrue(filteredJson.has("children"));
    assertTrue(filteredJson.getJSONArray("children").length() == 2);

    JSONArray returnChildren = filteredJson.getJSONArray("children");
    assertTrue(returnChildren.get(0).equals("child1"));
    assertTrue(returnChildren.get(1).equals("child2"));
  }

  @Test
  public void testQueryOneChildren() {
    String query = "children:1";
    JSONArray children = new JSONArray();
    children.put("child1");
    children.put("child2");

    Capability capability = new Capability("newProfile", "/cdmi_capabilities", "parentId");
    capability.setChildren(children);

    log.debug("Test filter JSON with {}", capability.toJson().toString());
    JSONObject filteredJson = controller.filterQueryFields(capability.toJson(), query);
    log.debug("Filtered JSON {}", filteredJson.toString());

    assertTrue(filteredJson.has("children"));
    assertTrue(filteredJson.getJSONArray("children").length() == 1);

    JSONArray returnChildren = filteredJson.getJSONArray("children");
    assertTrue(returnChildren.get(0).equals("child2"));
  }

  @Test
  public void testQueryMixedChildrenRange() {
    String query = "objectID;children:0-1";
    JSONArray children = new JSONArray();
    children.put("child1");
    children.put("child2");

    Capability capability = new Capability("newProfile", "/cdmi_capabilities", "parentId");
    capability.setChildren(children);

    log.debug("Test filter JSON with {}", capability.toJson().toString());
    JSONObject filteredJson = controller.filterQueryFields(capability.toJson(), query);
    log.debug("Filtered JSON {}", filteredJson.toString());

    assertTrue(filteredJson.has("children"));
    assertTrue(filteredJson.has("objectID"));
    assertTrue(filteredJson.getJSONArray("children").length() == 2);

    JSONArray returnChildren = filteredJson.getJSONArray("children");
    assertTrue(returnChildren.get(0).equals("child1"));
    assertTrue(returnChildren.get(1).equals("child2"));
  }

  @Test
  public void testQueryChildrenOneOutOfRange() {
    String query = "objectID;children:3";
    JSONArray children = new JSONArray();
    children.put("child1");
    children.put("child2");

    Capability capability = new Capability("newProfile", "/cdmi_capabilities", "parentId");
    capability.setChildren(children);

    log.debug("Test filter JSON with {}", capability.toJson().toString());
    JSONObject filteredJson = controller.filterQueryFields(capability.toJson(), query);
    log.debug("Filtered JSON {}", filteredJson.toString());

    // assertTrue(!filteredJson.has("children"));
    assertTrue(filteredJson.has("objectID"));
  }

  @Test
  public void testQueryChildrenOutOfRange() {
    String query = "objectID;children:0-3";
    JSONArray children = new JSONArray();
    children.put("child1");
    children.put("child2");

    Capability capability = new Capability("newProfile", "/cdmi_capabilities", "parentId");
    capability.setChildren(children);

    log.debug("Test filter JSON with {}", capability.toJson().toString());
    JSONObject filteredJson = controller.filterQueryFields(capability.toJson(), query);
    log.debug("Filtered JSON {}", filteredJson.toString());

    assertTrue(filteredJson.has("children"));
    assertTrue(filteredJson.has("objectID"));
    assertTrue(filteredJson.getJSONArray("children").length() == 2);

    JSONArray returnChildren = filteredJson.getJSONArray("children");
    assertTrue(returnChildren.get(0).equals("child1"));
    assertTrue(returnChildren.get(1).equals("child2"));
  }
}
