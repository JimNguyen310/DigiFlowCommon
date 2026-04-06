package fec.digiflow.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PagedResponse<T>(
        @Schema(description = "List of content in the current page") List<T> content,
        @Schema(description = "Current page number (0-based)") int page,
        @Schema(description = "Size of the page") int size,
        @Schema(description = "Total number of elements") long totalElements,
        @Schema(description = "Total number of pages") int totalPages,
        @Schema(description = "Is this the last page?") boolean last,
        @Schema(description = "Is this the first page?") boolean first
) {
    // Note: Mapping logic from Spring Data's `Page<T>` will reside in digiflow-common-jpa
}
