package fec.digiflow.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

public record PagedResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages, boolean last,
                               boolean first) {
    public PagedResponse(@Schema(description = "List of content in the current page") List<T> content, @Schema(description = "Current page number (0-based)") int page, @Schema(description = "Size of the page") int size, @Schema(description = "Total number of elements") long totalElements, @Schema(description = "Total number of pages") int totalPages, @Schema(description = "Is this the last page?") boolean last, @Schema(description = "Is this the first page?") boolean first) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
        this.first = first;
    }

    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<T>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast(), page.isFirst());
    }

    @Schema(
            description = "List of content in the current page"
    )
    public List<T> content() {
        return this.content;
    }

    @Schema(
            description = "Current page number (0-based)"
    )
    public int page() {
        return this.page;
    }

    @Schema(
            description = "Size of the page"
    )
    public int size() {
        return this.size;
    }

    @Schema(
            description = "Total number of elements"
    )
    public long totalElements() {
        return this.totalElements;
    }

    @Schema(
            description = "Total number of pages"
    )
    public int totalPages() {
        return this.totalPages;
    }

    @Schema(
            description = "Is this the last page?"
    )
    public boolean last() {
        return this.last;
    }

    @Schema(
            description = "Is this the first page?"
    )
    public boolean first() {
        return this.first;
    }
}
