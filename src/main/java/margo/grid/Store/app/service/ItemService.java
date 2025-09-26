package margo.grid.store.app.service;

import margo.grid.store.app.dto.ItemResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ItemService {
    Page<ItemResponseDto> getItems(Pageable pageable);

    ItemResponseDto getItemById(UUID id);
}
