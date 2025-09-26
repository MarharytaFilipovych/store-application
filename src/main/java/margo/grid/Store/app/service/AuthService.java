package margo.grid.store.app.service;

import jakarta.servlet.http.HttpServletRequest;
import margo.grid.store.app.dto.ResetPasswordDto;
import margo.grid.store.app.dto.UserDto;

public interface AuthService {
    void register(UserDto dto);

    String login(UserDto dto, HttpServletRequest request);

    String getResetCode(String email);

    void resetPassword(ResetPasswordDto dto);
}
