package org.example.aiassistantklawa;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
class AiAssistantKlawaApplicationTests {

    @Test
    void contextLoads() {
    }
    @Test
    void run() {
        ApplicationModules.of(AiAssistantKlawaApplication.class);
    }

}
