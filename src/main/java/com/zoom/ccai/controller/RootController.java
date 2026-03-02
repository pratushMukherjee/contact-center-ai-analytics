package com.zoom.ccai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "service", "Contact Center AI Analytics Engine",
                "version", "1.0.0",
                "endpoints", Map.of(
                        "query", "POST /api/v1/analytics/query",
                        "history", "GET /api/v1/analytics/history",
                        "dashboard", "GET /api/v1/metrics/dashboard",
                        "health", "GET /api/v1/health",
                        "prometheus", "GET /actuator/prometheus",
                        "h2Console", "GET /h2-console"
                ),
                "example", "curl -X POST http://localhost:8080/api/v1/analytics/query -H \"Content-Type: application/json\" -d '{\"query\": \"Which agents had the highest handle time?\"}'"
        );
    }
}
