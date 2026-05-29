package org.example.aiassistantklawa.user.api;

import org.example.aiassistantklawa.user.domain.User;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
    @PostMapping("api/v1/auth/register")
    public String registerUser(@RequestBody User user) {
        return  user.toString();
    }
    @PostMapping("api/v1/auth/login")
    public String login(@RequestBody User user) {
        return  user.toString();
    }
    @PostMapping("api/v1/auth/refresh")
    public String refreshJwt(@RequestBody User user) {
        return  user.toString();
    }
    @PostMapping("api/v1/auth/logout")
    public void logout(@RequestBody User user) {

    }
    @GetMapping("api/v1/auth/me")
    public User getCurrUser(@RequestBody User user) {
        return  user;
    }
    @PostMapping("api/v1/auth/me")
    public AuthController(@RequestBody User user){

    }
    @DeleteMapping
    public void deleteUser(@RequestBody User user) {

    }

}
