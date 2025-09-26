package margo.grid.store.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class MetaDto {
    private int page = 0;

    @JsonProperty("total_count")
    private long totalCount = 0;

    @JsonProperty("page_size")
    private int pageSize = 0;

    @JsonProperty("total_pages")
    private int totalPages = 0;

    @JsonProperty("has_next")
    private boolean hasNext = false;

    @JsonProperty("has_previous")
    private boolean hasPrevious = false;

    public MetaDto(Page<?> page) {
        if (page != null) {
            this.page = page.getNumber();
            this.totalCount = page.getTotalElements();
            this.pageSize = page.getSize();
            this.totalPages = page.getTotalPages();
            this.hasNext = page.hasNext();
            this.hasPrevious = page.hasPrevious();
        }
    }
}