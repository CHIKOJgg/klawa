package org.example.aiassistantklawa.agent.api;

import org.aspectj.weaver.loadtime.Agent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AiController {
    @PostMapping("api/v1/agent/chat")
    public void getAgents(){

    }
    @GetMapping("api/v1/agent/history")
    public void getAgentHistory(){

    }
    @PostMapping("api/v1/agent/summaries/daily")
    public void summariesDaily(){

    }
    @PostMapping("api/v1/agent/summaries/weekly")
    public void summariesWeekly(){

    }
    @PostMapping("api/v1/agent/priorities")
    public void priorities(){

    }
    @PostMapping("api/v1/agent/suggestions")
    public void suggestions(){}

}
