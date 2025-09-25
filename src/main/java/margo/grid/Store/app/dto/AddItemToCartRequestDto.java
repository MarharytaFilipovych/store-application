package margo.grid.Store.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.UUID;

@Data
public class AddItemToCartRequestDto {
    @NotNull
    private UUID itemId;

    @NotNull
    @Min(1)
    @Max(1000)
    private Integer quantity;
}
