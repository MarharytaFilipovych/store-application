package margo.grid.store.app.testdata;

import margo.grid.store.app.dto.OrderResponseDto;
import margo.grid.store.app.entity.Item;
import margo.grid.store.app.entity.Order;
import margo.grid.store.app.entity.OrderStatus;
import margo.grid.store.app.entity.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class OrderTestDataProvider {

    public static List<Order> getTestOrders(User user, List<Item> items) {
        List<Order> orders = new ArrayList<>();
        new Random(42);
        for (int i = 0; i < 5; i++) {
            Order order = Order.builder()
                    .id(UUID.randomUUID())
                    .status(i % 4 == 0 ? OrderStatus.CANCELLED : OrderStatus.CONFIRMED)
                    .user(user)
                    .items(new HashSet<>(items.subList(0, Math.min(3, items.size()))))
                    .build();
            orders.add(order);
        }
        return orders;
    }

    public static List<Order> getTestOrders(User user) {
        return getTestOrders(user, ItemTestDataProvider.getTestItems());
    }

    public static List<OrderResponseDto> getOrderResponseDtos() {
        List<OrderResponseDto> orders = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            orders.add(createOrderResponseDto(
                    UUID.randomUUID(),
                    OrderStatus.CONFIRMED,
                    new BigDecimal(100 + i * 50),
                    LocalDateTime.now().minusDays(i)
            ));
        }
        
        return orders;
    }

    public static List<OrderResponseDto> getOrderResponseDtos(List<Order> orders) {
        return orders.stream()
                .map(OrderTestDataProvider::createOrderResponseDto)
                .toList();
    }

    private static OrderResponseDto createOrderResponseDto(UUID id, OrderStatus status, BigDecimal total, LocalDateTime date) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(id);
        dto.setStatus(status);
        dto.setTotal(total);
        dto.setDate(date);
        return dto;
    }

    public static OrderResponseDto createOrderResponseDto(Order order) {
        BigDecimal total = order.getItems().stream()
                .map(Item::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return createOrderResponseDto(
                order.getId(),
                order.getStatus(),
                total,
                order.getCreatedAt() != null ? order.getCreatedAt().toLocalDateTime() : LocalDateTime.now()
        );
    }

    public static OrderResponseDto createOrderResponseDto() {
        return createOrderResponseDto(
                UUID.randomUUID(),
                OrderStatus.CONFIRMED,
                new BigDecimal("299.99"),
                LocalDateTime.now()
        );
    }

    public static Order createOrder(User user, List<Item> items) {
        return Order.builder()
                .id(UUID.randomUUID())
                .status(OrderStatus.CONFIRMED)
                .user(user)
                .items(new HashSet<>(items))
                .build();
    }

    public static Order createOrder(User user) {
        return createOrder(user, ItemTestDataProvider.getTestItems().subList(0, 3));
    }
}