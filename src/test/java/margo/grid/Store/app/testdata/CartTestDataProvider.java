package margo.grid.store.app.testdata;

import margo.grid.store.app.dto.CartDto;
import margo.grid.store.app.dto.CartItemResponseDto;
import margo.grid.store.app.dto.ItemToCartRequestDto;
import margo.grid.store.app.entity.Item;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CartTestDataProvider {

    public static ItemToCartRequestDto createItemToCartRequest(UUID itemId, Integer quantity) {
        return ItemToCartRequestDto.builder()
                .itemId(itemId)
                .quantity(quantity)
                .build();
    }

    public static ItemToCartRequestDto createItemToCartRequest(Item item, Integer quantity) {
        return createItemToCartRequest(item.getId(), quantity);
    }

    public static ItemToCartRequestDto createItemToCartRequest(UUID itemId) {
        return createItemToCartRequest(itemId, 1);
    }

    public static CartItemResponseDto createCartItemResponseDto(UUID itemId, String title, Integer quantity, 
                                                                 BigDecimal price, Integer ordinal) {
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
        
        return CartItemResponseDto.builder()
                .itemId(itemId)
                .title(title)
                .quantity(quantity)
                .price(price)
                .subtotal(subtotal)
                .ordinal(ordinal)
                .build();
    }

    public static CartItemResponseDto createCartItemResponseDto(Item item, Integer quantity, Integer ordinal) {
        return createCartItemResponseDto(
                item.getId(),
                item.getTitle(),
                quantity,
                item.getPrice(),
                ordinal
        );
    }

    public static List<CartItemResponseDto> createCartItemResponseDtos() {
        List<Item> items = ItemTestDataProvider.getTestItems();
        List<CartItemResponseDto> cartItems = new ArrayList<>();
        cartItems.add(createCartItemResponseDto(items.get(0), 2, 1));
        cartItems.add(createCartItemResponseDto(items.get(1), 1, 2));
        cartItems.add(createCartItemResponseDto(items.get(2), 3, 3));
        return cartItems;
    }

    public static CartDto createCartDto(List<CartItemResponseDto> items) {
        BigDecimal totalPrice = items.stream()
                .map(CartItemResponseDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Integer totalQuantity = items.stream()
                .map(CartItemResponseDto::getQuantity)
                .reduce(0, Integer::sum);
        
        return CartDto.builder()
                .items(items)
                .totalPrice(totalPrice)
                .totalQuantity(totalQuantity)
                .build();
    }

    public static CartDto createCartDto() {
        return createCartDto(createCartItemResponseDtos());
    }

    public static CartDto createEmptyCart() {
        return CartDto.builder()
                .items(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .totalQuantity(0)
                .build();
    }
}