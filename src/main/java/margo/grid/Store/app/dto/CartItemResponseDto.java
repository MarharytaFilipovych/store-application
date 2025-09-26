package margo.grid.store.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CartItemResponseDto {
    @JsonProperty("item_id")
    private UUID itemId;
    private String title;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    private Integer ordinal;
}