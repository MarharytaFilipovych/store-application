package margo.grid.store.app.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import margo.grid.store.app.dto.LoginResponseDto;
import margo.grid.store.app.dto.ResetCodeResponseDto;
import margo.grid.store.app.dto.ResetPasswordDto;
import margo.grid.store.app.dto.UserDto;
import margo.grid.store.app.entity.ResetCode;
import margo.grid.store.app.entity.User;
import margo.grid.store.app.exception.UserAlreadyExistsException;
import margo.grid.store.app.repository.ResetCodeRepository;
import margo.grid.store.app.repository.UserRepository;
import margo.grid.store.app.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final ResetCodeRepository resetCodeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${reset-code.expiration-time}")
    private Duration resetCodeLifeDuration;

    @Override
    public void register(UserDto dto) {
        if(userRepository.existsByEmail(dto.getEmail())){
            throw new UserAlreadyExistsException(dto.getEmail());
        }
        User user = User.builder()
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .build();
        userRepository.save(user);
    }

    @Override
    public LoginResponseDto login(UserDto dto, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken
                (dto.getEmail(), dto.getPassword());
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        HttpSession session = request.getSession(true);
        return new LoginResponseDto(session.getId());
    }

    @Override
    public ResetCodeResponseDto getResetCode(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new EntityNotFoundException("User with email " + email + " does not exist!"));
        return new ResetCodeResponseDto(resetCodeRepository.save
                        (new ResetCode(user, LocalDateTime.now().plus(resetCodeLifeDuration)))
                .getCode());
    }

    @Override
    public void resetPassword(ResetPasswordDto dto) {
        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow(() ->
                new EntityNotFoundException("User with email " + dto.getEmail() + " does not exist!"));
        if(resetCodeRepository.existsByCodeAndExpiresAtAfter(dto.getResetCode(), LocalDateTime.now())){
            user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
            userRepository.save(user);
        }
    }
}
