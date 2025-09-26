package margo.grid.store.app.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import margo.grid.store.app.dto.ItemResponseDto;
import margo.grid.store.app.mapper.ItemMapper;
import margo.grid.store.app.repository.ItemRepository;
import margo.grid.store.app.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    public Page<ItemResponseDto> getItems(Integer limit, Integer offset) {
        PageRequest pageRequest = PageRequest.of(limit, offset);
        return itemRepository.findAll(pageRequest).map(itemMapper::toItemResponseDto);
    }

    @Override
    public ItemResponseDto getItemById(UUID id) {
        return itemRepository.findById(id)
                .map(itemMapper::toItemResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Item with id " + id + " was not found!"));
    }
}
