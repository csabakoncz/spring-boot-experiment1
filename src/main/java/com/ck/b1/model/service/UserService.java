package com.ck.b1.model.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;

@Component
public class UserService {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    PasswordEncoder passwordEncoder;

    public UserDetails createUser(String name, String password) {
        /*
         * Might create an entity that maps to the USERS table and then we could use JPA
         * to create a new entry. UserDetailsService uses JDBC template to insert a new
         * record.
         */

        // ROLE is mandatory!
        UserDetails user = User.withUsername(name).password(passwordEncoder.encode(password)).roles("USER").build();
        if (userDetailsService instanceof UserDetailsManager) {
            UserDetailsManager userDetailsManager = (UserDetailsManager) userDetailsService;
            userDetailsManager.createUser(user);
            return userDetailsManager.loadUserByUsername(name);
        }
        return null;
    }

    public UserDetails findUser(String name) {
        UserDetailsManager userDetailsManager = (UserDetailsManager) userDetailsService;
        try {
            return userDetailsManager.loadUserByUsername(name);
        } catch (UsernameNotFoundException e) {
            log.info(e.toString());
            return null;
        }
    }
}
