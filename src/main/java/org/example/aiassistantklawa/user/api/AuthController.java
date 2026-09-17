package org.example.aiassistantklawa.user.api;

import lombok.RequiredArgsConstructor;
import org.example.aiassistantklawa.user.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest authRequest) {
        return ResponseEntity.ok(authService.authenticate(authRequest));
    }


    @PostMapping("/refresh")
    public String refreshJwt(@RequestBody User user) {
        return user.toString();
    }

    @PostMapping("/logout")
    public void logout(@RequestBody User user) {
    }
    @GetMapping("/me")
    public User getCurrUser(@RequestBody User user) {
        return user;
    }
}