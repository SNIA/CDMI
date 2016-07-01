/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.utils;

import static org.junit.Assert.assertTrue;

import edu.kit.scc.CdmiServerApplication;
import edu.kit.scc.Utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
@ActiveProfiles("test")
public class UtilsTest {

  @Test
  public void testCrc16() {
    byte[] bytes1 = {0};

    int checksum1 = Utils.crc16(bytes1);

    assertTrue(checksum1 == 0);

    byte[] bytes2 = {127};

    int checksum2 = Utils.crc16(bytes2);

    assertTrue(checksum2 == 57409);
  }

  @Test
  public void testBytesToHex() {
    byte[] bytes = {0};

    String str = Utils.bytesToHex(bytes);

    assertTrue(str.equals("00"));
  }
}
