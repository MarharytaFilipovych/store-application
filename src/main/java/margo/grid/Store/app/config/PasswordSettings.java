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

@ConfigurationProperties(prefix = "password")
@PropertySource("classpath:password.properties")
@Validated
@Getter
@Setter
@Component
public class PasswordSettings {

    @NotNull @Min(4) @Max(50)
    private Integer minLength = 8;

    @NotNull @Min(8) @Max(100)
    private Integer maxLength;

    @NotNull @Min(0) @Max(10)
    private Integer minUppercase;

    @NotNull @Min(0) @Max(10)
    private Integer minLowercase;

    @NotNull @Min(0) @Max(10)
    private Integer minDigits ;

    @NotNull @Min(0) @Max(10)
    private Integer minSpecial;

    @NotNull @Min(1) @Max(10)
    private Integer maxRepeatChars;

    @NotNull @Min(1) @Max(10)
    private Integer maxSequenceLength;

    private String[] commonPasswords = new String[0];
}