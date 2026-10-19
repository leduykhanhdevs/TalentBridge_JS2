package vn.talentbridge.adapter.in.metrics;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ApiRequestMetricsFilterTest {

    @Test
    @DisplayName("Ghi status và route template, không giữ ID cụ thể trong URL")
    void recordsStatusAgainstRouteTemplate() throws Exception {
        ApiMetricsCollector collector = new ApiMetricsCollector(Clock.fixed(Instant.parse("2026-10-19T10:00:00Z"), ZoneOffset.UTC));
        ApiRequestMetricsFilter filter = new ApiRequestMetricsFilter(collector);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/jobs/712/applications");
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/v1/jobs/{jobId}/applications");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (servletRequest, servletResponse) -> ((HttpServletResponse) servletResponse).setStatus(422);
        filter.doFilter(request, response, chain);

        ApiMetricsResponse.EndpointSeries endpoint = collector.snapshot(1).endpoints().getFirst();
        assertThat(endpoint.endpoint()).isEqualTo("POST /api/v1/jobs/{jobId}/applications");
        assertThat(endpoint.clientErrorCount()).isEqualTo(1);
        assertThat(endpoint.errorRatePercent()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Không lưu đường dẫn tùy ý cho route chưa được ánh xạ")
    void doesNotRecordUnmatchedRequestPath() throws Exception {
        ApiMetricsCollector collector = new ApiMetricsCollector(Clock.fixed(Instant.parse("2026-10-19T10:00:00Z"), ZoneOffset.UTC));
        ApiRequestMetricsFilter filter = new ApiRequestMetricsFilter(collector);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/private@example.com");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> ((HttpServletResponse) servletResponse).setStatus(404));

        assertThat(collector.snapshot(1).endpoints()).singleElement()
                .extracting(ApiMetricsResponse.EndpointSeries::endpoint)
                .isEqualTo("GET /_unmatched-api-route");
    }
}
