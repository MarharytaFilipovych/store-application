package margo.grid.store.app.mapper;

import margo.grid.Store.app.dto.ItemResponseDto;
import margo.grid.Store.app.entity.Item;
import org.mapstruct.Mapper;

@Mapper
public interface ItemMapper {

    ItemResponseDto toItemResponseDto(Item item);
}
