package margo.grid.store.app.controller;

import jakarta.persistence.EntityNotFoundException;
import margo.grid.store.app.dto.OrderResponseDto;
import margo.grid.store.app.entity.OrderStatus;
import margo.grid.store.app.service.OrderService;
import margo.grid.store.app.utils.MyUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import org.springframework.test.web.servlet.ResultActions;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import static margo.grid.store.app.testdata.OrderTestDataProvider.createOrderResponseDto;
import static margo.grid.store.app.testdata.OrderTestDataProvider.getOrderResponseDtos;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        performAuthenticatedPostRequest().andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("orders/" + orderResponseDto.getId())))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(orderResponseDto.getId().toString()))
                .andExpect(jsonPath("$.status").value(OrderStatus.CONFIRMED.toString()))
                .andExpect(jsonPath("$.total").value(orderResponseDto.getTotal()));

        verifyCreateOrderCaptureAndAssert();
    }

    @Test
    void createOrder_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        performUnauthenticatedPostRequest().andExpect(status().isUnauthorized());

        verify(orderService, never()).createOrder(userDetails);
    }

    @Test
    void getOrder_shouldReturnSpecificOrder() throws Exception {
        // Arrange
        UUID testOrderId = orderResponseDto.getId();
        when(orderService.getOrderById(testOrderId, userDetails)).thenReturn(orderResponseDto);

        // Act & Assert
        performAuthenticatedGetByIdRequest(testOrderId)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testOrderId.toString()))
                .andExpect(jsonPath("$.status").value(OrderStatus.CONFIRMED.toString()))
                .andExpect(jsonPath("$.total").value(orderResponseDto.getTotal()))
                .andExpect(jsonPath("$.date").exists());

        verifyGetOrderByIdCaptureAndAssert(testOrderId);
    }

    @ParameterizedTest
    @MethodSource("provideGetOrderExceptionScenarios")
    void getOrder_withException_shouldReturnExpectedStatus(
            Exception exception, int expectedStatus) throws Exception {
        // Arrange
        when(orderService.getOrderById(orderId, userDetails)).thenThrow(exception);

        // Act & Assert
        performAuthenticatedGetByIdRequest(orderId).andExpect(status().is(expectedStatus));

        verify(orderService).getOrderById(orderId, userDetails);
    }

    @Test
    void getOrder_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        performUnauthenticatedGetByIdRequest(orderId).andExpect(status().isUnauthorized());

        verify(orderService, never()).getOrderById(orderId, userDetails);
    }

    @Test
    void cancelOrder_shouldCancelOrderAndReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(orderService).cancelOrder(orderId, userDetails);

        // Act & Assert
        performAuthenticatedPatchRequest(orderId).andExpect(status().isNoContent());

        verifyCancelOrderCaptureAndAssert();
    }

    @ParameterizedTest
    @MethodSource("provideCancelOrderExceptionScenarios")
    void cancelOrder_withException_shouldReturnExpectedStatus(
            Exception exception, int expectedStatus) throws Exception {
        // Arrange
        doThrow(exception).when(orderService).cancelOrder(orderId, userDetails);

        // Act & Assert
        performAuthenticatedPatchRequest(orderId).andExpect(status().is(expectedStatus));

        verify(orderService).cancelOrder(orderId, userDetails);
    }

    @Test
    void cancelOrder_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        performUnauthenticatedPatchRequest(orderId)
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).cancelOrder(orderId, userDetails);
    }
    @Test
    void getAllUserOrders_shouldReturnAllUserOrders() throws Exception {
        // Arrange
        configurePageable(orderResponseDtos);

        // Act & Assert
        performAuthenticatedGetAllRequest(20, 2)
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
    }

    @Test
    void getAllUserOrders_withDefaultPagination_shouldReturnOrders() throws Exception {
        // Arrange
        configurePageable(orderResponseDtos);

        // Act & Assert
        performAuthenticatedGetAllRequest()
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.meta").exists())
                .andExpect(jsonPath("$.meta.page_size").value(10))
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(orderResponseDtos.size()));

        captureAndCheckPageable(0, 10);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidPaginationScenarios")
    void getAllUserOrders_withInvalidPagination_shouldReplaceWithDefaultPagination(
            int page, int size) throws Exception {
        // Arrange
        configurePageable(orderResponseDtos);

        // Act & Assert
        performAuthenticatedGetAllRequest(size, page)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.meta").exists())
                .andExpect(jsonPath("$.meta.page_size").value(10))
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(orderResponseDtos.size()));

        captureAndCheckPageable(0, 10);
    }

    @Test
    void getAllUserOrders_whenUserHasNoOrders_shouldReturnEmptyPage() throws Exception {
        // Arrange
        configurePageable(new ArrayList<>());

        // Act & Assert
        performAuthenticatedGetAllRequest()
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.meta").exists())
                .andExpect(jsonPath("$.meta.page_size").value(10))
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.content").isEmpty());

        captureAndCheckPageable(0, 10);
    }

    @Test
    void getAllUserOrders_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        performUnauthenticatedGetAllRequest().andExpect(status().isUnauthorized());

        verify(orderService, never()).getAllUserOrders(any(), any());
    }

    private ResultActions performAuthenticatedPostRequest() throws Exception {
        return mockMvc.perform(post("/orders")
                .with(user(userDetails))
                .accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performUnauthenticatedPostRequest() throws Exception {
        return mockMvc.perform(post("/orders")
                .accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performAuthenticatedGetByIdRequest(UUID id) throws Exception {
        return mockMvc.perform(get("/orders/{id}", id)
                .with(user(userDetails))
                .accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performUnauthenticatedGetByIdRequest(UUID id) throws Exception {
        return mockMvc.perform(get("/orders/{id}", id)
                .accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performAuthenticatedPatchRequest(UUID id) throws Exception {
        return mockMvc.perform(patch("/orders/{id}", id)
                .with(user(userDetails))
                .accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performUnauthenticatedPatchRequest(UUID id) throws Exception {
        return mockMvc.perform(patch("/orders/{id}", id)
                .accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performAuthenticatedGetAllRequest() throws Exception {
        return mockMvc.perform(get("/orders")
                .with(user(userDetails))
                .accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performAuthenticatedGetAllRequest(int size, int page) throws Exception {
        return mockMvc.perform(get("/orders")
                .with(user(userDetails))
                .param("size", String.valueOf(size))
                .param("page", String.valueOf(page))
                .accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performUnauthenticatedGetAllRequest() throws Exception {
        return mockMvc.perform(get("/orders")
                .accept(MediaType.APPLICATION_JSON));
    }

    private void verifyCreateOrderCaptureAndAssert() {
        verify(orderService).createOrder(userDetailsArgumentCaptor.capture());
        assertEquals(userId, userDetailsArgumentCaptor.getValue().getId());
    }

    private void verifyGetOrderByIdCaptureAndAssert(UUID expectedOrderId) {
        verify(orderService).getOrderById(uuidArgumentCaptor.capture(), userDetailsArgumentCaptor.capture());
        assertEquals(expectedOrderId, uuidArgumentCaptor.getValue());
        assertEquals(userId, userDetailsArgumentCaptor.getValue().getId());
    }

    private void verifyCancelOrderCaptureAndAssert() {
        verify(orderService).cancelOrder(uuidArgumentCaptor.capture(), userDetailsArgumentCaptor.capture());
        assertEquals(orderId, uuidArgumentCaptor.getValue());
        assertEquals(userId, userDetailsArgumentCaptor.getValue().getId());
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
        assertEquals(userId, userDetailsArgumentCaptor.getValue().getId());
    }

    private static Stream<Arguments> provideGetOrderExceptionScenarios() {
        UUID testOrderId = UUID.randomUUID();
        return Stream.of(
                Arguments.of(new EntityNotFoundException("Order with id: " + testOrderId + " was not found!"), 404),
                Arguments.of(new AccessDeniedException("You can only view your own orders!"), 403)
        );
    }

    private static Stream<Arguments> provideCancelOrderExceptionScenarios() {
        UUID testOrderId = UUID.randomUUID();
        return Stream.of(
                Arguments.of(new EntityNotFoundException("Order with id: " + testOrderId + " was not found!"), 404),
                Arguments.of(new AccessDeniedException("You can only cancel your own orders!"), 403),
                Arguments.of(new IllegalStateException("Order is already cancelled"), 400)
        );
    }

    private static Stream<Arguments> provideInvalidPaginationScenarios() {
        return Stream.of(
                Arguments.of(-1, -5),
                Arguments.of(-10, -1),
                Arguments.of(0, -3),
                Arguments.of(-2, 0)
        );
    }
}