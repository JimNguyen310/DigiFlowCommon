package fec.digiflow.common.utils;

import fec.digiflow.common.dto.LazyLoadEvent;
import org.springframework.data.domain.*;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

public final class CustomPageableUtils {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private CustomPageableUtils() {}

    public static Pageable createPageable(LazyLoadEvent event) {
        if (event == null || event.getRows() <= 0) {
            return PageRequest.of(0, DEFAULT_PAGE_SIZE);
        }

        int page = event.getFirst() / event.getRows();
        int size = event.getRows();

        if (StringUtils.hasText(event.getSortField())) {
            Sort.Direction direction = (event.getSortOrder() == 1) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, event.getSortField());
            return PageRequest.of(page, size, sort);
        } else {
            return PageRequest.of(page, size);
        }
    }

    public static <T> Page<T> createPageFromList(List<T> list, LazyLoadEvent eventDTO) {
        Pageable pageable = createPageable(eventDTO);
        if (list == null || list.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        List<T> pageContent = (start <= end) ? list.subList(start, end) : Collections.emptyList();
        return new PageImpl<>(pageContent, pageable, list.size());
    }
}
