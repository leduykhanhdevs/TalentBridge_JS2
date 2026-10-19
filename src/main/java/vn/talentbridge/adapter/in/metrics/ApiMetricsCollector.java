package vn.talentbridge.adapter.in.metrics;

import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.atomic.LongAccumulator;

/**
 * Low-overhead request aggregates. No URL query, payload, identity, or credential is retained.
 * Data is intentionally bounded to a rolling 24-hour window and the current process lifetime.
 */
@Component
public class ApiMetricsCollector {

    public static final int BUCKET_MINUTES = 5;
    public static final int RETENTION_HOURS = 24;
    private static final long BUCKET_SECONDS = BUCKET_MINUTES * 60L;
    private static final long RETENTION_SECONDS = RETENTION_HOURS * 60L * 60L;
    private static final long CLEANUP_INTERVAL_SECONDS = 60L;
    private static final Set<String> STANDARD_HTTP_METHODS = Set.of(
            "GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "TRACE", "CONNECT");

    private final Clock clock;
    private final Instant collectedSince;
    private final ConcurrentMap<BucketKey, MetricBucket> buckets = new ConcurrentHashMap<>();
    private final AtomicLong nextCleanupEpochSecond = new AtomicLong();

    public ApiMetricsCollector() {
        this(Clock.systemUTC());
    }

    ApiMetricsCollector(Clock clock) {
        this.clock = clock;
        this.collectedSince = clock.instant();
    }

    public void record(String method, String routePattern, int status, long elapsedNanos) {
        Instant now = clock.instant();
        long nowEpochSecond = now.getEpochSecond();
        cleanupExpiredBuckets(nowEpochSecond);

        String normalizedMethod = method == null ? "" : method.toUpperCase(Locale.ROOT);
        String safeMethod = STANDARD_HTTP_METHODS.contains(normalizedMethod) ? normalizedMethod : "OTHER";
        String safeRoute = routePattern == null || routePattern.isBlank() ? "/_unmatched-api-route" : routePattern;
        if (safeRoute.length() > 200) safeRoute = "/_unmatched-api-route";
        String endpoint = safeMethod + " " + safeRoute;
        long bucketStart = Math.floorDiv(nowEpochSecond, BUCKET_SECONDS) * BUCKET_SECONDS;

        buckets.computeIfAbsent(new BucketKey(endpoint, bucketStart), ignored -> new MetricBucket())
                .record(status, Math.max(0L, elapsedNanos));
    }

    public ApiMetricsResponse snapshot(int requestedHours) {
        int hours = Math.max(1, Math.min(RETENTION_HOURS, requestedHours));
        Instant to = clock.instant();
        Instant from = to.minusSeconds(hours * 60L * 60L);
        long firstBucket = bucketStart(from.getEpochSecond());
        long lastBucket = bucketStart(to.getEpochSecond());
        cleanupExpiredBuckets(to.getEpochSecond());

        Map<String, List<Map.Entry<BucketKey, MetricBucket>>> byEndpoint = new TreeMap<>();
        buckets.entrySet().stream()
                .filter(entry -> entry.getKey().bucketStartEpochSecond() >= firstBucket
                        && entry.getKey().bucketStartEpochSecond() <= lastBucket)
                .forEach(entry -> byEndpoint.computeIfAbsent(entry.getKey().endpoint(), ignored -> new ArrayList<>()).add(entry));

        List<ApiMetricsResponse.EndpointSeries> endpoints = new ArrayList<>();
        Totals all = new Totals();
        byEndpoint.forEach((endpoint, entries) -> {
            entries.sort(Comparator.comparingLong(entry -> entry.getKey().bucketStartEpochSecond()));
            Totals endpointTotals = new Totals();
            List<ApiMetricsResponse.Bucket> timeline = new ArrayList<>();
            for (Map.Entry<BucketKey, MetricBucket> entry : entries) {
                MetricSnapshot current = entry.getValue().snapshot();
                endpointTotals.add(current);
                all.add(current);
                timeline.add(new ApiMetricsResponse.Bucket(
                        Instant.ofEpochSecond(entry.getKey().bucketStartEpochSecond()),
                        current.requestCount(), current.clientErrors(), current.serverErrors(),
                        averageLatencyMs(current.totalLatencyNanos(), current.requestCount()),
                        nanosToMillis(current.maxLatencyNanos())));
            }
            endpoints.add(new ApiMetricsResponse.EndpointSeries(
                    endpoint, endpointTotals.requestCount, endpointTotals.clientErrors, endpointTotals.serverErrors,
                    errorRatePercent(endpointTotals), averageLatencyMs(endpointTotals.totalLatencyNanos, endpointTotals.requestCount),
                    nanosToMillis(endpointTotals.maxLatencyNanos), List.copyOf(timeline)));
        });

        return new ApiMetricsResponse(
                to, collectedSince, from, to, BUCKET_MINUTES, RETENTION_HOURS, true,
                all.requestCount, all.clientErrors, all.serverErrors, errorRatePercent(all),
                averageLatencyMs(all.totalLatencyNanos, all.requestCount), nanosToMillis(all.maxLatencyNanos),
                List.copyOf(endpoints));
    }

    @Scheduled(fixedRate = 60_000L)
    public void removeExpiredBuckets() {
        cleanupExpiredBuckets(clock.instant().getEpochSecond());
    }

    private void cleanupExpiredBuckets(long nowEpochSecond) {
        long nextCleanup = nextCleanupEpochSecond.get();
        if (nowEpochSecond < nextCleanup
                || !nextCleanupEpochSecond.compareAndSet(nextCleanup, nowEpochSecond + CLEANUP_INTERVAL_SECONDS)) return;
        long oldestAllowed = nowEpochSecond - RETENTION_SECONDS;
        buckets.keySet().removeIf(key -> key.bucketStartEpochSecond() < oldestAllowed);
    }

    private static long bucketStart(long epochSecond) {
        return Math.floorDiv(epochSecond, BUCKET_SECONDS) * BUCKET_SECONDS;
    }

    private static double errorRatePercent(Totals totals) {
        if (totals.requestCount == 0) return 0.0;
        return round((totals.clientErrors + totals.serverErrors) * 100.0 / totals.requestCount);
    }

    private static double averageLatencyMs(long totalLatencyNanos, long requestCount) {
        return requestCount == 0 ? 0.0 : round(totalLatencyNanos / (double) requestCount / 1_000_000.0);
    }

    private static double nanosToMillis(long nanos) {
        return round(nanos / 1_000_000.0);
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record BucketKey(String endpoint, long bucketStartEpochSecond) {}

    private record MetricSnapshot(long requestCount, long clientErrors, long serverErrors,
                                  long totalLatencyNanos, long maxLatencyNanos) {}

    private static final class MetricBucket {
        private final LongAdder requestCount = new LongAdder();
        private final LongAdder clientErrors = new LongAdder();
        private final LongAdder serverErrors = new LongAdder();
        private final LongAdder totalLatencyNanos = new LongAdder();
        private final LongAccumulator maxLatencyNanos = new LongAccumulator(Long::max, 0L);

        private void record(int status, long elapsedNanos) {
            requestCount.increment();
            if (status >= 400 && status < 500) clientErrors.increment();
            if (status >= 500) serverErrors.increment();
            totalLatencyNanos.add(elapsedNanos);
            maxLatencyNanos.accumulate(elapsedNanos);
        }

        private MetricSnapshot snapshot() {
            return new MetricSnapshot(requestCount.sum(), clientErrors.sum(), serverErrors.sum(),
                    totalLatencyNanos.sum(), maxLatencyNanos.get());
        }
    }

    private static final class Totals {
        private long requestCount;
        private long clientErrors;
        private long serverErrors;
        private long totalLatencyNanos;
        private long maxLatencyNanos;

        private void add(MetricSnapshot snapshot) {
            requestCount += snapshot.requestCount();
            clientErrors += snapshot.clientErrors();
            serverErrors += snapshot.serverErrors();
            totalLatencyNanos += snapshot.totalLatencyNanos();
            maxLatencyNanos = Math.max(maxLatencyNanos, snapshot.maxLatencyNanos());
        }
    }
}
