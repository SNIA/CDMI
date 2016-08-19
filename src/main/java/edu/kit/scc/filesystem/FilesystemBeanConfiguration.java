/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.filesystem;

import org.snia.cdmiserver.dao.CapabilityDao;
import org.snia.cdmiserver.dao.CdmiObjectDao;
import org.snia.cdmiserver.dao.ContainerDao;
import org.snia.cdmiserver.dao.DataObjectDao;
import org.snia.cdmiserver.dao.DomainDao;
import org.snia.cdmiserver.dao.filesystem.CapabilityDaoImpl;
import org.snia.cdmiserver.dao.filesystem.CdmiObjectDaoImpl;
import org.snia.cdmiserver.dao.filesystem.ContainerDaoImpl;
import org.snia.cdmiserver.dao.filesystem.DataObjectDaoImpl;
import org.snia.cdmiserver.dao.filesystem.DomainDaoImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"filesystem", "filesystem-test"})
public class FilesystemBeanConfiguration {

  @Value("${cdmi.data.baseDirectory}")
  private String baseDirectory;

  @Value("${cdmi.data.objectIdPrefix}")
  private String objectIdPrefix;

  /**
   * Initializes the file system version of the CDMI object DAO interface.
   * 
   * @return a {@link CdmiObjectDao}
   */
  @Bean
  public CdmiObjectDao cdmiObjectDao() {
    CdmiObjectDaoImpl cdmiObjectDaoImpl = new CdmiObjectDaoImpl();
    cdmiObjectDaoImpl.setBaseDirectory(baseDirectory);
    cdmiObjectDaoImpl.setObjectIdPrefix(objectIdPrefix);
    cdmiObjectDaoImpl.setObjectIdDirectory("cdmi_objectid");
    return cdmiObjectDaoImpl;
  }

  /**
   * Initializes the file system version of the {@link ContainerDao}.
   * 
   * @return a {@link ContainerDao}
   */
  @Bean
  public ContainerDao containerDao(CdmiObjectDao cdmiObjectDao) {
    ContainerDaoImpl containerDaoImpl = new ContainerDaoImpl();
    containerDaoImpl.setBaseDirectoryName(baseDirectory);
    containerDaoImpl.setCdmiObjectDao(cdmiObjectDao);
    return containerDaoImpl;
  }

  /**
   * Initializes the file system version of the {@link DataObjectDao}.
   * 
   * @return a {@link DataObjectDao}
   */
  @Bean
  public DataObjectDao dataObjectDao(CdmiObjectDao cdmiObjectDao) {
    DataObjectDaoImpl dataObjectDaoImpl = new DataObjectDaoImpl();
    dataObjectDaoImpl.setBaseDirectoryName(baseDirectory);
    dataObjectDaoImpl.setCdmiObjectDao(cdmiObjectDao);
    return dataObjectDaoImpl;
  }

  /**
   * Initializes the file system version of the {@link CapabilityDao}.
   * 
   * @return a {@link CapabilityDao}
   */
  @Bean
  public CapabilityDao capabilityDao(CdmiObjectDao cdmiObjectDao) {
    CapabilityDaoImpl capabilityDaoImpl = new CapabilityDaoImpl();
    capabilityDaoImpl.setBaseDirectory(baseDirectory);
    capabilityDaoImpl.setCdmiObjectDao(cdmiObjectDao);
    return capabilityDaoImpl;
  }

  /**
   * Initializes the file system version of the {@link DomainDao}.
   * 
   * @return a {@link DomainDao}
   */
  @Bean
  public DomainDao domainDao(CdmiObjectDao cdmiObjectDao) {
    DomainDaoImpl domainDaoImpl = new DomainDaoImpl();
    domainDaoImpl.setBaseDirectoryName(baseDirectory);
    domainDaoImpl.setCdmiObjectDao(cdmiObjectDao);
    return domainDaoImpl;
  }

}
