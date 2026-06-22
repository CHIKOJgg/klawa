package org.example.aiassistantklawa;

import org.example.aiassistantklawa.user.api.AuthController;
import org.example.aiassistantklawa.user.api.AuthenticationResponse;
import org.example.aiassistantklawa.user.api.RegisterRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class AuthControllerTest extends IntegrationTestBase{
    @Test
    void registerReturnsToken(){
        var req = new RegisterRequest("Sara", "Black","sara@tse.com","2222");
        var resp = testRestTemplate.postForEntity("api/v1/auth/register", req, AuthenticationResponse.class);
        Assertions.assertNotNull(resp);
        Assertions.assertEquals(HttpStatus.OK, resp.getStatusCode());
        Assertions.assertNotNull(resp.getBody().getToken());

    }
    @Test
    void duplicateEmailReturns409(){
        var req = new RegisterRequest("Sara", "Black","sara@tse.com","222");
        testRestTemplate.postForEntity("api/v1/auth/register", req, Void.class);
        var resp = testRestTemplate.postForEntity("api/v1/auth/register", req, Void.class);
        Assertions.assertNotNull(resp);
        Assertions.assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());

    }

}
