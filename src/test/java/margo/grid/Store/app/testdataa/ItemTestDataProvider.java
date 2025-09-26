package margo.grid.store.app.testdataa;

import margo.grid.store.app.dto.ItemResponseDto;
import margo.grid.store.app.entity.Item;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ItemTestDataProvider {

    public static List<Item> getTestItems() {
        return Arrays.asList(
                createItem("MacBook Pro 16-inch", "2499.99", 15),
                createItem("iPhone 15 Pro", "999.99", 25),
                createItem("Sony WH-1000XM5 Headphones", "399.99", 40),
                createItem("Samsung 4K Monitor 32-inch", "549.99", 18),
                createItem("Mechanical Gaming Keyboard", "149.99", 35),
                createItem("Wireless Gaming Mouse", "89.99", 50),
                createItem("iPad Air 5th Generation", "599.99", 22),
                createItem("Dell XPS 13 Laptop", "1299.99", 12),
                createItem("AirPods Pro 2nd Generation", "249.99", 60),
                createItem("Nintendo Switch OLED", "349.99", 30)
        );
    }

    public static List<ItemResponseDto> getItemResponseDtos(List<Item> items){
        return items.stream().map(ItemTestDataProvider::createItemResponseDto).toList();
    }

    public static List<ItemResponseDto> getItemResponseDtos(){
        return getTestItems().stream().map(ItemTestDataProvider::createItemResponseDto).toList();
    }

    private static Item createItem(String title, String price, int quantity) {
        return Item.builder()
                .id(UUID.randomUUID())
                .title(title)
                .price(new BigDecimal(price))
                .availableQuantity(quantity)
                .build();
    }

    private static ItemResponseDto createItemResponseDto(Item item) {
        ItemResponseDto dto = new ItemResponseDto();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setPrice(item.getPrice());
        dto.setAvailableQuantity(item.getAvailableQuantity());
        return dto;
    }
}