package org.example.aiassistantklawa;

import org.example.aiassistantklawa.user.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@DataJpaTest
public class UserRepositoryTest {
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer(new DockerImageName("postgres:16.0"));
    @Autowired
    private UserRepository userRepository;
}
