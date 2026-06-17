package org.example.aiassistantklawa.health.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("api/v1/system/health")
    public String health(){
        return "OK";
    }
    @GetMapping("api/v1/system/metrics")
    public void metrics(){}
    @GetMapping("api/v1/system/version")
    public void version(){}
}
