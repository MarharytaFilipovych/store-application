package margo.grid.Store.app.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import margo.grid.Store.app.dto.OrderResponseDto;
import margo.grid.Store.app.entity.Order;
import margo.grid.Store.app.entity.OrderStatus;
import margo.grid.Store.app.entity.User;
import margo.grid.Store.app.mapper.OrderMapper;
import margo.grid.Store.app.repository.OrderRepository;
import margo.grid.Store.app.repository.UserRepository;
import margo.grid.Store.app.utils.MyUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final CartService cartService;

    @Override
    public OrderResponseDto getOrderById(UUID id, MyUserDetails userDetails) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order with id: " + id + " was not found!"));

        if (!order.getUser().getId().equals(userDetails.getId())) {
            throw new AccessDeniedException("You can only view your own orders!");
        }

        return orderMapper.toDto(order);
    }

    @Override
    public OrderResponseDto createOrder(MyUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        Order order = Order.builder()
                .status(OrderStatus.CONFIRMED)
                .user(user)
                .items(new HashSet<>(cartService.getAllItemsInCart()))
                .build();
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    public void cancelOrder(UUID id, MyUserDetails user) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order with id: " + id + " was not found!"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only cancel your own orders!");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Override
    public Page<OrderResponseDto> getAllUserOrders(MyUserDetails userDetails, Integer limit, Integer offset) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        return new PageImpl<>(orderRepository.findOrdersByUser_IdAndStatus_(user.getId(), OrderStatus.CONFIRMED,
                PageRequest.of(limit, offset)).map(orderMapper::toDto).getContent());
    }
}
