package margo.grid.Store.app.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import margo.grid.Store.app.dto.ResetPasswordDto;
import margo.grid.Store.app.dto.UserDto;

public interface AuthService {
    void register(UserDto dto);

    String login(UserDto dto, HttpServletRequest request);

    String getResetCode(String email);

    void resetPassword(ResetPasswordDto dto);
}
