package margo.grid.store.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForgotPasswordDto {
    @Email
    @NotBlank
    private String email;
}