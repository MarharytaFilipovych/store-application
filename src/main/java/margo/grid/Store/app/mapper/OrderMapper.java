package margo.grid.store.app.mapper;


import margo.grid.store.app.dto.OrderResponseDto;
import margo.grid.store.app.entity.Item;
import margo.grid.store.app.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper
public interface OrderMapper {
    @Mapping(target = "date", source = "createdAt")
    @Mapping(target = "total", expression = "java(calculateTotal(order))")
    OrderResponseDto toDto(Order order);

    default BigDecimal calculateTotal(Order order) {
        return order.getItems().stream()
                .map(Item::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
