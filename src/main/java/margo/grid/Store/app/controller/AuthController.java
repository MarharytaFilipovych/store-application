package margo.grid.Store.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import margo.grid.Store.app.dto.ResetPasswordDto;
import margo.grid.Store.app.dto.UserDto;
import margo.grid.Store.app.dto.LoginResponseDto;
import margo.grid.Store.app.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
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

    @PostMapping
    public ResponseEntity<String> forgotPassword(@Email String email){
        return ResponseEntity.ok().body(authService.getResetCode(email));
    }

    @PostMapping
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordDto dto){
        authService.resetPassword(dto);
        return ResponseEntity.ok().build();
    }
}
