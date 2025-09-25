package margo.grid.Store.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponseDto {
    private List<CartItemResponseDto> items;

    @JsonProperty("total_price")
    private BigDecimal totalPrice;

    @JsonProperty("total_quantity")
    private Integer totalQuantity;
}
