/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

  private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

  @Value("${rest.user}")
  private String restUser;

  @Value("${rest.pass}")
  private String restPassword;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.debug("Load user: {}", username);
    if (username.equals(restUser)) {
      return new User(username, restPassword, true, true, true, true,
          AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
    }

    throw new UsernameNotFoundException(username + " unknown");
  }

}
