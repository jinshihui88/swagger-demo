package com.example.swagger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SwaggerDemoApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpEntity<String> json(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    @Test
    void listUsersReturnsOk() {
        ResponseEntity<String> response = rest.getForEntity(url("/api/users"), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("1001"));
    }

    @Test
    void missingUserReturns404() {
        ResponseEntity<String> response = rest.getForEntity(url("/api/users/999999"), String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("\"code\":404"));
    }

    @Test
    void createUserReturns201() {
        ResponseEntity<String> response = rest.postForEntity(url("/api/users"),
                json("{\"username\":\"王五\",\"email\":\"wangwu@example.com\"}"), String.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().contains("1003"));
    }

    @Test
    void invalidUserReturns400WithFieldErrors() {
        ResponseEntity<String> response = rest.postForEntity(url("/api/users"),
                json("{\"username\":\"\",\"email\":\"bad-email\"}"), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("fieldErrors"));
    }

    @Test
    void openApiJsonContainsTagsAndSecurityScheme() {
        ResponseEntity<String> response = rest.getForEntity(url("/v3/api-docs"), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertTrue(body.contains("\"openapi\":\"3."));
        assertTrue(body.contains("UserCreateRequest"));
        assertTrue(body.contains("Bearer Authentication"));
    }

    @Test
    void openApiYamlAndSwaggerUiAreAvailable() {
        assertEquals(HttpStatus.OK,
                rest.getForEntity(url("/v3/api-docs.yaml"), String.class).getStatusCode());
        HttpStatusCode uiStatus = rest.getForEntity(url("/swagger-ui/index.html"), String.class).getStatusCode();
        assertTrue(uiStatus.is2xxSuccessful() || uiStatus.is3xxRedirection());
    }
}
