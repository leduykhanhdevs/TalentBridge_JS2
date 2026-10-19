package vn.talentbridge.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.talentbridge.adapter.in.metrics.ApiMetricsCollector;
import vn.talentbridge.adapter.in.metrics.ApiMetricsResponse;
import vn.talentbridge.common.ApiResponse;

@RestController
@RequestMapping("/api/v1/admin/metrics")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Admin API Metrics", description = "Thống kê request, độ trễ và lỗi theo endpoint")
public class AdminApiMetricsController {

    private final ApiMetricsCollector collector;

    @GetMapping("/api")
    @Operation(summary = "Thống kê API theo endpoint", description = "Trả về bucket 5 phút trong tối đa 24 giờ gần nhất; dữ liệu giữ trong bộ nhớ của tiến trình hiện tại.")
    public ResponseEntity<ApiResponse<ApiMetricsResponse>> getApiMetrics(
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(ApiResponse.success("Đã tải thống kê API", collector.snapshot(hours)));
    }
}
