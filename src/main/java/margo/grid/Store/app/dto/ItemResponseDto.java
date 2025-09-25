package margo.grid.Store.app.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ItemResponseDto {
    private UUID id;
    private String title;
    private Integer availableQuantity;
    private BigDecimal price;
}
