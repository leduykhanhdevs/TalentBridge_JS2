package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.talentbridge.common.ApiResponse;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health Check", description = "Kiểm tra trạng thái hoạt động của hệ thống")
public class HealthCheckController {

    @GetMapping
    @Operation(summary = "Health check API", description = "Trả về trạng thái hoạt động hiện tại của TalentBridge API")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> healthInfo = new LinkedHashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("application", "TalentBridge");
        healthInfo.put("version", "1.0.0-SNAPSHOT");
        healthInfo.put("framework", "Java Spring Boot 3.3.4 (Java 21 LTS)");
        healthInfo.put("serverTime", LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success("Hệ thống TalentBridge đang hoạt động bình thường", healthInfo));
    }
}