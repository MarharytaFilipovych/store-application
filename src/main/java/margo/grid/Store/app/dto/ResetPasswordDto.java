package margo.grid.store.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import margo.grid.store.app.annotation.ValidPassword;
import java.util.UUID;

@Data
public class ResetPasswordDto {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @JsonProperty("reset_code")
    private UUID resetCode;

    @ValidPassword
    @JsonProperty("new_password")
    private String newPassword;
}
