package com.ck.b1.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import com.ck.b1.model.service.UserService;

@ActiveProfiles("test")
@SpringBootTest()
public class UserServiceTest {

	@Autowired
	UserService userService;

	@Test
	void canCreateUsers() {
		UserDetails user = userService.createUser("user1", "password1");
		Assertions.assertNotNull(user);
	}
}
