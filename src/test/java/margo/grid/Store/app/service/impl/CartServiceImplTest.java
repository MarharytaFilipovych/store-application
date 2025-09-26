package margo.grid.store.app.service.impl;

import jakarta.persistence.EntityNotFoundException;
import margo.grid.store.app.dto.CartDto;
import margo.grid.store.app.dto.CartItemResponseDto;
import margo.grid.store.app.dto.ItemToCartRequestDto;
import margo.grid.store.app.entity.Item;
import margo.grid.store.app.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.*;
import static margo.grid.store.app.testdataa.ItemTestDataProvider.getTestItems;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Item item;
    private ItemToCartRequestDto cartRequest;
    private UUID itemId;
    private List<Item> items;

    @BeforeEach
    void setUp() {
        items = getTestItems();
        item = items.getFirst();
        itemId = item.getId();
        cartRequest = ItemToCartRequestDto.builder()
                .quantity(1)
                .itemId(itemId)
                .build();
    }


    @Test
    void addItem_withValidQuantity_shouldAddItemAndDecreaseStock() {
        // Arrange
        setupMocks(item);
        int originalQuantity = item.getAvailableQuantity();

        // Act
        cartService.addItem(cartRequest);

        // Assert
        verifyInteractions(item);
        assertEquals(originalQuantity - cartRequest.getQuantity(), item.getAvailableQuantity());
    }

    @Test
    void addItem_withInsufficientQuantity_shouldThrowException() {
        // Arrange
        cartRequest.setQuantity(item.getAvailableQuantity() + 2);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> cartService.addItem(cartRequest));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void addItem_withNonExistentItem_shouldThrowException() {
        // Arrange
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> cartService.addItem(cartRequest));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void addItem_withExactAvailableQuantity_shouldBeAdded() {
        // Arrange
        cartRequest.setQuantity(item.getAvailableQuantity());
        setupMocks(item);

        // Act
        cartService.addItem(cartRequest);

        // Assert
        verifyInteractions(item);
        assertEquals(0, item.getAvailableQuantity());
    }

    @Test
    void modifyItem_increasingQuantity_shouldUpdateStock() {
        // Arrange
        int initialStock = 78;
        int currentCartQuantity = 5;
        int newCartQuantity = 15;

        item.setAvailableQuantity(initialStock);
        addToCart(itemId, currentCartQuantity);
        cartRequest.setQuantity(newCartQuantity);
        setupMocks(item);

        // Act
        cartService.modifyItem(cartRequest);

        // Assert
        verifyInteractions(item);
        assertEquals(68, item.getAvailableQuantity()); // 78 - (15-5)
    }

    @Test
    void modifyItem_decreasingQuantity_shouldUpdateStock() {
        // Arrange
        int initialStock = 78;
        int currentCartQuantity = 20;
        int newCartQuantity = 10;

        item.setAvailableQuantity(initialStock);
        addToCart(itemId, currentCartQuantity);
        cartRequest.setQuantity(newCartQuantity);
        setupMocks(item);

        // Act
        cartService.modifyItem(cartRequest);

        // Assert
        verifyInteractions(item);
        assertEquals(88, item.getAvailableQuantity()); // 78 + (20-10)
    }

    @Test
    void modifyItem_withItemNotInCart_shouldThrowException() {
        // Arrange
        emptyCart();

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> cartService.modifyItem(cartRequest));
        verify(itemRepository, never()).findById(any());
    }

    @Test
    void modifyItem_withInsufficientStock_shouldThrowException() {
        // Arrange
        addToCart(itemId, 5);
        item.setAvailableQuantity(3);
        cartRequest.setQuantity(10);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> cartService.modifyItem(cartRequest));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void removeItem_whenExists_shouldRemoveAndUpdateStock() {
        // Arrange
        int cartQuantity = 5;
        int originalQuantity = item.getAvailableQuantity();

        addToCart(itemId, cartQuantity);
        setupMocks(item);

        // Act
        cartService.removeItem(itemId);

        // Assert
        verifyInteractions(item);
        assertEquals(originalQuantity + cartQuantity, item.getAvailableQuantity());
    }

    @Test
    void removeItem_whenNotInCart_shouldThrowException() {
        // Arrange
        emptyCart();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> cartService.removeItem(itemId));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void removeItem_whenItemNotInDatabase_shouldThrowException() {
        // Arrange
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> cartService.removeItem(itemId));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void getCart_whenEmpty_shouldReturnEmptyCart() {
        // Arrange
        emptyCart();

        // Act
        CartDto result = cartService.getCart();

        // Assert
        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, result.getTotalPrice());
        assertEquals(0, result.getTotalQuantity());
    }

    @Test
    void getCart_withSingleItem_shouldReturnCorrectCart() {
        // Arrange
        int cartQuantity = 3;
        BigDecimal expectedSubtotal = item.getPrice().multiply(BigDecimal.valueOf(cartQuantity));

        addToCart(itemId, cartQuantity);
        when(itemRepository.findAllById(any())).thenReturn(List.of(item));

        // Act
        CartDto result = cartService.getCart();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getItems().size());

        CartItemResponseDto cartItem = result.getItems().getFirst();
        assertEquals(itemId, cartItem.getItemId());
        assertEquals(item.getTitle(), cartItem.getTitle());
        assertEquals(cartQuantity, cartItem.getQuantity());
        assertEquals(expectedSubtotal, cartItem.getSubtotal());
        assertEquals(1, cartItem.getOrdinal());

        assertEquals(expectedSubtotal, result.getTotalPrice());
        assertEquals(cartQuantity, result.getTotalQuantity());
    }

    @Test
    void getCart_withMultipleItems_shouldReturnCorrectCart() {
        // Arrange
        Item secondItem = items.get(1);
        UUID secondItemId = secondItem.getId();

        addMultipleToCart(itemId, 2, secondItemId, 5);
        when(itemRepository.findAllById(any())).thenReturn(List.of(item, secondItem));

        BigDecimal expectedTotal = item.getPrice().multiply(BigDecimal.valueOf(2))
                .add(secondItem.getPrice().multiply(BigDecimal.valueOf(5)));

        // Act
        CartDto result = cartService.getCart();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        assertEquals(expectedTotal, result.getTotalPrice());
        assertEquals(7, result.getTotalQuantity());
    }


    @Test
    void getAllItemsInCart_withEmptyCart_shouldReturnEmptyList() {
        // Arrange
        emptyCart();
        when(itemRepository.findAllById(any())).thenReturn(Collections.emptyList());

        // Act
        List<Item> result = cartService.getAllItemsInCart();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllItemsInCart_withItems_shouldReturnItems() {
        // Arrange
        Item secondItem = items.get(1);
        UUID secondItemId = secondItem.getId();

        addMultipleToCart(itemId, 2, secondItemId, 3);
        when(itemRepository.findAllById(any())).thenReturn(List.of(item, secondItem));

        // Act
        List<Item> result = cartService.getAllItemsInCart();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(item, secondItem)));
    }

    private void setupMocks(Item item) {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
    }

    private void verifyInteractions(Item item) {
        verify(itemRepository).findById(item.getId());
        verify(itemRepository).save(item);
    }

    private void addToCart(UUID itemId, Integer quantity) {
        Map<UUID, Integer> cartItems = new HashMap<>();
        cartItems.put(itemId, quantity);
        setCart(cartItems);
    }

    private void addMultipleToCart(UUID firstId, int firstQty, UUID secondId, int secondQty) {
        Map<UUID, Integer> cartItems = new HashMap<>();
        cartItems.put(firstId, firstQty);
        cartItems.put(secondId, secondQty);
        setCart(cartItems);
    }

    private void emptyCart() {
        setCart(new HashMap<>());
    }

    private void setCart(Map<UUID, Integer> cartItems) {
        try {
            var field = CartServiceImpl.class.getDeclaredField("cartItems");
            field.setAccessible(true);
            field.set(cartService, cartItems);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set cart items for testing", e);
        }
    }
}