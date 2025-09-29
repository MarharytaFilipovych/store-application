package margo.grid.store.app.service.impl;

import jakarta.persistence.EntityNotFoundException;
import margo.grid.store.app.dto.OrderResponseDto;
import margo.grid.store.app.entity.Item;
import margo.grid.store.app.entity.Order;
import margo.grid.store.app.entity.OrderStatus;
import margo.grid.store.app.entity.User;
import margo.grid.store.app.mapper.OrderMapper;
import margo.grid.store.app.repository.OrderRepository;
import margo.grid.store.app.repository.UserRepository;
import margo.grid.store.app.service.CartService;
import margo.grid.store.app.utils.MyUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import java.util.*;

import static margo.grid.store.app.testdata.AuthTestDataProvider.createOtherUser;
import static margo.grid.store.app.testdata.AuthTestDataProvider.createTestUser;
import static margo.grid.store.app.testdata.ItemTestDataProvider.getTestItems;
import static margo.grid.store.app.testdata.OrderTestDataProvider.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private CartService cartService;
    @Mock private MyUserDetails userDetails;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Captor
    private ArgumentCaptor<Order> orderArgumentCaptor;

    private User user;
    private User otherUser;
    private List<Item> items;
    private Order order;
    private OrderResponseDto orderResponseDto;
    private List<Order> orders;
    private List<OrderResponseDto> orderResponseDtos;

    @BeforeEach
    void setUp() {
        user =  createTestUser();

        otherUser = createOtherUser();

        items = getTestItems();
        order = createOrder(user, items);

        orderResponseDto = createOrderResponseDto(order);
        orders = getTestOrders(user, items);
        orderResponseDtos = getOrderResponseDtos(orders);

        lenient().when(userDetails.getId()).thenReturn(user.getId());
        lenient().when(userDetails.getUsername()).thenReturn(user.getEmail());
    }

    @Test
    void getOrderById_withValidIdAndOwner_shouldReturnOrder() {
        // Arrange
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);

        // Act
        OrderResponseDto result = orderService.getOrderById(order.getId(), userDetails);

        // Assert
        assertNotNull(result);
        assertEquals(orderResponseDto.getId(), result.getId());
        assertEquals(orderResponseDto.getStatus(), result.getStatus());
        assertEquals(orderResponseDto.getTotal(), result.getTotal());

        verify(orderRepository).findById(order.getId());
        verify(orderMapper).toDto(order);
    }

    @Test
    void getOrderById_withNonExistentId_shouldThrowException() {
        // Arrange
        when(orderRepository.findById(order.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> orderService.getOrderById(order.getId(), userDetails));

        verify(orderRepository).findById(order.getId());
        verify(orderMapper, never()).toDto(order);
    }

    @Test
    void getOrderById_withDifferentUser_shouldThrowAccessDeniedException() {
        // Arrange
        order.setUser(otherUser);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> orderService.getOrderById(order.getId(), userDetails));

        verify(orderRepository).findById(order.getId());
        verify(orderMapper, never()).toDto(order);
    }

    @Test
    void createOrder_withValidUser_shouldCreateAndReturnOrder() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(cartService.getAllItemsInCart()).thenReturn(items);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);

        // Act
        OrderResponseDto result = orderService.createOrder(userDetails);

        // Assert
        assertNotNull(result);
        assertEquals(orderResponseDto.getId(), result.getId());
        assertEquals(orderResponseDto.getStatus(), result.getStatus());

        verify(userRepository).findById(user.getId());
        verify(cartService).getAllItemsInCart();
        verify(orderRepository).save(orderArgumentCaptor.capture());
        verify(orderMapper).toDto(order);

        Order savedOrder = orderArgumentCaptor.getValue();
        assertEquals(OrderStatus.CONFIRMED, savedOrder.getStatus());
        assertEquals(user, savedOrder.getUser());
        assertEquals(items.size(), savedOrder.getItems().size());
    }

    @Test
    void createOrder_withNonExistentUser_shouldThrowException() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> orderService.createOrder(userDetails));

        verify(userRepository).findById(user.getId());
        verify(cartService, never()).getAllItemsInCart();
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_withValidIdAndOwner_shouldCancelOrder() {
        // Arrange
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        // Act
        orderService.cancelOrder(order.getId(), userDetails);

        // Assert
        verify(orderRepository).findById(order.getId());
        verify(orderRepository).save(orderArgumentCaptor.capture());

        Order cancelledOrder = orderArgumentCaptor.getValue();
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
    }

    @Test
    void cancelOrder_withNonExistentId_shouldThrowException() {
        // Arrange
        when(orderRepository.findById(order.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> orderService.cancelOrder(order.getId(), userDetails));

        verify(orderRepository).findById(order.getId());
        verify(orderRepository, never()).save(order);
    }

    @Test
    void cancelOrder_withDifferentUser_shouldThrowAccessDeniedException() {
        // Arrange
        order.setUser(otherUser);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> orderService.cancelOrder(order.getId(), userDetails));

        verify(orderRepository).findById(order.getId());
        verify(orderRepository, never()).save(order);
    }

    @Test
    void cancelOrder_withAlreadyCancelledOrder_shouldThrowException() {
        // Arrange
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> orderService.cancelOrder(order.getId(), userDetails));

        verify(orderRepository).findById(order.getId());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getAllUserOrders_withValidUser_shouldReturnUserOrders() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(orderRepository.findOrdersByUser_IdAndStatus_(user.getId(), OrderStatus.CONFIRMED, pageable))
                .thenReturn(orderPage);

        for (int i = 0; i < orders.size(); i++) {
            when(orderMapper.toDto(orders.get(i))).thenReturn(orderResponseDtos.get(i));
        }

        // Act
        Page<OrderResponseDto> result = orderService.getAllUserOrders(userDetails, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(orders.size(), result.getContent().size());

        verify(userRepository).findById(user.getId());
        verify(orderRepository).findOrdersByUser_IdAndStatus_(user.getId(), OrderStatus.CONFIRMED, pageable);
        verify(orderMapper, times(orders.size())).toDto(any(Order.class));
    }

    @Test
    void getAllUserOrders_withEmptyResult_shouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(orderRepository.findOrdersByUser_IdAndStatus_(user.getId(), OrderStatus.CONFIRMED, pageable))
                .thenReturn(emptyPage);

        // Act
        Page<OrderResponseDto> result = orderService.getAllUserOrders(userDetails, pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(userRepository).findById(user.getId());
        verify(orderRepository).findOrdersByUser_IdAndStatus_(user.getId(), OrderStatus.CONFIRMED, pageable);
        verify(orderMapper, never()).toDto(any(Order.class));
    }

    @Test
    void getAllUserOrders_withNonExistentUser_shouldThrowException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class,
                () -> orderService.getAllUserOrders(userDetails, pageable));

        verify(userRepository).findById(user.getId());
        verify(orderRepository, never()).findOrdersByUser_IdAndStatus_(user.getId(), OrderStatus.CONFIRMED, pageable);
    }
}