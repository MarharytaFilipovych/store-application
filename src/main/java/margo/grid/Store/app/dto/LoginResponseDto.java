package margo.grid.Store.app.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class LoginResponseDto {
    private UUID sessionId;
}
