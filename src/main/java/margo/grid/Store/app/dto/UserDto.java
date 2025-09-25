package margo.grid.Store.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import margo.grid.Store.app.annotation.ValidPassword;

@Data
public class UserDto {
    @NotBlank
    @Email
    private String email;

    @ValidPassword
    private String password;
}
