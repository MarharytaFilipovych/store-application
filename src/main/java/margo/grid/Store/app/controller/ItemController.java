package margo.grid.store.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import margo.grid.store.app.service.ItemService;
import margo.grid.store.app.dto.ItemResponseDto;
import margo.grid.store.app.dto.PageResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

import static margo.grid.store.app.config.PathConstants.ITEMS_PATH;

@RestController
@RequestMapping(ITEMS_PATH)
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<PageResponseDto<ItemResponseDto>> getALlStoreItems(@Valid @PageableDefault(sort = "title") Pageable pageable){
        Page<ItemResponseDto> items = itemService.getItems(pageable);
        return ResponseEntity.ok().body(PageResponseDto.from(items));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDto> getParticularItems(@PathVariable UUID id){
        return ResponseEntity.ok().body(itemService.getItemById(id));
    }
}
