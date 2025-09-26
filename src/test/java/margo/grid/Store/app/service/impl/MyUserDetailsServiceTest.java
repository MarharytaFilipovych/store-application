package margo.grid.store.app.service.impl;

import margo.grid.store.app.entity.User;
import margo.grid.store.app.repository.UserRepository;
import margo.grid.store.app.utils.MyUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyUserDetailsServiceTest {
    @Mock private UserRepository userRepository;

    @InjectMocks
    MyUserDetailsService myUserDetailsService;

    @Test
    void loadUserByUsername_ifEmailIsFound_shouldLoadCorrectUserDetails() {
        // Arrange
        User user = User.builder()
                .email("test@exampl.com")
                .id(UUID.randomUUID())
                .passwordHash("$2a$10$N9qo8uLOickgx2ZMRZoMye7VpkVfPxOWYC/JwQPO.DRk2CqyG1x4O")
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername(user.getEmail());

        // Assert
        assertNotNull(userDetails);
        assertEquals(userDetails.getUsername(), user.getEmail());
        assertEquals(userDetails.getId(), user.getId());
        assertEquals(userDetails.getPassword(), user.getPasswordHash());
    }

    @Test
    void loadUserByUsername_ifEmailIsNotFound_shouldThrowException() {
        // Arrange
        String nonExistentEmail = "some email";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () ->
                myUserDetailsService.loadUserByUsername(nonExistentEmail));
    }
}