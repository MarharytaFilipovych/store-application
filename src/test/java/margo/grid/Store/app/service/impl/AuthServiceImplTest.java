package margo.grid.store.app.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import margo.grid.store.app.dto.LoginResponseDto;
import margo.grid.store.app.dto.ResetCodeResponseDto;
import margo.grid.store.app.dto.ResetPasswordDto;
import margo.grid.store.app.dto.UserDto;
import margo.grid.store.app.entity.ResetCode;
import margo.grid.store.app.entity.User;
import margo.grid.store.app.exception.UserAlreadyExistsException;
import margo.grid.store.app.repository.ResetCodeRepository;
import margo.grid.store.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static margo.grid.store.app.testdata.AuthTestDataProvider.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private ResetCodeRepository resetCodeRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private HttpServletRequest httpServletRequest;
    @Mock private HttpSession httpSession;
    @Mock private Authentication authentication;
    @InjectMocks private AuthServiceImpl authService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Captor
    private ArgumentCaptor<ResetCode> resetCodeArgumentCaptor;

    @Captor
    private ArgumentCaptor<UsernamePasswordAuthenticationToken> authTokenCaptor;

    private UserDto userDto;
    private User user;
    private ResetPasswordDto resetPasswordDto;
    private UUID resetCode;
    private String sessionId;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        userDto = createUserDto();
        user = createTestUser();
        resetCode = UUID.randomUUID();
        resetPasswordDto = createResetPasswordDto(resetCode);
        sessionId = UUID.randomUUID().toString();
        encodedPassword = PASSWORD_HASH;
        ReflectionTestUtils.setField(authService, "resetCodeLifeDuration", Duration.ofMinutes(7));
    }

    @Test
    void register_withValidData_shouldSaveUser() {
        // Arrange
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        authService.register(userDto);

        // Assert
        verify(userRepository).existsByEmail(userDto.getEmail());
        verify(passwordEncoder).encode(userDto.getPassword());
        verify(userRepository).save(userArgumentCaptor.capture());

        User savedUser = userArgumentCaptor.getValue();
        assertEquals(userDto.getEmail(), savedUser.getEmail());
        assertEquals(encodedPassword, savedUser.getPasswordHash());
    }

    @Test
    void register_withExistingEmail_shouldThrowException() {
        // Arrange
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(userDto));

        verify(userRepository).existsByEmail(userDto.getEmail());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_withValidCredentials_shouldReturnSessionId() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(httpServletRequest.getSession(true)).thenReturn(httpSession);
        when(httpSession.getId()).thenReturn(sessionId);

        // Act
        LoginResponseDto result = authService.login(userDto, httpServletRequest);

        // Assert
        assertEquals(sessionId, result.getSessionId());
        verify(authenticationManager).authenticate(authTokenCaptor.capture());

        UsernamePasswordAuthenticationToken capturedToken = authTokenCaptor.getValue();
        assertEquals(userDto.getEmail(), capturedToken.getPrincipal());
        assertEquals(userDto.getPassword(), capturedToken.getCredentials());

        verify(httpServletRequest).getSession(true);
        verify(httpSession).getId();
    }

    @Test
    void login_withInvalidCredentials_shouldThrowException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(userDto, httpServletRequest));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    void getResetCode_withValidEmail_shouldReturnResetCode() {
        // Arrange
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(user));
        when(resetCodeRepository.save(any(ResetCode.class))).thenAnswer(invocation -> {
            ResetCode rc = invocation.getArgument(0);
            rc.setCode(resetCode);
            return rc;
        });

        // Act
        ResetCodeResponseDto result = authService.getResetCode(userDto.getEmail());

        // Assert
        assertEquals(resetCode, result.getResetCode());
        verify(userRepository).findByEmail(userDto.getEmail());
        verify(resetCodeRepository).save(resetCodeArgumentCaptor.capture());

        ResetCode capturedResetCode = resetCodeArgumentCaptor.getValue();
        assertEquals(user, capturedResetCode.getUser());
        assertNotNull(capturedResetCode.getExpiresAt());
        assertTrue(capturedResetCode.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void getResetCode_withNonExistentEmail_shouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> authService.getResetCode(userDto.getEmail())
        );

        assertTrue(exception.getMessage().contains(userDto.getEmail()));
        verify(userRepository).findByEmail(userDto.getEmail());
        verify(resetCodeRepository, never()).save(any());
    }

    @Test
    void resetPassword_withValidCodeAndEmail_shouldUpdatePassword() {
        // Arrange
        resetPasswordDto.setResetCode(resetCode);
        when(userRepository.findByEmail(resetPasswordDto.getEmail())).thenReturn(Optional.of(user));
        when(resetCodeRepository.existsByCodeAndExpiresAtAfter(eq(resetCode), any(LocalDateTime.class)))
                .thenReturn(true);
        when(passwordEncoder.encode(resetPasswordDto.getNewPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        authService.resetPassword(resetPasswordDto);

        // Assert
        verify(userRepository).findByEmail(resetPasswordDto.getEmail());
        verify(resetCodeRepository).existsByCodeAndExpiresAtAfter(eq(resetCode), any(LocalDateTime.class));
        verify(passwordEncoder).encode(resetPasswordDto.getNewPassword());
        verify(userRepository).save(userArgumentCaptor.capture());

        User updatedUser = userArgumentCaptor.getValue();
        assertEquals(encodedPassword, updatedUser.getPasswordHash());
    }

    @Test
    void resetPassword_withNonExistentEmail_shouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(resetPasswordDto.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> authService.resetPassword(resetPasswordDto)
        );

        assertTrue(exception.getMessage().contains(resetPasswordDto.getEmail()));
        verify(userRepository).findByEmail(resetPasswordDto.getEmail());
        verify(resetCodeRepository, never()).existsByCodeAndExpiresAtAfter(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_withExpiredCode_shouldNotUpdatePassword() {
        // Arrange
        when(userRepository.findByEmail(resetPasswordDto.getEmail())).thenReturn(Optional.of(user));
        when(resetCodeRepository.existsByCodeAndExpiresAtAfter(eq(resetCode), any(LocalDateTime.class)))
                .thenReturn(false);

        // Act
        authService.resetPassword(resetPasswordDto);

        // Assert
        verify(userRepository).findByEmail(resetPasswordDto.getEmail());
        verify(resetCodeRepository).existsByCodeAndExpiresAtAfter(eq(resetCode), any(LocalDateTime.class));
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_withInvalidCode_shouldNotUpdatePassword() {
        // Arrange
        when(userRepository.findByEmail(resetPasswordDto.getEmail())).thenReturn(Optional.of(user));
        when(resetCodeRepository.existsByCodeAndExpiresAtAfter(eq(resetCode), any(LocalDateTime.class)))
                .thenReturn(false);

        // Act
        authService.resetPassword(resetPasswordDto);

        // Assert
        verify(resetCodeRepository).existsByCodeAndExpiresAtAfter(eq(resetCode), any(LocalDateTime.class));
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}