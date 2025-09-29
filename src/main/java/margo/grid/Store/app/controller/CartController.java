package margo.grid.store.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import margo.grid.store.app.dto.CartDto;
import margo.grid.store.app.dto.ItemToCartRequestDto;
import margo.grid.store.app.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

import static margo.grid.store.app.config.PathConstants.CART_ITEMS_PATH;

@RestController
@RequestMapping(CART_ITEMS_PATH)
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<UUID> addItemToCart(@Valid @RequestBody ItemToCartRequestDto dto){
       cartService.addItem(dto);
       return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> modifyItemInCart(@Valid @RequestBody ItemToCartRequestDto dto){
        cartService.modifyItem(dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UUID> deleteItemFromCart(@PathVariable UUID id){
        cartService.removeItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<CartDto> getAllItemsInCart(){
        return ResponseEntity.ok().body(cartService.getCart());
    }
}
