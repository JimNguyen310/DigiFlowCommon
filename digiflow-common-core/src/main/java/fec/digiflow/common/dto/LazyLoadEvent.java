package fec.digiflow.common.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fec.digiflow.common.utils.FiltersDeserializer;

import java.util.List;
import java.util.Map;

public class LazyLoadEvent {

    private int first;
    private int rows;
    private String sortField;
    private int sortOrder;

    @JsonDeserialize(using = FiltersDeserializer.class)
    private Map<String, List<FilterMetadata>> filters;

    private String globalFilter;

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Map<String, List<FilterMetadata>> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, List<FilterMetadata>> filters) {
        this.filters = filters;
    }

    public String getGlobalFilter() {
        return globalFilter;
    }

    public void setGlobalFilter(String globalFilter) {
        this.globalFilter = globalFilter;
    }

    public static class FilterMetadata {
        private Object value;
        private String matchMode;
        private String operator;

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getMatchMode() {
            return matchMode;
        }

        public void setMatchMode(String matchMode) {
            this.matchMode = matchMode;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }
    }
}
