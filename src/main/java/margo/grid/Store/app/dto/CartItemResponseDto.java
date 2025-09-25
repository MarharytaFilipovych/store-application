package margo.grid.Store.app.dto;

import lombok.Data;
import org.hibernate.validator.constraints.UUID;

@Data
public class CartItemResponseDto {
    private UUID id;
    private Integer ordinal;
    private Integer quantity;
}
