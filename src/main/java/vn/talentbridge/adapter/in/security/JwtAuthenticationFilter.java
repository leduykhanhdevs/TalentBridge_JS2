package vn.talentbridge.adapter.in.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.talentbridge.core.application.port.out.TokenProviderPort;
import vn.talentbridge.core.application.port.out.AuthSessionRepositoryPort;
import vn.talentbridge.core.domain.model.AuthSession;
import java.time.LocalDateTime;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final TokenProviderPort tokenProvider;
    private final AuthSessionRepositoryPort authSessionRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                SecurityContextHolder.clearContext();

                if (tokenProvider.validateToken(jwt)
                        && "access".equals(tokenProvider.getTokenTypeFromToken(jwt))
                        && hasActiveSession(jwt)) {

                    String email = tokenProvider.getEmailFromToken(jwt);
                    Long userId = tokenProvider.getUserIdFromToken(jwt);
                    String role = tokenProvider.getRoleFromToken(jwt);

                    UserPrincipal principal = new UserPrincipal(userId, email, role);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    principal.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            log.debug("JWT authentication failed: {}", ex.getClass().getSimpleName());
        }

        filterChain.doFilter(request, response);
    }

    private boolean hasActiveSession(String jwt) {
        String sessionId = tokenProvider.getSessionIdFromToken(jwt);
        Long userId = tokenProvider.getUserIdFromToken(jwt);

        if (!StringUtils.hasText(sessionId) || userId == null) {
            return false;
        }

        AuthSession authSession = authSessionRepository
                .findActiveBySessionId(sessionId, LocalDateTime.now())
                .orElse(null);

        return authSession != null
                && userId.equals(authSession.getUserId());
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}