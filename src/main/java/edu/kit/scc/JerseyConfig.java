/*
 * Copyright 2015 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc;

import org.glassfish.jersey.server.ResourceConfig;
import org.snia.cdmiserver.provider.BadRequestExceptionMapper;
import org.snia.cdmiserver.provider.CapabilityProvider;
import org.snia.cdmiserver.provider.ConflictExceptionMapper;
import org.snia.cdmiserver.provider.ContainerProvider;
import org.snia.cdmiserver.provider.ForbiddenExceptionMapper;
import org.snia.cdmiserver.provider.NotFoundExceptionMapper;
import org.snia.cdmiserver.provider.UnauthorizedExceptionMapper;
import org.snia.cdmiserver.provider.UnsupportedOperationExceptionMapper;
import org.snia.cdmiserver.resource.CapabilityResource;
import org.snia.cdmiserver.resource.ObjectIdResource;
import org.snia.cdmiserver.resource.PathResource;

// @Configuration
public class JerseyConfig extends ResourceConfig {

  /**
   * Used for SNIA endpoint configuration.
   */
  @Deprecated
  public JerseyConfig() {

    register(BadRequestExceptionMapper.class);
    register(ConflictExceptionMapper.class);
    register(ForbiddenExceptionMapper.class);
    register(NotFoundExceptionMapper.class);
    register(UnauthorizedExceptionMapper.class);
    register(UnsupportedOperationExceptionMapper.class);

    register(CapabilityProvider.class);
    register(ContainerProvider.class);

    register(CapabilityResource.class);
    register(PathResource.class);
    register(ObjectIdResource.class);
  }
}
