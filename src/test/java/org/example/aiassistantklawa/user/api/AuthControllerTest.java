package org.example.aiassistantklawa.user.api;

import org.example.aiassistantklawa.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
// Статические импорты — ВАЖНО импортировать через *
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import static org.junit.jupiter.api.Assertions.*;
@WebMvcTest(AuthController.class)
class AuthControllerTest {
@Autowired
private MockMvc mockMvc;
@MockitoBean
private AuthService authService;
@Autowired
private ObjectMapper objectMapper;

@BeforeEach
void setUp() {
//    UserRepository userRepository;
//    AuthService authService = new AuthService();
}

    @Test
    void register() throws Exception {
    given(authService.register(new RegisterRequest())).willReturn(any());
    mockMvc.perform(get("/api/v1/auth/register")
            .contentType(MediaType.ALL))
            .andExpect(any());
    }

    @Test
    void authenticate() {
    }

    @Test
    void login() {
    }

    @Test
    void refreshJwt() {
    }

    @Test
    void logout() {
    }

    @Test
    void getCurrUser() {
    }
}