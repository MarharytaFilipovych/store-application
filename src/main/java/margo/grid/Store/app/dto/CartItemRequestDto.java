package margo.grid.Store.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.UUID;

@Data
public class CartItemRequestDto {
    @NotNull
    private UUID id;

    @NotNull
    @Min(0)
    @Max(1000)
    private Integer quantity;
}
