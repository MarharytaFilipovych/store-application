package margo.grid.store.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import margo.grid.store.app.annotation.ValidPassword;

@Data
public class UserDto {
    @NotBlank
    @Email
    private String email;

    @ValidPassword
    private String password;
}
