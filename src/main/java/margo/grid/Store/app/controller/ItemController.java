package margo.grid.Store.app.controller;

import lombok.RequiredArgsConstructor;
import margo.grid.Store.app.dto.ItemResponseDto;
import margo.grid.Store.app.dto.PageResponseDto;
import margo.grid.Store.app.dto.PaginationRequestDto;
import margo.grid.Store.app.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<PageResponseDto<ItemResponseDto>> getALlStoreItems(PaginationRequestDto pagination){
        Page<ItemResponseDto> items = orderService.getItems(pagination.getLimit(), pagination.getOffset());
        return ResponseEntity.ok().body(PageResponseDto.from(items));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDto> getParticularItems(@PathVariable UUID id){
        ItemResponseDto item = orderService.getItemById(id);
        return ResponseEntity.ok().body(item);
    }
}
