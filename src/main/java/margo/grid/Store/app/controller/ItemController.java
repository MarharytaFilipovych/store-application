package margo.grid.store.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import margo.grid.Store.app.service.ItemService;
import margo.grid.store.app.dto.ItemResponseDto;
import margo.grid.store.app.dto.PageResponseDto;
import margo.grid.store.app.dto.PaginationRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<PageResponseDto<ItemResponseDto>> getALlStoreItems(@Valid PaginationRequestDto pagination){
        Page<ItemResponseDto> items = itemService.getItems(pagination.getLimit(), pagination.getOffset());
        return ResponseEntity.ok().body(PageResponseDto.from(items));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDto> getParticularItems(@PathVariable UUID id){
        return ResponseEntity.ok().body(itemService.getItemById(id));
    }
}
