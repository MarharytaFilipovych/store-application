package margo.grid.store.app.testdata;

import margo.grid.store.app.dto.ResetPasswordDto;
import margo.grid.store.app.dto.UserDto;
import margo.grid.store.app.entity.User;
import java.util.UUID;

public class AuthTestDataProvider {

    public static UserDto createUserDto() {
        return UserDto.builder()
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .build();
    }

    public static ResetPasswordDto createResetPasswordDto(UUID resetCode) {
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setEmail(VALID_EMAIL);
        dto.setResetCode(resetCode);
        dto.setNewPassword(VALID_PASSWORD);
        return dto;
    }


    public static User createTestUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email(VALID_EMAIL)
                .passwordHash(PASSWORD_HASH)
                .build();
    }

    public final static String VALID_EMAIL = "margosha@gmail.com";
    public final static String PASSWORD_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMye7VpkVfPxOWYC/JwQPO.DRk2CqyG1x4O";
    public final static String VALID_PASSWORD = "NewValidP@8**";
}