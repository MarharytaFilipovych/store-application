package margo.grid.store.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import margo.grid.store.app.dto.ForgotPasswordDto;
import margo.grid.store.app.dto.ResetPasswordDto;
import margo.grid.store.app.dto.UserDto;
import margo.grid.store.app.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody UserDto dto){
        authService.register(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody UserDto dto, HttpServletRequest request){
        return ResponseEntity.ok().body(authService.login(dto, request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordDto dto){
        return ResponseEntity.ok().body(authService.getResetCode(dto.getEmail()));
    }

    @PostMapping("reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordDto dto){
        authService.resetPassword(dto);
        return ResponseEntity.ok().build();
    }
}
