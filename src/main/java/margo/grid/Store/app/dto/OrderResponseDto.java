package margo.grid.Store.app.dto;

import lombok.Data;
import margo.grid.Store.app.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OrderResponseDto {
    private UUID id;
    private LocalDateTime date;
    private BigDecimal total;
    private OrderStatus status;
}
