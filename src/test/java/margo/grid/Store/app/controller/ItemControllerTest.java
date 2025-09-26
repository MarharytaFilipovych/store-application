package margo.grid.store.app.controller;

import margo.grid.store.app.config.RateLimitSettings;
import margo.grid.store.app.config.SecurityConfig;
import margo.grid.store.app.dto.ItemResponseDto;
import margo.grid.store.app.filter.RateLimitFilter;
import margo.grid.store.app.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@Import(SecurityConfig.class)
class ItemControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ItemService itemService;
    @MockitoBean
    RateLimitSettings rateLimitSettings;
    @MockitoBean
    RateLimitFilter rateLimitFilter;
    @MockitoBean
    AuthenticationManager authenticationManager;

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    ArgumentCaptor<Pageable> pageableArgumentCaptor;

    private ItemResponseDto itemResponseDto;
    private List<ItemResponseDto> itemResponseDtos;

    @BeforeEach
    void setUp() {
        itemResponseDtos = getItemResponseDtos();
        itemResponseDto = itemResponseDtos.getFirst();
    }

    @Test
    void getAllStoreItems_returnsAllStoreItems() throws Exception {
        // Arrange
        when(itemService.getItems(PageRequest.of(1, 10)))
                .thenReturn(new PageImpl<>(itemResponseDtos));

        // Act & Assert
        mockMvc.perform(get("/items")
                        .param("size", "10")
                        .param("page", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.meta").exists())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(itemResponseDtos.size()))
                .andExpect(jsonPath("$.content[0].title").value(itemResponseDtos.getFirst().getTitle()))
                .andExpect(jsonPath("$.content[0].price").value(itemResponseDtos.getFirst().getPrice()));

        verify(itemService).getItems(pageableArgumentCaptor.capture());
        assertEquals(PageRequest.of(1, 10), pageableArgumentCaptor.getValue());
    }

    @Test
    void getItemById_returnsSpecificItem() throws Exception {
        // Arrange
        UUID itemId = UUID.randomUUID();
        when(itemService.getItemById(itemId)).thenReturn(itemResponseDto);

        // Act & Assert
        mockMvc.perform(get("/items/{id}", itemId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(itemResponseDto.getTitle()))
                .andExpect(jsonPath("$.price").value(itemResponseDto.getPrice()))
                .andExpect(jsonPath("$.available_quantity").value(itemResponseDto.getAvailableQuantity()));

        verify(itemService).getItemById(uuidArgumentCaptor.capture());
        assertEquals(itemId, uuidArgumentCaptor.getValue());
    }

    @Test
    void getAllStoreItems_withDefaultPagination_returnsItems() throws Exception {
        // Arrange
        when(itemService.getItems(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(itemResponseDtos));

        // Act & Assert
        mockMvc.perform(get("/items")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAllStoreItems_withInvalidPagination_returnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/items")
                        .param("page", "-1")
                        .param("size", "-3")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private static List<ItemResponseDto> getItemResponseDtos() {
        return List.of(
                createItem("Laptop", 5, BigDecimal.valueOf(999.99)),
                createItem("Smartphone", 10, BigDecimal.valueOf(499.50)),
                createItem("Headphones", 20, BigDecimal.valueOf(79.90)),
                createItem("Keyboard", 15, BigDecimal.valueOf(45.00)),
                createItem("Monitor", 7, BigDecimal.valueOf(189.99))
        );
    }

    private static ItemResponseDto createItem(String title, Integer availableQuantity, BigDecimal price) {
        ItemResponseDto dto = new ItemResponseDto();
        dto.setId(UUID.randomUUID());
        dto.setTitle(title);
        dto.setAvailableQuantity(availableQuantity);
        dto.setPrice(price);
        return dto;
    }
}