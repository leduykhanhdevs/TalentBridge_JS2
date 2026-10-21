package vn.talentbridge.adapter.in.metrics;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;

/** Records bounded endpoint-level measurements without persisting request data or blocking on I/O. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class ApiRequestMetricsFilter extends OncePerRequestFilter {

    private static final String API_PREFIX = "/api/v1/";
    private static final String ADMIN_METRICS_PREFIX = "/api/v1/admin/metrics";
    private static final String HEALTH_PREFIX = "/api/v1/health";

    private final ApiMetricsCollector collector;

    public ApiRequestMetricsFilter(ApiMetricsCollector collector) {
        this.collector = collector;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith(API_PREFIX) || path.startsWith(ADMIN_METRICS_PREFIX) || path.startsWith(HEALTH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startedAt = System.nanoTime();
        StatusCaptureResponse wrappedResponse = new StatusCaptureResponse(response);
        boolean failed = false;
        try {
            filterChain.doFilter(request, wrappedResponse);
        } catch (IOException | ServletException | RuntimeException | Error exception) {
            failed = true;
            throw exception;
        } finally {
            Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String routePattern = route instanceof String pattern ? pattern : null;
            int status = wrappedResponse.getStatus();
            if (failed && status < HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
            collector.record(request.getMethod(), routePattern, status, System.nanoTime() - startedAt);
        }
    }

    private static final class StatusCaptureResponse extends HttpServletResponseWrapper {
        private int capturedStatus;

        private StatusCaptureResponse(HttpServletResponse response) {
            super(response);
            capturedStatus = response.getStatus();
        }

        @Override
        public void setStatus(int status) {
            capturedStatus = status;
            super.setStatus(status);
        }

        @Override
        public void sendError(int status) throws IOException {
            capturedStatus = status;
            super.sendError(status);
        }

        @Override
        public void sendError(int status, String message) throws IOException {
            capturedStatus = status;
            super.sendError(status, message);
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            capturedStatus = HttpServletResponse.SC_FOUND;
            super.sendRedirect(location);
        }

        @Override
        public int getStatus() {
            return capturedStatus;
        }
    }
}
