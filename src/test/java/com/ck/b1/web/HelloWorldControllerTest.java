package com.ck.b1.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class HelloWorldControllerTest {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;


	@Test
	void greetingShouldReturnDefaultMessage() throws Exception {

		var url = "http://localhost:" + port + "/hello-world";

		var responseEntity = restTemplate.getForEntity(url, String.class);
		System.out.println(responseEntity.getBody());

		// unauthenticated get, we will be redirected:
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
	}
}
