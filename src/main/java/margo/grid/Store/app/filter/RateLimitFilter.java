package margo.grid.store.app.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import margo.grid.store.app.config.RateLimitSettings;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
    private final Map<String, BucketEntry> buckets = new ConcurrentHashMap<>();
    private final RateLimitSettings settings;

    private static class BucketEntry {
        private final Bucket bucket;

        @Getter
        private volatile Instant lastAccessed;

        public BucketEntry(Bucket bucket) {
            this.bucket = bucket;
            this.lastAccessed = Instant.now();
        }

        public Bucket getBucket() {
            this.lastAccessed = Instant.now();
            return bucket;
        }
    }

    @Scheduled(fixedRateString = "#{rateLimitSettings.cleanupInterval.toMillis()}")
    public void cleanOldBuckets(){
        buckets.entrySet().removeIf(entry ->
                entry.getValue()
                        .getLastAccessed()
                        .isBefore(Instant.now()
                                .minus(settings.getCleanupInterval())));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!settings.isEnabled() || !shouldApplyRateLimit(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientId = getClientId(request);
        BucketEntry bucketEntry = buckets.computeIfAbsent(clientId, this::createNewBucketEntry);

        if(bucketEntry.getBucket().tryConsume(1)) filterChain.doFilter(request, response);
        else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON.getType());
            response.getWriter().write("{\"message\": \"Too many attempts. Try later!!!\"}");
        }
    }

    private boolean shouldApplyRateLimit(String path) {
        if (!path.startsWith("/store/auth")) return false;
        for (String excludedPath : settings.getExcludedPaths()) {
            if (path.contains(excludedPath)) return false;
        }
        return true;
    }

    private BucketEntry createNewBucketEntry(String clientId) {
        return new BucketEntry(Bucket.builder()
                .addLimit(Bandwidth.classic(settings.getMaxRequests(),
                        Refill.greedy(settings.getMaxRequests(), settings.getRefillPeriod())))
                .build());
    }

    private String getClientId(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) return xRealIp;
        return request.getRemoteAddr();
    }
}
