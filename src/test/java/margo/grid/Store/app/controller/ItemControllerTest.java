package margo.grid.store.app.controller;

import jakarta.persistence.EntityNotFoundException;
import margo.grid.store.app.dto.ItemResponseDto;
import margo.grid.store.app.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static margo.grid.store.app.testdata.ItemTestDataProvider.getItemResponseDtos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @Captor
    private ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableArgumentCaptor;

    private ItemResponseDto itemResponseDto;
    private List<ItemResponseDto> itemResponseDtos;

    @BeforeEach
    void setUp() {
        itemResponseDtos = getItemResponseDtos();
        itemResponseDto = itemResponseDtos.getFirst();
    }

    @Test
    void getAllStoreItems_shouldReturnAllStoreItems() throws Exception {
        // Arrange
        configurePageable(itemResponseDtos);

        // Act & Assert
        mockMvc.perform(get("/items")
                        .param("size", "20")
                        .param("page", "3")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.meta").exists())
                .andExpect(jsonPath("$.meta.page_size").value(20))
                .andExpect(jsonPath("$.meta.page").value(3))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(itemResponseDtos.size()))
                .andExpect(jsonPath("$.content[0].title").value(itemResponseDtos.getFirst().getTitle()))
                .andExpect(jsonPath("$.content[0].price").value(itemResponseDtos.getFirst().getPrice()));

        captureAndCheckPageable(3, 20);
    }

    @Test
    void getAllStoreItems_withDefaultPagination_shouldReturnItems() throws Exception {
        // Arrange
        configurePageable(itemResponseDtos);

        // Act & Assert
        mockMvc.perform(get("/items")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.meta").exists())
                .andExpect(jsonPath("$.meta.page_size").value(10))
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(itemResponseDtos.size()))
                .andExpect(jsonPath("$.content[0].title").value(itemResponseDtos.getFirst().getTitle()))
                .andExpect(jsonPath("$.content[0].price").value(itemResponseDtos.getFirst().getPrice()));

        captureAndCheckPageable(0, 10);
    }

    @Test
    void getAllStoreItems_withInvalidPagination_shouldRelaceThemWithDefaultPagination() throws Exception {
        configurePageable(itemResponseDtos);

        mockMvc.perform(get("/items")
                        .param("page", "-1")
                        .param("size", "-3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.meta").exists())
                .andExpect(jsonPath("$.meta.page_size").value(10))
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(itemResponseDtos.size()))
                .andExpect(jsonPath("$.content[0].title").value(itemResponseDtos.getFirst().getTitle()))
                .andExpect(jsonPath("$.content[0].price").value(itemResponseDtos.getFirst().getPrice()));

        captureAndCheckPageable(0, 10);
    }

    @Test
    void getAllStoreItems_whenThereAreNoItems_shouldReturnEmptyPage() throws Exception {
        configurePageable(new ArrayList<>());

        mockMvc.perform(get("/items")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.meta").exists())
                .andExpect(jsonPath("$.meta.page_size").value(10))
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.content").isEmpty());

        captureAndCheckPageable(0, 10);
    }

    @Test
    void getItemById_shouldReturnSpecificItem() throws Exception {
        // Arrange
        UUID itemId = UUID.randomUUID();
        when(itemService.getItemById(itemId)).thenReturn(itemResponseDto);

        // Act & Assert
        mockMvc.perform(get("/items/{id}", itemId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(itemResponseDto.getId().toString()))
                .andExpect(jsonPath("$.title").value(itemResponseDto.getTitle()))
                .andExpect(jsonPath("$.price").value(itemResponseDto.getPrice()))
                .andExpect(jsonPath("$.available_quantity").value(itemResponseDto.getAvailableQuantity()));

        verify(itemService).getItemById(uuidArgumentCaptor.capture());
        assertEquals(itemId, uuidArgumentCaptor.getValue());
    }

    @Test
    void getItemById_whenItemDoesNotExist_shouldReturnNotFoundStatus() throws Exception {
        // Arrange
        UUID itemId = UUID.randomUUID();
        when(itemService.getItemById(itemId)).thenThrow(EntityNotFoundException.class);

        // Act & Assert
        mockMvc.perform(get("/items/{id}", itemId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(itemService).getItemById(uuidArgumentCaptor.capture());
        assertEquals(itemId, uuidArgumentCaptor.getValue());
    }

    private void configurePageable(List<ItemResponseDto> itemResponseDtos){
        when(itemService.getItems(any(Pageable.class)))
                .thenAnswer(invocation -> {
                    Pageable pageable = invocation.getArgument(0);
                    return new PageImpl<>(itemResponseDtos, pageable, itemResponseDtos.size());
                });

    }

    private void captureAndCheckPageable(int pageNumber, int pageSize){
        verify(itemService).getItems(pageableArgumentCaptor.capture());
        Pageable capturedPageable = pageableArgumentCaptor.getValue();
        assertEquals(pageNumber, capturedPageable.getPageNumber());
        assertEquals(pageSize, capturedPageable.getPageSize());
    }
}