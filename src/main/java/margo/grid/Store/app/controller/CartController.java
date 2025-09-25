package margo.grid.Store.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import margo.grid.Store.app.dto.AddItemToCartRequestDto;
import margo.grid.Store.app.dto.CartItemResponseDto;
import margo.grid.Store.app.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cart-items")
@Validated
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<UUID> addItemToCart(@Valid @RequestBody AddItemToCartRequestDto dto){
        UUID id = cartService.addItem(dto);
        return ResponseEntity.ok().body(id);
    }

    @PutMapping
    public ResponseEntity<Void> modifyItemInCart(@Valid @RequestBody AddItemToCartRequestDto dto){
        UUID id = cartService.modifyItem(dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UUID> deleteItemFromCart(@PathVariable UUID id){
        cartService.removeItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponseDto>> getAllItemsInCart
            (@RequestBody AddItemToCartRequestDto dto){
        List<CartItemResponseDto> items = cartService.getAllItems(dto);
        return ResponseEntity.ok().body(items);
    }
}
