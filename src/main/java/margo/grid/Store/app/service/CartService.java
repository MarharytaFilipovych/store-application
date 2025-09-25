package margo.grid.Store.app.service;

import margo.grid.Store.app.dto.CartDto;
import margo.grid.Store.app.dto.ItemToCartRequestDto;
import margo.grid.Store.app.entity.Item;

import java.util.List;
import java.util.UUID;

public interface CartService {
    void addItem(ItemToCartRequestDto dto);

    void modifyItem(ItemToCartRequestDto dto);

    void removeItem(UUID id);

    CartDto getCart();

    List<Item> getAllItemsInCart();
}
