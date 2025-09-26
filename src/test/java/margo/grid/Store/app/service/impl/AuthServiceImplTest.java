package margo.grid.store.app.service.impl;

import margo.grid.store.app.dto.UserDto;
import margo.grid.store.app.repository.ResetCodeRepository;
import margo.grid.store.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private ResetCodeRepository resetCodeRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private final Duration resetCodeLifeDuration = Duration.of(7, ChronoUnit.MINUTES);

    @InjectMocks
    AuthServiceImpl authService;

    @BeforeEach
    void setUp() {

    }

    @Test
    void register() {
    }

    @Test
    void login() {
    }

    @Test
    void getResetCode() {
    }

    @Test
    void resetPassword() {
    }
}