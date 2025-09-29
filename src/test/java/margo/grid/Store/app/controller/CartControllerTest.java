package margo.grid.store.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import margo.grid.store.app.dto.CartDto;
import margo.grid.store.app.dto.ItemToCartRequestDto;
import margo.grid.store.app.service.CartService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.util.UUID;
import java.util.stream.Stream;
import static margo.grid.store.app.testdata.CartTestDataProvider.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartService cartService;

    @Captor
    private ArgumentCaptor<ItemToCartRequestDto> itemToCartRequestDtoCaptor;

    @Captor
    private ArgumentCaptor<UUID> uuidArgumentCaptor;

    private MyUserDetails userDetails;
    private UUID itemId;
    private ItemToCartRequestDto itemToCartRequest;
    private CartDto cartDto;
    private String invalidJson;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        userDetails = mock(MyUserDetails.class);
        when(userDetails.getId()).thenReturn(userId);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        itemToCartRequest = createItemToCartRequest(itemId, 5);
        cartDto = createCartDto();
        invalidJson = "invalid{json";
    }

    @Test
    void addItemToCart_withValidRequest_shouldAddItemAndReturnOk() throws Exception {
        // Arrange
        doNothing().when(cartService).addItem(any(ItemToCartRequestDto.class));

        // Act & Assert
        performPostRequest(itemToCartRequest)
                .andExpect(status().isOk());

        verify(cartService).addItem(itemToCartRequestDtoCaptor.capture());
        ItemToCartRequestDto captured = itemToCartRequestDtoCaptor.getValue();
        assertEquals(itemToCartRequest.getItemId(), captured.getItemId());
        assertEquals(itemToCartRequest.getQuantity(), captured.getQuantity());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCartRequests")
    void addItemToCart_withInvalidRequest_shouldReturnBadRequest(ItemToCartRequestDto invalidRequest) throws Exception {
        // Act & Assert
        performPostRequest(invalidRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verify(cartService, never()).addItem(itemToCartRequest);
    }

    @ParameterizedTest
    @MethodSource("provideExceptionScenarios")
    void addItemToCart_withException_shouldReturnExpectedStatus(
            Exception exception, int expectedStatus) throws Exception {
        // Arrange
        doThrow(exception).when(cartService).addItem(any(ItemToCartRequestDto.class));

        // Act & Assert
        performPostRequest(itemToCartRequest)
                .andExpect(status().is(expectedStatus));

        verify(cartService).addItem(itemToCartRequest);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidJsonScenarios")
    void addItemToCart_withInvalidJson_shouldReturnBadRequest(String json) throws Exception {
        // Act & Assert
        performPostRequestWithRawJson(json)
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/cart-items")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        verify(cartService, never()).addItem(itemToCartRequest);
    }

    @Test
    void addItemToCart_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        performUnauthenticatedPostRequest(itemToCartRequest)
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).addItem(any());
    }

    @Test
    void modifyItemInCart_withValidRequest_shouldModifyItemAndReturnNoContent() throws Exception {
        // Arrange
        itemToCartRequest.setQuantity(10);
        doNothing().when(cartService).modifyItem(any(ItemToCartRequestDto.class));

        // Act & Assert
        performPutRequest(itemToCartRequest)
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(cartService).modifyItem(itemToCartRequestDtoCaptor.capture());
        ItemToCartRequestDto captured = itemToCartRequestDtoCaptor.getValue();
        assertEquals(itemToCartRequest.getItemId(), captured.getItemId());
        assertEquals(itemToCartRequest.getQuantity(), captured.getQuantity());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCartRequests")
    void modifyItemInCart_withInvalidRequest_shouldReturnBadRequest(ItemToCartRequestDto invalidRequest) throws Exception {
        // Act & Assert
        performPutRequest(invalidRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verify(cartService, never()).modifyItem(any());
    }

    @ParameterizedTest
    @MethodSource("provideModifyExceptionScenarios")
    void modifyItemInCart_withException_shouldReturnExpectedStatus(
            Exception exception, int expectedStatus) throws Exception {
        // Arrange
        doThrow(exception).when(cartService).modifyItem(itemToCartRequest);

        // Act & Assert
        performPutRequest(itemToCartRequest)
                .andExpect(status().is(expectedStatus));

        verify(cartService).modifyItem(itemToCartRequest);
    }

    @Test
    void modifyItemInCart_withInvalidJson_shouldReturnBadRequest() throws Exception {
        // Act & Assert
        performPutRequestWithRawJson(invalidJson)
                .andExpect(status().isBadRequest());

        verify(cartService, never()).modifyItem(any());
    }

    @Test
    void modifyItemInCart_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        performUnauthenticatedPutRequest(itemToCartRequest)
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).modifyItem(any());
    }

    @Test
    void deleteItemFromCart_withValidId_shouldDeleteItemAndReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(cartService).removeItem(itemId);

        // Act & Assert
        performDeleteRequest(itemId)
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(cartService).removeItem(uuidArgumentCaptor.capture());
        assertEquals(itemId, uuidArgumentCaptor.getValue());
    }

    @Test
    void deleteItemFromCart_withException_shouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException()).when(cartService).removeItem(itemId);

        // Act & Assert
        performDeleteRequest(itemId)
                .andExpect(status().isNotFound());

        verify(cartService).removeItem(itemId);
    }

    @Test
    void deleteItemFromCart_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        performUnauthenticatedDeleteRequest(itemId)
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).removeItem(any());
    }

    @Test
    void getAllItemsInCart_shouldReturnCartDto() throws Exception {
        // Arrange
        when(cartService.getCart()).thenReturn(cartDto);

        // Act & Assert
        performGetRequest()
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(cartDto.getItems().size()))
                .andExpect(jsonPath("$.total_price").value(cartDto.getTotalPrice()))
                .andExpect(jsonPath("$.total_quantity").value(cartDto.getTotalQuantity()));

        verify(cartService).getCart();
    }

    @Test
    void getAllItemsInCart_whenCartIsEmpty_shouldReturnEmptyCart() throws Exception {
        // Arrange
        CartDto emptyCart = createEmptyCart();
        when(cartService.getCart()).thenReturn(emptyCart);

        // Act & Assert
        performGetRequest()
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.total_price").value(0))
                .andExpect(jsonPath("$.total_quantity").value(0));

        verify(cartService).getCart();
    }

    @Test
    void getAllItemsInCart_shouldReturnItemsWithCorrectStructure() throws Exception {
        // Arrange
        when(cartService.getCart()).thenReturn(cartDto);

        // Act & Assert
        performGetRequest()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].item_id").exists())
                .andExpect(jsonPath("$.items[0].title").exists())
                .andExpect(jsonPath("$.items[0].quantity").exists())
                .andExpect(jsonPath("$.items[0].price").exists())
                .andExpect(jsonPath("$.items[0].subtotal").exists())
                .andExpect(jsonPath("$.items[0].ordinal").exists());

        verify(cartService).getCart();
    }

    @Test
    void getAllItemsInCart_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        performUnauthenticatedGetRequest()
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).getCart();
    }

    private ResultActions performPostRequest(Object requestBody) throws Exception {
        return mockMvc.perform(post("/cart-items")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
    }

    private ResultActions performPutRequest(Object requestBody) throws Exception {
        return mockMvc.perform(put("/cart-items")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
    }

    private ResultActions performDeleteRequest(UUID id) throws Exception {
        return mockMvc.perform(delete("/cart-items/{id}", id.toString())
                .with(user(userDetails)));
    }

    private ResultActions performGetRequest() throws Exception {
        return mockMvc.perform(get("/cart-items")
                .with(user(userDetails))
                .accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performPostRequestWithRawJson(String json) throws Exception {
        return mockMvc.perform(post("/cart-items")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions performPutRequestWithRawJson(String json) throws Exception {
        return mockMvc.perform(put("/cart-items")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions performUnauthenticatedPostRequest(ItemToCartRequestDto requestBody) throws Exception {
        return mockMvc.perform(post("/cart-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
    }

    private ResultActions performUnauthenticatedPutRequest(ItemToCartRequestDto requestBody) throws Exception {
        return mockMvc.perform(put("/cart-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
    }

    private ResultActions performUnauthenticatedDeleteRequest(UUID id) throws Exception {
        return mockMvc.perform(delete("/cart-items/{id}", id));
    }

    private ResultActions performUnauthenticatedGetRequest() throws Exception {
        return mockMvc.perform(get("/cart-items")
                .accept(MediaType.APPLICATION_JSON));
    }

    private static Stream<ItemToCartRequestDto> provideInvalidCartRequests() {
        UUID testItemId = UUID.randomUUID();
        return Stream.of(
                ItemToCartRequestDto.builder().itemId(testItemId).quantity(0).build(),
                ItemToCartRequestDto.builder().itemId(testItemId).quantity(-1).build(),
                ItemToCartRequestDto.builder().itemId(testItemId).quantity(1001).build(),
                ItemToCartRequestDto.builder().itemId(null).quantity(5).build(),
                ItemToCartRequestDto.builder().itemId(testItemId).quantity(null).build()
        );
    }

    private static Stream<Arguments> provideExceptionScenarios() {
        UUID testItemId = UUID.randomUUID();
        return Stream.of(
                Arguments.of(new EntityNotFoundException("Item with id: " + testItemId + " was not found"), 404),
                Arguments.of(new IllegalArgumentException("The requested quantity exceeded available quantity!"), 400)
        );
    }

    private static Stream<Arguments> provideModifyExceptionScenarios() {
        UUID testItemId = UUID.randomUUID();
        return Stream.of(
                Arguments.of(new EntityNotFoundException("There is no item in the cart with id: " + testItemId), 404),
                Arguments.of(new IllegalArgumentException("The requested quantity exceeded available quantity!"),400)
        );
    }

    private static Stream<Arguments> provideInvalidJsonScenarios() {
        UUID testItemId = UUID.randomUUID();
        return Stream.of(
                Arguments.of("invalid{json", null),
                Arguments.of("{\"item_id\":\"" + testItemId + "\"}", "missing quantity")
        );
    }
}