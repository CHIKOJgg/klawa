package org.example.aiassistantklawa.user.api;

import org.example.aiassistantklawa.user.domain.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    @GetMapping("api/v1/users/{id}")
    public void logout(@PathVariable String id) {
    }
    @GetMapping("api/v1/users")
    public List<User> getUsers() {
        return null;
    }
    @PutMapping("api/v1/users/{id}")
    public void updateUser(@PathVariable String id, @RequestBody User user) {
    }
    @DeleteMapping("api/v1/users/{id}")
    public void deleteUser(@PathVariable String id) {}
}
