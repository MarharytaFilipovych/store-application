package margo.grid.store.app.service.impl;

import jakarta.persistence.EntityNotFoundException;
import margo.grid.store.app.dto.ItemResponseDto;
import margo.grid.store.app.entity.Item;
import margo.grid.store.app.mapper.ItemMapper;
import margo.grid.store.app.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static margo.grid.store.app.testdata.ItemTestDataProvider.getItemResponseDtos;
import static margo.grid.store.app.testdata.ItemTestDataProvider.getTestItems;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private List<Item> items;
    private List<ItemResponseDto> itemResponseDtos;
    private Item item;
    private ItemResponseDto itemResponseDto;
    private UUID itemId;

    @BeforeEach
    void setUp() {
        items = getTestItems();
        itemResponseDtos = getItemResponseDtos(items);
        item = items.getFirst();
        itemResponseDto = itemResponseDtos.getFirst();
        itemId = item.getId();

    }

    @Test
    void getItems_shouldReturnAllItemsCorrectlyMapped() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(1, 20);
        when(itemRepository.findAll(pageRequest)).thenReturn(new PageImpl<>(items));

        for (int i = 0; i < items.size(); i++) {
            when(itemMapper.toItemResponseDto(items.get(i))).thenReturn(itemResponseDtos.get(i));
        }

        // Act
        Page<ItemResponseDto> result = itemService.getItems(pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(items.size(), result.getTotalElements());

        List<ItemResponseDto> dtos = result.getContent();
        assertEquals(items.size(), dtos.size());

        verifyAllItemsCorrectlyMapped(items, dtos);
        verify(itemRepository).findAll(pageRequest);
        verify(itemMapper, times(items.size())).toItemResponseDto(any(Item.class));
    }


    @Test
    void getItems_withEmptyPage_shouldReturnEmptyPage() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(1, 20);
        setupGetItemsTest(pageRequest, Collections.emptyList());

        // Act
        Page<ItemResponseDto> result = itemService.getItems(pageRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verifyRepositoryAndMapperInteractions(pageRequest, 0);
    }

    @Test
    void getItemById_withExistingItem_shouldReturnMappedItem() {
        // Arrange
        setupGetItemByIdTest(itemId, item, itemResponseDto);

        // Act
        ItemResponseDto result = itemService.getItemById(itemId);

        // Assert
        assertNotNull(result);
        assertItemResponseDtoCorrect(item, result);

        verify(itemRepository).findById(itemId);
        verify(itemMapper).toItemResponseDto(item);
    }

    @Test
    void getItemById_withNonExistentItem_shouldThrowException() {
        // Arrange
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> itemService.getItemById(itemId));

        verify(itemRepository).findById(itemId);
        verify(itemMapper, never()).toItemResponseDto(item);
    }

    private ItemResponseDto createItemResponseDto(Item item) {
        ItemResponseDto dto = new ItemResponseDto();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setPrice(item.getPrice());
        dto.setAvailableQuantity(item.getAvailableQuantity());
        return dto;
    }

    private void setupGetItemsTest(Pageable pageable, List<Item> itemsToReturn) {
        when(itemRepository.findAll(pageable)).thenReturn(new PageImpl<>(itemsToReturn));
        setupItemMapperMock();
    }

    private void setupGetItemByIdTest(UUID itemId, Item item, ItemResponseDto expectedDto) {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toItemResponseDto(item)).thenReturn(expectedDto);
    }

    private void setupItemMapperMock() {
        lenient().when(itemMapper.toItemResponseDto(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            return itemMapper.toItemResponseDto(item);
        });
    }

    private void verifyAllItemsCorrectlyMapped(List<Item> originalItems, List<ItemResponseDto> mappedDtos) {
        for (int i = 0; i < originalItems.size(); i++) {
            Item originalItem = originalItems.get(i);
            ItemResponseDto mappedDto = mappedDtos.get(i);
            assertItemResponseDtoCorrect(originalItem, mappedDto);
        }
    }

    private void assertItemResponseDtoCorrect(Item originalItem, ItemResponseDto dto) {
        assertEquals(originalItem.getId(), dto.getId());
        assertEquals(originalItem.getTitle(), dto.getTitle());
        assertEquals(originalItem.getPrice(), dto.getPrice());
        assertEquals(originalItem.getAvailableQuantity(), dto.getAvailableQuantity());
    }

    private void verifyRepositoryAndMapperInteractions(Pageable pageable, int expectedMapperCalls) {
        verify(itemRepository).findAll(pageable);
        verify(itemMapper, times(expectedMapperCalls)).toItemResponseDto(any(Item.class));
    }
}