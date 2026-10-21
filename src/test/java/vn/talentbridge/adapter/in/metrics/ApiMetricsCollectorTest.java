package vn.talentbridge.adapter.in.metrics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ApiMetricsCollectorTest {

    @Test
    @DisplayName("Gom request, lỗi HTTP và độ trễ theo route trong các bucket 5 phút")
    void aggregatesRequestsByEndpointAndFiveMinuteBucket() {
        MutableClock clock = new MutableClock(Instant.parse("2026-10-19T10:02:00Z"));
        ApiMetricsCollector collector = new ApiMetricsCollector(clock);

        collector.record("GET", "/api/v1/jobs/{jobId}", 200, 10_000_000L);
        collector.record("POST", "/api/v1/jobs/{jobId}", 400, 30_000_000L);
        collector.record("POST", "/api/v1/jobs/{jobId}", 500, 90_000_000L);
        clock.advanceSeconds(5 * 60L);
        collector.record("GET", "/api/v1/jobs/{jobId}", 200, 20_000_000L);

        ApiMetricsResponse response = collector.snapshot(6);

        assertThat(response.bucketMinutes()).isEqualTo(5);
        assertThat(response.retentionHours()).isEqualTo(24);
        assertThat(response.resetsOnRestart()).isTrue();
        assertThat(response.requestCount()).isEqualTo(4);
        assertThat(response.clientErrorCount()).isEqualTo(1);
        assertThat(response.serverErrorCount()).isEqualTo(1);
        assertThat(response.errorRatePercent()).isEqualTo(50.0);
        assertThat(response.averageLatencyMs()).isEqualTo(37.5);
        assertThat(response.maxLatencyMs()).isEqualTo(90.0);
        assertThat(response.endpoints()).hasSize(2);
        assertThat(response.endpoints()).filteredOn(endpoint -> endpoint.endpoint().equals("GET /api/v1/jobs/{jobId}"))
                .singleElement().satisfies(endpoint -> {
            assertThat(endpoint.buckets()).hasSize(2);
            assertThat(endpoint.requestCount()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Giữ metrics tối đa 24 giờ và giới hạn khoảng truy vấn")
    void prunesExpiredBucketsAndClampsRequestedRange() {
        MutableClock clock = new MutableClock(Instant.parse("2026-10-19T10:02:00Z"));
        ApiMetricsCollector collector = new ApiMetricsCollector(clock);
        collector.record("GET", "/api/v1/jobs", 200, 1_000_000L);

        assertThat(collector.snapshot(48).retentionHours()).isEqualTo(24);
        clock.advanceSeconds(25 * 60L * 60L);

        assertThat(collector.snapshot(24).endpoints()).isEmpty();
    }

    @Test
    @DisplayName("Gom method HTTP không chuẩn để giới hạn số nhóm metrics")
    void groupsNonStandardMethodsIntoBoundedSeries() {
        ApiMetricsCollector collector = new ApiMetricsCollector(
                Clock.fixed(Instant.parse("2026-10-19T10:02:00Z"), ZoneOffset.UTC));

        collector.record("CUSTOM-VERB-1", "/api/v1/jobs", 200, 1_000_000L);
        collector.record("CUSTOM-VERB-2", "/api/v1/jobs", 200, 1_000_000L);

        assertThat(collector.snapshot(1).endpoints()).singleElement().satisfies(endpoint -> {
            assertThat(endpoint.endpoint()).isEqualTo("OTHER /api/v1/jobs");
            assertThat(endpoint.requestCount()).isEqualTo(2);
        });
    }

    private static final class MutableClock extends Clock {
        private final AtomicReference<Instant> instant;

        private MutableClock(Instant initialInstant) {
            this.instant = new AtomicReference<>(initialInstant);
        }

        private void advanceSeconds(long seconds) {
            instant.updateAndGet(current -> current.plusSeconds(seconds));
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant.get();
        }
    }
}
