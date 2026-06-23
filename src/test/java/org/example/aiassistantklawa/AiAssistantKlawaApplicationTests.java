package org.example.aiassistantklawa;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class AiAssistantKlawaApplicationTests {

    @Test
    void verifyModulithStructure() {
        ApplicationModules.of(AiAssistantKlawaApplication.class).verify();
    }

}
