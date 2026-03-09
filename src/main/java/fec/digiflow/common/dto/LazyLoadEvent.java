package fec.digiflow.common.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fec.digiflow.common.utils.FiltersDeserializer;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LazyLoadEvent {

    private int first;
    private int rows;
    private String sortField;
    private int sortOrder;

    @JsonDeserialize(using = FiltersDeserializer.class)
    private Map<String, List<FilterMetadata>> filters;

    private String globalFilter;

    @Data
    public static class FilterMetadata {
        private Object value;
        private String matchMode;
        private String operator;
    }
}
