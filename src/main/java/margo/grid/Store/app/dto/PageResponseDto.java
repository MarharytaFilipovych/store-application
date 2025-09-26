package margo.grid.store.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseDto<T> {
    private MetaDto meta;
    private List<T> content;
    
    public static <T> PageResponseDto<T> from(Page<T> page) {
        return new PageResponseDto<>(new MetaDto(page), page.getContent());
    }
}