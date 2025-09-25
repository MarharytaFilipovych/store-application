package margo.grid.Store.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthDto {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
