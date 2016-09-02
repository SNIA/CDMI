/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.redis;

import edu.kit.scc.cdmiserver.dao.redis.CapabilityDaoImpl;
import edu.kit.scc.cdmiserver.dao.redis.CdmiObjectDaoImpl;
import edu.kit.scc.cdmiserver.dao.redis.ContainerDaoImpl;
import edu.kit.scc.cdmiserver.dao.redis.DataObjectDaoImpl;
import edu.kit.scc.cdmiserver.dao.redis.DomainDaoImpl;

import org.snia.cdmiserver.dao.CapabilityDao;
import org.snia.cdmiserver.dao.CdmiObjectDao;
import org.snia.cdmiserver.dao.ContainerDao;
import org.snia.cdmiserver.dao.DataObjectDao;
import org.snia.cdmiserver.dao.DomainDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@Profile({"redis", "redis-embedded"})
public class RedisBeanConfiguration {

  @Value("${spring.redis.port}")
  private int port;

  /**
   * Initializes the redis server connection.
   * 
   * @return a {@link JedisConnectionFactory}
   */
  @Bean
  JedisConnectionFactory jedisConnectionFactory() {
    JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
    jedisConnectionFactory.setPort(port);
    return jedisConnectionFactory;
  }

  /**
   * Initializes the redis access template.
   * 
   * @return a {@link StringRedisTemplate}
   */
  @Bean
  StringRedisTemplate redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
    return new StringRedisTemplate(jedisConnectionFactory);
  }

  /**
   * Initializes the redis version of {@link CdmiObjectDao}.
   * 
   * @return a {@link CdmiObjectDao}
   */
  @Bean
  CdmiObjectDao cdmiObjectDao(StringRedisTemplate redisTemplate) {
    CdmiObjectDaoImpl cdmiObjectDaoImpl = new CdmiObjectDaoImpl();
    cdmiObjectDaoImpl.setRedisTemplate(redisTemplate);
    return cdmiObjectDaoImpl;
  }

  /**
   * Initializes the redis version of {@link CapabilityDao}.
   * 
   * @return a {@link CapabilityDao}
   */
  @Bean
  CapabilityDao capabilityDao(CdmiObjectDao cdmiObjectDao) {
    CapabilityDaoImpl capabilityDaoImpl = new CapabilityDaoImpl();
    capabilityDaoImpl.setCdmiObjectDao(cdmiObjectDao);
    return capabilityDaoImpl;
  }

  /**
   * Initializes the redis version of {@link ContainerDao}.
   * 
   * @return a {@link ContainerDao}
   */
  @Bean
  ContainerDao containerDao(CdmiObjectDao cdmiObjectDao) {
    ContainerDaoImpl containerDaoImpl = new ContainerDaoImpl();
    containerDaoImpl.setCdmiObjectDao(cdmiObjectDao);
    return containerDaoImpl;
  }

  /**
   * Initializes the redis version of {@link DataObjectDao}.
   * 
   * @return a {@link DataObjectDao}
   */
  @Bean
  DataObjectDao dataObjectDao(CdmiObjectDao cdmiObjectDao) {
    ContainerDaoImpl containerDaoImpl = new ContainerDaoImpl();
    containerDaoImpl.setCdmiObjectDao(cdmiObjectDao);

    DataObjectDaoImpl dataObjectDaoImpl = new DataObjectDaoImpl();
    dataObjectDaoImpl.setCdmiObjectDao(cdmiObjectDao);
    dataObjectDaoImpl.setContainerDao(containerDaoImpl);
    return dataObjectDaoImpl;
  }

  /**
   * Initializes the redis version of {@link DomainDao}.
   * 
   * @return a {@link DomainDao}
   */
  @Bean
  DomainDao domainDao(CdmiObjectDao cdmiObjectDao) {
    DomainDaoImpl domainDaoImpl = new DomainDaoImpl();
    return domainDaoImpl;
  }
}
