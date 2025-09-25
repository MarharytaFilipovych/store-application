package margo.grid.Store.app.service;

import jakarta.persistence.EntityNotFoundException;
import margo.grid.Store.app.dto.CartItemResponseDto;
import margo.grid.Store.app.dto.CartDto;
import margo.grid.Store.app.dto.ItemToCartRequestDto;
import margo.grid.Store.app.entity.Item;
import margo.grid.Store.app.repository.ItemRepository;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CartServiceImpl implements CartService {
    private final Map<UUID, Integer> cartItems;
    private final ItemRepository itemRepository;


    public CartServiceImpl(ItemRepository itemRepository) {
        this.cartItems = new LinkedHashMap<>();
        this.itemRepository = itemRepository;
    }

    @Override
    public void addItem(ItemToCartRequestDto dto) {
        itemRepository.findById(dto.getItemId()).ifPresentOrElse(
                foundItem -> {
                    if (foundItem.getAvailableQuantity() < dto.getQuantity()) {
                        throw new IllegalArgumentException("The requested quantity exceeded available quantity!");
                    }
                    cartItems.put(dto.getItemId(), dto.getQuantity());
                },
                () -> {
                    throw new EntityNotFoundException("Item with id: " + dto.getItemId() + " was not found");
                }
        );
    }

    @Override
    public void modifyItem(ItemToCartRequestDto dto) {
        itemRepository.findById(dto.getItemId()).ifPresentOrElse(
                foundItem -> {
                    int currentQuantity = cartItems.getOrDefault(dto.getItemId(), 0);
                    if (foundItem.getAvailableQuantity() < currentQuantity + dto.getQuantity()) {
                        throw new IllegalStateException("The requested quantity exceeded available quantity!");
                    }
                    cartItems.merge(dto.getItemId(), dto.getQuantity(), Integer::sum);
                },
                () -> {
                    throw new EntityNotFoundException("Item with id: " + dto.getItemId() + " was not found");
                }
        );
    }

    @Override
    public void removeItem(UUID id) {
        cartItems.remove(id);
    }

    @Override
    public CartDto getCart() {
        List<CartItemResponseDto> items = getCartItemDtos();
        return CartDto.builder()
                .items(items)
                .totalPrice(getTotalPrice(items))
                .totalQuantity(getTotalQuantity(items))
                .build();
    }

    public List<Item> getAllItemsInCart(){
        return itemRepository.findAllById(cartItems.keySet());
    }

    private BigDecimal getSubtotal(BigDecimal price, Integer quantity) {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    private List<CartItemResponseDto> getCartItemDtos(){
        List<UUID> itemIds = new ArrayList<>(cartItems.keySet());
        Map<UUID, Item> itemMap = itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, item -> item));

       return itemIds.stream()
                .map(itemId -> {
                    Item item = itemMap.get(itemId);
                    Integer quantity = cartItems.get(itemId);
                    BigDecimal price = item.getPrice();
                    int ordinal = itemIds.indexOf(itemId) + 1;

                    return CartItemResponseDto.builder()
                            .itemId(itemId)
                            .title(item.getTitle())
                            .price(price)
                            .ordinal(ordinal)
                            .quantity(quantity)
                            .subtotal(getSubtotal(price, quantity))
                            .build();
                })
                .toList();
    }

    private BigDecimal getTotalPrice(List<CartItemResponseDto> items) {
        return items.stream()
                .map(CartItemResponseDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int getTotalQuantity(List<CartItemResponseDto> items) {
        return cartItems.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }
}
