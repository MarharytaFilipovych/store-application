package margo.grid.store.app.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import margo.grid.store.app.config.RateLimitSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock private RateLimitSettings rateLimitSettings;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;
    @Mock private PrintWriter writer;
    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() throws IOException {
        rateLimitFilter = new RateLimitFilter(rateLimitSettings);
        lenient().when(rateLimitSettings.isEnabled()).thenReturn(true);
        lenient().when(rateLimitSettings.getMaxRequests()).thenReturn(5);
        lenient().when(rateLimitSettings.getRefillPeriod()).thenReturn(Duration.ofMinutes(20));
        lenient().when(rateLimitSettings.getCleanupInterval()).thenReturn(Duration.ofMinutes(30));
        lenient().when(rateLimitSettings.getExcludedPaths()).thenReturn(new String[]{"/register", "/forgot-password"});
        lenient().when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void doFilterInternal_whenRateLimitDisabled_shouldAllowRequest() throws ServletException, IOException {
        // Arrange
        when(rateLimitSettings.isEnabled()).thenReturn(false);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/items", "/orders", "/cart-items", "/store/items"})
    void doFilterInternal_whenPathNotUnderAuth_shouldAllowRequest(String path) throws ServletException, IOException {
        // Arrange
        when(request.getServletPath()).thenReturn(path);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/register", "/forgot-password"})
    void doFilterInternal_whenPathIsExcluded_shouldAllowRequest(String path) throws ServletException, IOException {
        // Arrange
        when(request.getServletPath()).thenReturn(path);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilterInternal_whenWithinRateLimit_shouldAllowRequest() throws ServletException, IOException {
        // Arrange
        when(request.getServletPath()).thenReturn("/store/auth/login");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilterInternal_whenExceedingRateLimit_shouldBlockRequest() throws ServletException, IOException {
        // Arrange
        String clientIp = "192.168.1.1";
        when(request.getServletPath()).thenReturn("/store/auth/login");
        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(rateLimitSettings.getMaxRequests()).thenReturn(3);

        // Act
        for (int i = 0; i < 3; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        clearInvocations(response, filterChain, writer);
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON.getType());
        verify(writer).write("{\"message\": \"Too many attempts. Try later!!!\"}");
    }

    @Test
    void doFilterInternal_withDifferentClients_shouldTrackSeparately() throws ServletException, IOException {
        // Arrange
        when(request.getServletPath()).thenReturn("/store/auth/login");
        when(rateLimitSettings.getMaxRequests()).thenReturn(2);

        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        rateLimitFilter.doFilterInternal(request, response, filterChain);
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        when(request.getRemoteAddr()).thenReturn("192.168.1.2");
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(3)).doFilter(request, response);
        verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    void cleanOldBuckets_shouldRemoveExpiredBuckets() throws ServletException, IOException, InterruptedException {
        // Arrange
        when(request.getServletPath()).thenReturn("/store/auth/login");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(rateLimitSettings.getCleanupInterval()).thenReturn(Duration.ofMillis(100));
        rateLimitFilter.doFilterInternal(request, response, filterChain);
        Thread.sleep(150);

        // Act
        rateLimitFilter.cleanOldBuckets();

        // Assert
        clearInvocations(filterChain);
        rateLimitFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void cleanOldBuckets_shouldNotRemoveRecentBuckets() throws ServletException, IOException {
        // Arrange
        when(request.getServletPath()).thenReturn("/store/auth/login");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(rateLimitSettings.getMaxRequests()).thenReturn(1);
        when(rateLimitSettings.getCleanupInterval()).thenReturn(Duration.ofHours(1));

        // Create a bucket by making a request (exhausts the limit)
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Act
        rateLimitFilter.cleanOldBuckets();

        // Assert
        clearInvocations(response, filterChain, writer);
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }
}