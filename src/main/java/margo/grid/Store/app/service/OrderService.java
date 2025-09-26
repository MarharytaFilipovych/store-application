package margo.grid.store.app.service;


import margo.grid.store.app.dto.OrderResponseDto;
import margo.grid.store.app.utils.MyUserDetails;
import org.springframework.data.domain.Page;
import java.util.UUID;

public interface OrderService {

    OrderResponseDto getOrderById(UUID id, MyUserDetails user);

    OrderResponseDto createOrder(MyUserDetails user);

    void cancelOrder(UUID id, MyUserDetails user);

    Page<OrderResponseDto> getAllUserOrders(MyUserDetails user, Integer limit, Integer offset);
}
