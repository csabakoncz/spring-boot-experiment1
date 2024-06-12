package com.ck.b1.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate.HttpClientOption;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;

import com.ck.b1.model.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    void testBasicAuth() throws Exception {

        var url = "/hello-world";

        var responseEntity = restTemplate.getForEntity(url, String.class);

        // unauthenticated get, we will be redirected:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(responseEntity.getHeaders().getLocation().toString()).contains("/login");


        // TODO use @DirtiesContext instead of creating new users all the time to avoid collision
        // with users created by other tests.
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
        var loginUrl = loginUrl();

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
        var map = new LinkedMultiValueMap<String, String>();
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

    private TestRestTemplate createClient() {
        // for "remember me" we need cookies:
        var rtb = new RestTemplateBuilder().additionalInterceptors(new ClientHttpRequestInterceptor() {
            @Override
            public org.springframework.http.client.ClientHttpResponse intercept(
                    org.springframework.http.HttpRequest request, byte[] body,
                    org.springframework.http.client.ClientHttpRequestExecution execution) throws java.io.IOException {
                request.getHeaders().add("X-Requested-With", "XMLHttpRequest");
                return execution.execute(request, body);
            };
        });

        var client = new TestRestTemplate(rtb, null, null, HttpClientOption.ENABLE_COOKIES);
        return client;
    }

    @Test
    void testRememberMeLoginWithoutRedirects() throws Exception {

        var userUrl = "http://localhost:" + port + "/user";
        var loginUrl = loginUrl();

        var client = createClient();

        var responseEntity = client.getForEntity(userUrl, String.class);

        // unauthenticated get, we will be redirected:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        var username = "user4";
        var password = "password4";
        userService.createUser(username, password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        var map = new LinkedMultiValueMap<String, String>();
        map.add("username", username);
        map.add("password", password);

        // we log in
        var entity = new HttpEntity(map, headers);
        responseEntity = client.exchange(loginUrl, HttpMethod.POST, entity, String.class);

        // we also get a redirect, but this time to the root
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        // retrieve the user information again:
        responseEntity = client.getForEntity(userUrl, String.class);
        // Now it succeeds
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private String loginUrl() {
        return "http://localhost:" + port + "/login";
    }

    @Test
    public void testLoginWithJsonBody() throws JsonProcessingException {
        var username = "user5";
        var password = "password5";
        userService.createUser(username, password);

        var data = new HashMap<String,String>();
        data.put("username", username);
        data.put("password", password);

        var entity = jsonEntity(data);

        // we log in
        var client = createClient();
        var responseEntity = client.exchange(loginUrl(), HttpMethod.POST, entity, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        // check wrong password:
        data.put("password", "BAD");
        entity = jsonEntity(data);
        responseEntity = client.exchange(loginUrl(), HttpMethod.POST, entity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private HttpEntity jsonEntity(HashMap<String, String> data) throws JsonProcessingException {
        var om = new ObjectMapper();
        var jsonString = om.writeValueAsString(data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var entity = new HttpEntity(jsonString, headers);
        return entity;
    }
}
