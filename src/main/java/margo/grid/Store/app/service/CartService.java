package margo.grid.store.app.service;

import margo.grid.store.app.dto.CartDto;
import margo.grid.store.app.dto.ItemToCartRequestDto;
import margo.grid.store.app.entity.Item;
import java.util.List;
import java.util.UUID;

public interface CartService {
    void addItem(ItemToCartRequestDto dto);

    void modifyItem(ItemToCartRequestDto dto);

    void removeItem(UUID id);

    CartDto getCart();

    List<Item> getAllItemsInCart();
}
