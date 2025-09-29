package margo.grid.store.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import margo.grid.store.app.dto.*;
import margo.grid.store.app.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static margo.grid.store.app.config.PathConstants.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(AUTH_PATH)
public class AuthController {
    private final AuthService authService;

    @PostMapping(REGISTER_PATH)
    public ResponseEntity<Void> register(@Valid @RequestBody UserDto dto){
        authService.register(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping(LOGIN_PATH)
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody UserDto dto, HttpServletRequest request){
        return ResponseEntity.ok().body(authService.login(dto, request));
    }

    @PostMapping(FORGOT_PASSWORD_PATH)
    public ResponseEntity<ResetCodeResponseDto> forgotPassword(@Valid @RequestBody ForgotPasswordDto dto){
        return ResponseEntity.ok().body(authService.getResetCode(dto.getEmail()));
    }

    @PostMapping(RESET_PASSWORD_PATH)
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordDto dto){
        authService.resetPassword(dto);
        return ResponseEntity.ok().build();
    }
}
