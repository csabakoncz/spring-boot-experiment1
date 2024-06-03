package com.ck.b1.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import com.ck.b1.model.service.UserService;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class HelloWorldControllerTest {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	UserService userService;

	@Test
	void testAuth() throws Exception {

		var url = "http://localhost:" + port + "/hello-world";

		var responseEntity = restTemplate.getForEntity(url, String.class);

		// unauthenticated get, we will be redirected:
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);

		var username = "user2";
		var password = "password2";
		userService.createUser(username, password);

		responseEntity = restTemplate.withBasicAuth(username, password).getForEntity(url, String.class);

		// we get a valid response
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
}
