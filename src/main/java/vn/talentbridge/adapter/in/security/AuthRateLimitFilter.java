package vn.talentbridge.adapter.in.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.talentbridge.adapter.in.web.exception.ErrorCode;
import vn.talentbridge.common.ApiResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Value("${talentbridge.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${talentbridge.rate-limit.auth-limit:10}")
    private int authLimit;

    @Value("${talentbridge.rate-limit.window-seconds:60}")
    private int windowSeconds;

    private static final Set<String> RATE_LIMITED_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/forgot-password"
    );

    private final ConcurrentHashMap<String, Deque<Long>> clientRequestMap = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (!isRateLimitedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = extractClientIp(request);
        String key = clientIp + ":" + path;
        long now = System.currentTimeMillis();
        long windowMillis = windowSeconds * 1000L;

        Deque<Long> queue = clientRequestMap.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        synchronized (queue) {
            while (!queue.isEmpty() && now - queue.peekFirst() > windowMillis) {
                queue.pollFirst();
            }

            if (queue.size() >= authLimit) {
                Long earliest = queue.peekFirst();
                long retryAfter = earliest != null ? Math.max(1, (windowMillis - (now - earliest)) / 1000) : windowSeconds;
                log.warn("Rate limit exceeded for IP [{}] on path [{}]. Requests in window: {}", clientIp, path, queue.size());
                sendRateLimitResponse(response, retryAfter);
                return;
            }

            queue.addLast(now);
        }

        // Periodic maintenance cleanup
        if (clientRequestMap.size() > 5000) {
            cleanupStaleEntries(now, windowMillis);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimitedPath(String path) {
        if (path == null) {
            return false;
        }
        for (String limitedPath : RATE_LIMITED_PATHS) {
            if (path.equals(limitedPath) || path.endsWith(limitedPath)) {
                return true;
            }
        }
        return false;
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    private void sendRateLimitResponse(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ApiResponse<Object> apiResponse = ApiResponse.error(
                ErrorCode.TOO_MANY_REQUESTS.getCode(),
                ErrorCode.TOO_MANY_REQUESTS.getMessage()
        );

        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }

    private void cleanupStaleEntries(long now, long windowMillis) {
        clientRequestMap.entrySet().removeIf(entry -> {
            Deque<Long> queue = entry.getValue();
            synchronized (queue) {
                while (!queue.isEmpty() && now - queue.peekFirst() > windowMillis) {
                    queue.pollFirst();
                }
                return queue.isEmpty();
            }
        });
    }

    public void reset() {
        clientRequestMap.clear();
    }
}
