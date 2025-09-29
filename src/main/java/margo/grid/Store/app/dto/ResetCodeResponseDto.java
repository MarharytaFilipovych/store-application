package margo.grid.store.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ResetCodeResponseDto {

    @JsonProperty("reset_code")
    private UUID resetCode;
}
