package margo.grid.store.app.service;

import jakarta.servlet.http.HttpServletRequest;
import margo.grid.store.app.dto.LoginResponseDto;
import margo.grid.store.app.dto.ResetCodeResponseDto;
import margo.grid.store.app.dto.ResetPasswordDto;
import margo.grid.store.app.dto.UserDto;

public interface AuthService {
    void register(UserDto dto);

    LoginResponseDto login(UserDto dto, HttpServletRequest request);

    ResetCodeResponseDto getResetCode(String email);

    void resetPassword(ResetPasswordDto dto);
}
