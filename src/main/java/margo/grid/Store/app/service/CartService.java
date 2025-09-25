package margo.grid.Store.app.service;

import margo.grid.Store.app.dto.AddItemToCartRequestDto;
import margo.grid.Store.app.dto.CartItemResponseDto;

import java.util.List;
import java.util.UUID;

public interface CartService {
    UUID addItem(AddItemToCartRequestDto dto);

    UUID modifyItem(AddItemToCartRequestDto dto);

    void removeItem(UUID id);

    List<CartItemResponseDto> getAllItems(AddItemToCartRequestDto dto);
}
