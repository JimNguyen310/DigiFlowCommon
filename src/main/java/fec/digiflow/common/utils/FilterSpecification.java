package fec.digiflow.common.utils;

import fec.digiflow.common.dto.LazyLoadEvent;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A generic and reusable Specification for filtering JPA entities based on a LazyLoadEventDTO.
 * This implementation is type-safe, robust, and extensible.
 *
 * @param <T> The entity type.
 */
public class FilterSpecification<T> implements Specification<T> {

    private static final String GLOBAL_FILTER_KEY = "global";
    private static final String OPERATOR_OR = "or";

    private final LazyLoadEvent event;

    public FilterSpecification(LazyLoadEvent event) {
        this.event = event;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        if (event == null || event.getFilters() == null || event.getFilters().isEmpty()) {
            return cb.conjunction(); // Returns a predicate that is always true
        }

        // Create a mutable copy to safely remove the global filter
        Map<String, List<LazyLoadEvent.FilterMetadata>> filters = new java.util.HashMap<>(event.getFilters());

        List<Predicate> mainPredicates = new ArrayList<>();

        // 1. Apply Global Filter
        Predicate globalFilterPredicate = createGlobalFilter(root, cb, filters.remove(GLOBAL_FILTER_KEY));
        if (globalFilterPredicate != null) {
            mainPredicates.add(globalFilterPredicate);
        }

        // 2. Apply Field-Specific Filters
        for (Map.Entry<String, List<LazyLoadEvent.FilterMetadata>> entry : filters.entrySet()) {
            String field = entry.getKey();
            List<LazyLoadEvent.FilterMetadata> metadataList = entry.getValue();

            if (metadataList == null || metadataList.isEmpty()) {
                continue;
            }

            List<Predicate> fieldPredicates = metadataList.stream()
                    .map(meta -> createFieldPredicate(root, cb, field, meta))
                    .filter(Objects::nonNull)
                    .toList();

            if (!fieldPredicates.isEmpty()) {
                // The operator (and/or) is taken from the first metadata entry for that field
                String operator = metadataList.get(0).getOperator();
                if (OPERATOR_OR.equalsIgnoreCase(operator)) {
                    mainPredicates.add(cb.or(fieldPredicates.toArray(new Predicate[0])));
                } else {
                    mainPredicates.add(cb.and(fieldPredicates.toArray(new Predicate[0])));
                }
            }
        }

        return cb.and(mainPredicates.toArray(new Predicate[0]));
    }

    /**
     * Creates a predicate for the global filter, searching across all String attributes of the entity.
     */
    private Predicate createGlobalFilter(Root<T> root, CriteriaBuilder cb, List<LazyLoadEvent.FilterMetadata> globalFilterMetadata) {
        if (globalFilterMetadata == null || globalFilterMetadata.isEmpty()) {
            return null;
        }
        Object filterValue = globalFilterMetadata.get(0).getValue();
        if (filterValue == null || !StringUtils.hasText(filterValue.toString())) {
            return null;
        }

        String keyword = "%" + filterValue.toString().toLowerCase() + "%";
        EntityType<T> entityType = root.getModel();

        Predicate[] stringFieldPredicates = entityType.getAttributes().stream()
                .filter(attr -> attr.getPersistentAttributeType() != Attribute.PersistentAttributeType.EMBEDDED)
                .filter(attr -> attr.getJavaType().equals(String.class))
                .map(attr -> cb.like(cb.lower(root.get(attr.getName())), keyword))
                .toArray(Predicate[]::new);

        return cb.or(stringFieldPredicates);
    }

    /**
     * Creates a single predicate for a specific field based on its filter metadata.
     */
    private Predicate createFieldPredicate(Root<T> root, CriteriaBuilder cb, String field, LazyLoadEvent.FilterMetadata meta) {
        if (meta == null || meta.getValue() == null || !StringUtils.hasText(meta.getValue().toString())) {
            return null;
        }

        Path<?> path = getPath(root, field);
        Object convertedValue = convertValueFor(path, meta.getValue());
        if (convertedValue == null) {
            return null; // Could not convert value, skip this filter
        }

        FilterOperation operation = FilterOperation.fromString(meta.getMatchMode());
        return operation.build(cb, path, convertedValue);
    }

    /**
     * Safely resolves a potentially nested entity path (e.g., "customer.name").
     */
    private Path<?> getPath(Root<T> root, String field) {
        if (!field.contains(".")) {
            return root.get(field);
        }
        String[] parts = field.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

    /**
     * Converts the raw filter value from the DTO to the actual Java type of the entity attribute.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object convertValueFor(Path<?> path, Object value) {
        Class<?> attributeType = path.getJavaType();
        String valueStr = value.toString();

        try {
            if (attributeType.equals(LocalDate.class)) {
                return DatetimeUtils.tryParseLocalDateTime(valueStr, DatetimeUtils.ISO_DATETIME_FORMAT, DatetimeUtils.ISO_DATE_FORMAT).toLocalDate();
            } else if (attributeType.equals(LocalDateTime.class)) {
                return DatetimeUtils.tryParseLocalDateTime(valueStr, DatetimeUtils.ISO_DATETIME_FORMAT, DatetimeUtils.ISO_DATE_FORMAT);
            } else if (attributeType.isEnum()) {
                return Enum.valueOf((Class<Enum>) attributeType, valueStr.toUpperCase());
            } else if (attributeType.equals(Integer.class) || attributeType.equals(int.class)) {
                return Integer.parseInt(valueStr);
            } else if (attributeType.equals(Long.class) || attributeType.equals(long.class)) {
                return Long.parseLong(valueStr);
            } else if (attributeType.equals(Double.class) || attributeType.equals(double.class)) {
                return Double.parseDouble(valueStr);
            } else if (attributeType.equals(Boolean.class) || attributeType.equals(boolean.class)) {
                return Boolean.parseBoolean(valueStr);
            }
            return value; // Default to the original value (e.g., for String)
        } catch (Exception e) {
            // Log this error in a real application
            // e.g., log.warn("Failed to convert value '{}' for path '{}'", value, path, e);
            return null; // Failed conversion leads to ignoring the filter
        }
    }

    /**
     * Enum-based Strategy Pattern for filter operations.
     * This is more robust and readable than a map of lambdas.
     */
    private enum FilterOperation {
        CONTAINS("contains") {
            @Override
            <Y extends Comparable<? super Y>> Predicate build(CriteriaBuilder cb, Path<?> path, Object value) {
                return cb.like(cb.lower(path.as(String.class)), "%" + value.toString().toLowerCase() + "%");
            }
        },
        NOT_CONTAINS("notContains") {
            @Override
            <Y extends Comparable<? super Y>> Predicate build(CriteriaBuilder cb, Path<?> path, Object value) {
                return cb.notLike(cb.lower(path.as(String.class)), "%" + value.toString().toLowerCase() + "%");
            }
        },
        STARTS_WITH("startsWith") {
            @Override
            <Y extends Comparable<? super Y>> Predicate build(CriteriaBuilder cb, Path<?> path, Object value) {
                return cb.like(cb.lower(path.as(String.class)), value.toString().toLowerCase() + "%");
            }
        },
        ENDS_WITH("endsWith") {
            @Override
            <Y extends Comparable<? super Y>> Predicate build(CriteriaBuilder cb, Path<?> path, Object value) {
                return cb.like(cb.lower(path.as(String.class)), "%" + value.toString().toLowerCase());
            }
        },
        EQUALS("equals") {
            @Override
            <Y extends Comparable<? super Y>> Predicate build(CriteriaBuilder cb, Path<?> path, Object value) {
                return cb.equal(path, value);
            }
        },
        NOT_EQUALS("notEquals") {
            @Override
            <Y extends Comparable<? super Y>> Predicate build(CriteriaBuilder cb, Path<?> path, Object value) {
                return cb.notEqual(path, value);
            }
        },
        GT("gt") {
            @Override
            @SuppressWarnings("unchecked")
            <Y extends Comparable<? super Y>> Predicate build(CriteriaBuilder cb, Path<?> path, Object value) {
                return cb.greaterThan((Path<Y>) path, (Y) value);
            }
        },
        GTE("gte") {
            @Override
            @SuppressWarnings("unchecked")
            <Y extends Comparable<? super Y>> Predicate build(CriteriaBuilder cb, Path<?> path, Object value) {
                return cb.greaterThanOrEqualTo((Path<Y>) path, (Y) value);
            }
        },
        LT("lt") {
            @Override
            @SuppressWarnings("unchecked")
            <Y extends Comparable<? super Y>> Predicate build(CriteriaBuilder cb, Path<?> path, Object value) {
                return cb.lessThan((Path<Y>) path, (Y) value);
            }
        },
        LTE("lte") {
            @Override
            @SuppressWarnings("unchecked")
            <Y extends Comparable<? super Y>> Predicate build(CriteriaBuilder cb, Path<?> path, Object value) {
                return cb.lessThanOrEqualTo((Path<Y>) path, (Y) value);
            }
        },
        DATE_IS("dateIs") {
            @Override
            <Y extends Comparable<? super Y>> Predicate build(CriteriaBuilder cb, Path<?> path, Object value) {
                return cb.equal(path.as(LocalDate.class), value);
            }
        },
        DATE_IS_NOT("dateIsNot") {
            @Override
            <Y extends Comparable<? super Y>> Predicate build(CriteriaBuilder cb, Path<?> path, Object value) {
                return cb.notEqual(path.as(LocalDate.class), value);
            }
        },
        DATE_BEFORE("dateBefore") {
            @Override
            <Y extends Comparable<? super Y>> Predicate build(CriteriaBuilder cb, Path<?> path, Object value) {
                return cb.lessThan(path.as(LocalDate.class), (LocalDate) value);
            }
        },
        DATE_AFTER("dateAfter") {
            @Override
            <Y extends Comparable<? super Y>> Predicate build(CriteriaBuilder cb, Path<?> path, Object value) {
                return cb.greaterThan(path.as(LocalDate.class), (LocalDate) value);
            }
        };

        private final String key;

        FilterOperation(String key) {
            this.key = key;
        }

        abstract <Y extends Comparable<? super Y>> Predicate build(CriteriaBuilder cb, Path<?> path, Object value);

        public static FilterOperation fromString(String text) {
            return Stream.of(values())
                    .filter(op -> op.key.equalsIgnoreCase(text))
                    .findFirst()
                    .orElse(EQUALS); // Default to EQUALS if not found
        }
    }
}
