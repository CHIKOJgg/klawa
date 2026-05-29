package org.example.aiassistantklawa.telegram.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TelegramController {
    @PostMapping("api/v1/telegram/webhook")
    public void webhook(){}
    @GetMapping("api/v1/telegram/health")
    public void health(){}
    @PostMapping("api/v1/telegram/send")
    public void send(){}

}
