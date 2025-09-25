package margo.grid.Store.app.dto;

import lombok.Data;
import org.hibernate.validator.constraints.UUID;

import java.math.BigDecimal;

@Data
public class CartItemResponseDto {
    private UUID itemId;
    private String title;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    private Integer ordinal;

    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}