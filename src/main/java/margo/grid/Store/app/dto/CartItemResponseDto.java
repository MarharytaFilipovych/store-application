package margo.grid.Store.app.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CartItemResponseDto {
    private UUID itemId;
    private String title;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    private Integer ordinal;
}