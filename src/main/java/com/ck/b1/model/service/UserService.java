package com.ck.b1.model.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;

@Component
public class UserService {

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    PasswordEncoder passwordEncoder;

    public UserDetails createUser(String name, String password) {
        /*
         * Might create an entity that maps to the USERS table and then we could use JPA to creat a new entry.
         * UserDetailsService uses JDBC template to insert a new record.
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
}
