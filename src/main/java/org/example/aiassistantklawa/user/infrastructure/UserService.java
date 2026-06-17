package org.example.aiassistantklawa.user.infrastructure;

import jakarta.transaction.Transactional;
import org.example.aiassistantklawa.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Transactional
    public void getUser(User user) {
        userRepository.findByEmail(user.getEmail());
    }
}
