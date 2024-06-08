package com.ck.b1.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate.HttpClientOption;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;

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

        var url = "/hello-world";

        var responseEntity = restTemplate.getForEntity(url, String.class);

        // unauthenticated get, we will be redirected:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(responseEntity.getHeaders().getLocation().toString()).contains("/login");

        var username = "user2";
        var password = "password2";
        userService.createUser(username, password);

        responseEntity = restTemplate.withBasicAuth(username, password).getForEntity(url, String.class);

        // we get a valid response
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testRememberMeLogin() throws Exception {

        var userUrl = "http://localhost:" + port + "/user";
        var loginUrl = "http://localhost:" + port + "/login";

        // for "remember me" we need cookies:
        var client = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

        var responseEntity = client.getForEntity(userUrl, String.class);

        // unauthenticated get, we will be redirected:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(responseEntity.getHeaders().getLocation().toString()).contains("/login");

        var username = "user3";
        var password = "password3";
        userService.createUser(username, password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        var map= new LinkedMultiValueMap<String, String>();
        map.add("username", username);
        map.add("password", password);

        // we log in
        var entity = new HttpEntity(map, headers);
        responseEntity = client.exchange(loginUrl, HttpMethod.POST, entity, String.class);

        // we also get a redirect, but this time to the root
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(responseEntity.getHeaders().getLocation().toString()).doesNotContain("/login");

        // retrieve the user information again:
        responseEntity = client.getForEntity(userUrl, String.class);
        // Now it succeeds
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
