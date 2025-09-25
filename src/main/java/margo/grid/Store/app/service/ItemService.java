package margo.grid.Store.app.service;

import margo.grid.Store.app.dto.ItemResponseDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ItemService {
    Page<ItemResponseDto> getItems(Integer limit, Integer offset);

    ItemResponseDto getItemById(UUID id);
}
