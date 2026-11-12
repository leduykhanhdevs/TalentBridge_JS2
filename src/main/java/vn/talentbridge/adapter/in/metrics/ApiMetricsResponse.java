package vn.talentbridge.adapter.in.metrics;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Bounded, in-process API telemetry returned to administrators. */
public record ApiMetricsResponse(
        Instant generatedAt,
        Instant collectedSince,
        Instant from,
        Instant to,
        int bucketMinutes,
        int retentionHours,
        boolean resetsOnRestart,
        long requestCount,
        long clientErrorCount,
        long serverErrorCount,
        double errorRatePercent,
        double averageLatencyMs,
        double maxLatencyMs,
        List<EndpointSeries> endpoints,
        Map<String, Long> statusCounts) {

    public record EndpointSeries(
            String endpoint,
            long requestCount,
            long clientErrorCount,
            long serverErrorCount,
            double errorRatePercent,
            double averageLatencyMs,
            double maxLatencyMs,
            List<Bucket> buckets,
            Map<String, Long> statusCounts) {}

    public record Bucket(
            Instant startsAt,
            long requestCount,
            long clientErrorCount,
            long serverErrorCount,
            double averageLatencyMs,
            double maxLatencyMs,
            Map<String, Long> statusCounts) {}
}
