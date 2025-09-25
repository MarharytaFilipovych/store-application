package margo.grid.Store.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ItemResponseDto {
    private UUID id;
    private String title;

    @JsonProperty("available_quantity")
    private Integer availableQuantity;
    private BigDecimal price;
}
