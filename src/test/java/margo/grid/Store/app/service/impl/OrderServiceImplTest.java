package margo.grid.store.app.service.impl;

import margo.grid.store.app.dto.OrderResponseDto;
import margo.grid.store.app.entity.Order;
import margo.grid.store.app.mapper.OrderMapper;
import margo.grid.store.app.repository.OrderRepository;
import margo.grid.store.app.repository.UserRepository;
import margo.grid.store.app.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private CartService cartService;

    @InjectMocks
    OrderServiceImpl orderService;

    List<OrderResponseDto> orderResponseDtos;
    List<Order> orders;
    Order order;
    OrderResponseDto orderResponseDto;

    @BeforeEach
    void setUp() {

    }

    @Test
    void getOrderById() {
    }

    @Test
    void createOrder() {
    }

    @Test
    void cancelOrder() {
    }

    @Test
    void getAllUserOrders() {
    }
}