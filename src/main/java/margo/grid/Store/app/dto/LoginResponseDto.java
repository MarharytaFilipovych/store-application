package margo.grid.store.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class LoginResponseDto {
    @JsonProperty("session_id")
    private UUID sessionId;
}
