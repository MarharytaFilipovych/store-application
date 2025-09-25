package margo.grid.Store.app.service;

import margo.grid.Store.app.dto.OrderResponseDto;
import org.springframework.data.domain.Page;
import java.util.UUID;

public interface OrderService {

    OrderResponseDto getOrderById(UUID id);

    OrderResponseDto createOrder();

    void cancelOrder(UUID id);

    Page<OrderResponseDto> getAllUserOrders(Integer limit, Integer offset);
}
