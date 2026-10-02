package org.example.klawa.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class KlawaAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(KlawaAiAgentApplication.class, args);
    }
}
