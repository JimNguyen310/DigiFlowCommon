package fec.digiflow.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fec.digiflow.common.exception.JsonOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;

public final class JacksonUtils {

    private static final ObjectMapper MAPPER = createObjectMapper();

    private JacksonUtils() {}

    private static ObjectMapper createObjectMapper() {
        return JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .addModule(new JavaTimeModule()) // Thêm module hỗ trợ Java 8 Date/Time
                .build();
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    public static String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new JsonOperationException("Failed to serialize object to JSON", e);
        }
    }

    public static String toPrettyJson(Object obj) {
        if (obj == null) return null;
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            throw new JsonOperationException("Failed to serialize object to pretty JSON", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) return null;
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new JsonOperationException("Failed to deserialize JSON to object of class " + clazz.getSimpleName(), e);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) return null;
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            throw new JsonOperationException("Failed to deserialize JSON to generic type " + typeReference.getType(), e);
        }
    }

    public static <T> T fromInputStream(InputStream inputStream, Class<T> clazz) {
        if (inputStream == null) return null;
        try (InputStream stream = inputStream) {
            return MAPPER.readValue(stream, clazz);
        } catch (IOException e) {
            throw new JsonOperationException("Failed to deserialize JSON from InputStream to class " + clazz.getSimpleName(), e);
        }
    }

    public static <T> T fromInputStream(InputStream inputStream, TypeReference<T> typeReference) {
        if (inputStream == null) return null;
        try (InputStream stream = inputStream) {
            return MAPPER.readValue(stream, typeReference);
        } catch (IOException e) {
            throw new JsonOperationException("Failed to deserialize JSON from InputStream to generic type " + typeReference.getType(), e);
        }
    }

    public static <T> T fromMap(Map<String, Object> map, Class<T> clazz) {
        if (map == null) return null;
        try {
            return MAPPER.convertValue(map, clazz);
        } catch (IllegalArgumentException e) {
            throw new JsonOperationException("Failed to convert map to object of class " + clazz.getSimpleName(), e);
        }
    }

    public static <T> T convertValue(Object object, Type type) {
        JavaType javaType = MAPPER.getTypeFactory().constructType(type);
        return MAPPER.convertValue(object, javaType);
    }

    public static Map<String, Object> toMap(Object obj) {
        if (obj == null) return null;
        return MAPPER.convertValue(obj, new TypeReference<Map<String, Object>>() {});
    }

    public static JsonNode toJsonNode(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return MAPPER.readTree(json);
        } catch (Exception e) {
            throw new JsonOperationException("Failed to parse JSON string to JsonNode", e);
        }
    }
}