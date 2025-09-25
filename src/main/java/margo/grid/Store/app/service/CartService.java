package margo.grid.Store.app.service;

import margo.grid.Store.app.dto.CartResponseDto;
import margo.grid.Store.app.dto.ItemToCartRequestDto;
import margo.grid.Store.app.dto.CartItemResponseDto;

import java.util.List;
import java.util.UUID;

public interface CartService {
    void addItem(ItemToCartRequestDto dto);

    void modifyItem(ItemToCartRequestDto dto);

    void removeItem(UUID id);

    CartResponseDto getAllItems();
}
