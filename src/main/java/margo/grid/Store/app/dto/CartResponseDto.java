package margo.grid.Store.app.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponseDto {
    private List<CartItemResponseDto> items;
    private BigDecimal totalPrice;
    private Integer totalQuantity;
}
