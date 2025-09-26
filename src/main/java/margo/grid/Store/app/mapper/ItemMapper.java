package margo.grid.store.app.mapper;

import margo.grid.store.app.dto.ItemResponseDto;
import margo.grid.store.app.entity.Item;
import org.mapstruct.Mapper;

@Mapper
public interface ItemMapper {

    ItemResponseDto toItemResponseDto(Item item);
}
