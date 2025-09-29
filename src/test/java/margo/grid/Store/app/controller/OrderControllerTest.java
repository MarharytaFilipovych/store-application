package margo.grid.store.app.controller;

import jakarta.persistence.EntityNotFoundException;
import margo.grid.store.app.dto.OrderResponseDto;
import margo.grid.store.app.entity.OrderStatus;
import margo.grid.store.app.service.OrderService;
import margo.grid.store.app.utils.MyUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static margo.grid.store.app.testdata.OrderTestDataProvider.createOrderResponseDto;
import static margo.grid.store.app.testdata.OrderTestDataProvider.getOrderResponseDtos;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Captor
    private ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableArgumentCaptor;

    @Captor
    private ArgumentCaptor<MyUserDetails> userDetailsArgumentCaptor;

    private OrderResponseDto orderResponseDto;
    private List<OrderResponseDto> orderResponseDtos;
    private MyUserDetails userDetails;
    private UUID userId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        userDetails = mock(MyUserDetails.class);
        when(userDetails.getId()).thenReturn(userId);
        when(userDetails.getUsername()).thenReturn("petya@vaanya.com");

        orderResponseDto = createOrderResponseDto();
        orderResponseDtos = getOrderResponseDtos();
    }

    @Test
    void createOrder_shouldCreateOrderAndReturnCreatedStatus() throws Exception {
        // Arrange
        when(orderService.createOrder(userDetails)).thenReturn(orderResponseDto);

        // Act & Assert
        mockMvc.perform(post("/orders")
                        .with(user(userDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("orders/" + orderResponseDto.getId())))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(orderResponseDto.getId().toString()))
                .andExpect(jsonPath("$.status").value(OrderStatus.CONFIRMED.toString()))
                .andExpect(jsonPath("$.total").value(orderResponseDto.getTotal()));

        verify(orderService).createOrder(userDetailsArgumentCaptor.capture());
        assertEquals(userId, userDetailsArgumentCaptor.getValue().getId());
    }

    @Test
    void createOrder_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/orders")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).createOrder(userDetails);
    }

    @Test
    void getOrder_shouldReturnSpecificOrder() throws Exception {
        // Arrange
        UUID testOrderId = orderResponseDto.getId();
        when(orderService.getOrderById(testOrderId, userDetails)).thenReturn(orderResponseDto);

        // Act & Assert
        mockMvc.perform(get("/orders/{id}", testOrderId)
                        .with(user(userDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testOrderId.toString()))
                .andExpect(jsonPath("$.status").value(OrderStatus.CONFIRMED.toString()))
                .andExpect(jsonPath("$.total").value(orderResponseDto.getTotal()))
                .andExpect(jsonPath("$.date").exists());

        verify(orderService).getOrderById(uuidArgumentCaptor.capture(), userDetailsArgumentCaptor.capture());
        assertEquals(testOrderId, uuidArgumentCaptor.getValue());
        assertEquals(userId, userDetailsArgumentCaptor.getValue().getId());
    }

    @Test
    void getOrder_whenOrderDoesNotExist_shouldReturnNotFound() throws Exception {
        // Arrange
        when(orderService.getOrderById(orderId, userDetails))
                .thenThrow(new EntityNotFoundException("Order with id: " + orderId + " was not found!"));

        // Act & Assert
        mockMvc.perform(get("/orders/{id}", orderId)
                        .with(user(userDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order with id: " + orderId + " was not found!"));

        verify(orderService).getOrderById(uuidArgumentCaptor.capture(), userDetailsArgumentCaptor.capture());
        assertEquals(orderId, uuidArgumentCaptor.getValue());
    }

    @Test
    void getOrder_whenAccessDenied_shouldReturnForbidden() throws Exception {
        // Arrange
        when(orderService.getOrderById(orderId, userDetails))
                .thenThrow(new AccessDeniedException("You can only view your own orders!"));

        // Act & Assert
        mockMvc.perform(get("/orders/{id}", orderId)
                        .with(user(userDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(orderService).getOrderById(orderId, userDetails);
    }

    @Test
    void getOrder_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders/{id}", orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).getOrderById(orderId,userDetails);
    }

    @Test
    void cancelOrder_shouldCancelOrderAndReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(orderService).cancelOrder(orderId, userDetails);

        // Act & Assert
        mockMvc.perform(patch("/orders/{id}", orderId)
                        .with(user(userDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(orderService).cancelOrder(uuidArgumentCaptor.capture(), userDetailsArgumentCaptor.capture());
        assertEquals(orderId, uuidArgumentCaptor.getValue());
        assertEquals(userId, userDetailsArgumentCaptor.getValue().getId());
    }

    @Test
    void cancelOrder_whenOrderDoesNotExist_shouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Order with id: " + orderId + " was not found!"))
                .when(orderService).cancelOrder(orderId, userDetails);

        // Act & Assert
        mockMvc.perform(patch("/orders/{id}", orderId)
                        .with(user(userDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order with id: " + orderId + " was not found!"));

        verify(orderService).cancelOrder(orderId, userDetails);
    }

    @Test
    void cancelOrder_whenAccessDenied_shouldReturnForbidden() throws Exception {
        // Arrange
        doThrow(new AccessDeniedException("You can only cancel your own orders!"))
                .when(orderService).cancelOrder(orderId, userDetails);

        // Act & Assert
        mockMvc.perform(patch("/orders/{id}", orderId)
                        .with(user(userDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(orderService).cancelOrder(orderId, userDetails);
    }

    @Test
    void cancelOrder_whenAlreadyCancelled_shouldReturnBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalStateException("Order is already cancelled"))
                .when(orderService).cancelOrder(orderId, userDetails);

        // Act & Assert
        mockMvc.perform(patch("/orders/{id}", orderId)
                        .with(user(userDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Order is already cancelled"));

        verify(orderService).cancelOrder(orderId, userDetails);
    }

    @Test
    void cancelOrder_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/orders/{id}", orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).cancelOrder(orderId, userDetails);
    }

    @Test
    void getAllUserOrders_shouldReturnAllUserOrders() throws Exception {
        // Arrange
        configurePageable(orderResponseDtos);

        // Act & Assert
        mockMvc.perform(get("/orders")
                        .with(user(userDetails))
                        .param("size", "20")
                        .param("page", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.meta").exists())
                .andExpect(jsonPath("$.meta.page_size").value(20))
                .andExpect(jsonPath("$.meta.page").value(2))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(orderResponseDtos.size()))
                .andExpect(jsonPath("$.content[0].id").value(orderResponseDtos.getFirst().getId().toString()))
                .andExpect(jsonPath("$.content[0].status").value(OrderStatus.CONFIRMED.toString()))
                .andExpect(jsonPath("$.content[0].total").value(orderResponseDtos.getFirst().getTotal()));

        captureAndCheckPageable(2, 20);
        assertEquals(userId, userDetailsArgumentCaptor.getValue().getId());
    }

    @Test
    void getAllUserOrders_withDefaultPagination_shouldReturnOrders() throws Exception {
        // Arrange
        configurePageable(orderResponseDtos);

        // Act & Assert
        mockMvc.perform(get("/orders")
                        .with(user(userDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.meta").exists())
                .andExpect(jsonPath("$.meta.page_size").value(10))
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(orderResponseDtos.size()));

        captureAndCheckPageable(0, 10);
        assertEquals(userId, userDetailsArgumentCaptor.getValue().getId());
    }

    @Test
    void getAllUserOrders_withInvalidPagination_shouldReplaceWithDefaultPagination() throws Exception {
        // Arrange
        configurePageable(orderResponseDtos);

        // Act & Assert
        mockMvc.perform(get("/orders")
                        .with(user(userDetails))
                        .param("page", "-1")
                        .param("size", "-5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.meta").exists())
                .andExpect(jsonPath("$.meta.page_size").value(10))
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(orderResponseDtos.size()));

        captureAndCheckPageable(0, 10);
        assertEquals(userId, userDetailsArgumentCaptor.getValue().getId());
    }

    @Test
    void getAllUserOrders_whenUserHasNoOrders_shouldReturnEmptyPage() throws Exception {
        // Arrange
        configurePageable(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/orders")
                        .with(user(userDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.meta").exists())
                .andExpect(jsonPath("$.meta.page_size").value(10))
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.content").isEmpty());

        captureAndCheckPageable(0, 10);
        assertEquals(userId, userDetailsArgumentCaptor.getValue().getId());
    }

    @Test
    void getAllUserOrders_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).getAllUserOrders(any(), any());
    }

    private void configurePageable(List<OrderResponseDto> orderResponseDtos) {
        when(orderService.getAllUserOrders(eq(userDetails), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    Pageable pageable = invocation.getArgument(1);
                    return new PageImpl<>(orderResponseDtos, pageable, orderResponseDtos.size());
                });
    }

    private void captureAndCheckPageable(int pageNumber, int pageSize) {
        verify(orderService).getAllUserOrders(userDetailsArgumentCaptor.capture(), pageableArgumentCaptor.capture());
        Pageable capturedPageable = pageableArgumentCaptor.getValue();
        assertEquals(pageNumber, capturedPageable.getPageNumber());
        assertEquals(pageSize, capturedPageable.getPageSize());
    }
}