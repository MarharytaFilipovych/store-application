package margo.grid.Store.app.mapper;

import margo.grid.Store.app.dto.CartItemResponseDto;
import margo.grid.Store.app.dto.OrderResponseDto;
import margo.grid.Store.app.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface OrderMapper {
    OrderResponseDto toDto(Order order);
}
