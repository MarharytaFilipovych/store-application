package margo.grid.store.app.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "rate-limit")
@PropertySource("classpath:rate-limit.properties")
@Component
@Validated
@Getter
@Setter
public class RateLimitSettings {
    @NotNull @Min(1) @Max(1000)
    private Integer maxRequests;

    @NotNull
    private Duration refillPeriod;

    @NotNull
    private Duration cleanupInterval;

    private boolean enabled = true;

    private String[] excludedPaths = {"/register", "/forgot-password"};
}
