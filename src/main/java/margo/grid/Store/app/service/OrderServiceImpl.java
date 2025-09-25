package margo.grid.Store.app.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import margo.grid.Store.app.dto.ItemResponseDto;
import margo.grid.Store.app.dto.OrderResponseDto;
import margo.grid.Store.app.entity.OrderStatus;
import margo.grid.Store.app.mapper.OrderMapper;
import margo.grid.Store.app.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public OrderResponseDto getOrderById(UUID id) {
        return orderMapper.toDto(
                orderRepository.findById(id)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @Override
    public OrderResponseDto createOrder() {

        return null;
    }

    @Override
    public void cancelOrder(UUID id) {
        orderRepository.findById(id).ifPresentOrElse(order -> {
            order.setStatus(OrderStatus.CANCELLED);
        }, EntityNotFoundException::new);
    }

    @Override
    public Page<OrderResponseDto> getAllUserOrders(Integer limit, Integer offset) {
        return null;
    }
}
