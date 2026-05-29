package org.example.aiassistantklawa.analytics.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalyticsController {
    @GetMapping("/api/v1/analytics/productivity")
    public void productivity(){}
    @GetMapping("/api/v1/analytics/tasks")
    public void tasks(){}
    @GetMapping("/api/v1/analytics/habits")
    public void habits(){}
    @GetMapping("/api/v1/analytics/workload")
    public void workload(){}
}
