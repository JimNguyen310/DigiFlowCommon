package fec.digiflow.common.utils;

import fec.digiflow.common.exception.JsonOperationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * A utility class for JSON operations using the Jackson library.
 * Provides a pre-configured, thread-safe ObjectMapper and convenient methods
 * for serialization and deserialization.
 */
public final class JacksonUtils {

    private static final ObjectMapper MAPPER = createObjectMapper();

    private JacksonUtils() {
        // Private constructor to prevent instantiation
    }

    private static ObjectMapper createObjectMapper() {
        return JsonMapper.builder()
                // Ignore unknown properties during deserialization
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // Do not write dates as timestamps
                // Fail on empty beans to prevent serializing empty objects
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .build();
    }

    /**
     * Returns the centrally configured ObjectMapper instance.
     *
     * @return The configured ObjectMapper.
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new JsonOperationException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * Serializes an object to a pretty-printed JSON string representation.
     *
     * @param obj The object to serialize.
     * @return The pretty-printed JSON string.
     * @throws JsonOperationException if serialization fails.
     */
    public static String toPrettyJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            throw new JsonOperationException("Failed to serialize object to pretty JSON", e);
        }
    }

    /**
     * Deserializes a JSON string into an object of the specified class.
     *
     * @param json  The JSON string.
     * @param clazz The class of the target object.
     * @param <T>   The type of the target object.
     * @return The deserialized object.
     * @throws JsonOperationException if deserialization fails.
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new JsonOperationException("Failed to deserialize JSON to object of class " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Deserializes a JSON string into a generic type (e.g., List<User>).
     *
     * @param json          The JSON string.
     * @param typeReference The TypeReference describing the target type.
     * @param <T>           The generic type of the target object.
     * @return The deserialized object.
     * @throws JsonOperationException if deserialization fails.
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            throw new JsonOperationException("Failed to deserialize JSON to generic type " + typeReference.getType(), e);
        }
    }

    /**
     * Deserializes a JSON InputStream into an object of the specified class.
     * The InputStream will be closed automatically after the operation.
     *
     * @param inputStream The InputStream containing JSON data.
     * @param clazz       The class of the target object.
     * @param <T>         The type of the target object.
     * @return The deserialized object.
     * @throws JsonOperationException if deserialization fails.
     */
    public static <T> T fromInputStream(InputStream inputStream, Class<T> clazz) {
        if (inputStream == null) {
            return null;
        }
        try (InputStream stream = inputStream) { // Ensure stream is closed
            return MAPPER.readValue(stream, clazz);
        } catch (IOException e) {
            throw new JsonOperationException("Failed to deserialize JSON from InputStream to class " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Deserializes a JSON InputStream into a generic type (e.g., List<User>).
     * The InputStream will be closed automatically after the operation.
     *
     * @param inputStream   The InputStream containing JSON data.
     * @param typeReference The TypeReference describing the target type.
     * @param <T>           The generic type of the target object.
     * @return The deserialized object.
     * @throws JsonOperationException if deserialization fails.
     */
    public static <T> T fromInputStream(InputStream inputStream, TypeReference<T> typeReference) {
        if (inputStream == null) {
            return null;
        }
        try (InputStream stream = inputStream) { // Ensure stream is closed
            return MAPPER.readValue(stream, typeReference);
        } catch (IOException e) {
            throw new JsonOperationException("Failed to deserialize JSON from InputStream to generic type " + typeReference.getType(), e);
        }
    }

    /**
     * Converts a Map to an object of the specified class.
     *
     * @param map   The map to convert.
     * @param clazz The class of the target object.
     * @param <T>   The type of the target object.
     * @return The converted object.
     * @throws JsonOperationException if conversion fails.
     */
    public static <T> T fromMap(Map<String, Object> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }
        try {
            return MAPPER.convertValue(map, clazz);
        } catch (IllegalArgumentException e) {
            throw new JsonOperationException("Failed to convert map to object of class " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Converts a value to a specified generic type. This is useful for converting between different POJO types
     * or for converting a generic {@code Object} to a specific type, leveraging Jackson's data binding capabilities.
     *
     * @param object The object to convert.
     * @param type The target generic type (e.g., {@code new TypeReference<List<String>>(){}.getType()}).
     * @param <T> The generic type of the target object.
     * @return The converted object.
     */
    public static <T> T convertValue(Object object, Type type) {
        JavaType javaType = MAPPER.getTypeFactory().constructType(type);
        return MAPPER.convertValue(object, javaType);
    }

    /**
     * Converts an object into a Map.
     *
     * @param obj The object to convert.
     * @return A map representation of the object.
     * @throws JsonOperationException if conversion fails.
     */
    public static Map<String, Object> toMap(Object obj) {
        if (obj == null) {
            return null;
        }
        // Use TypeReference to safely convert to a generic Map
        return MAPPER.convertValue(obj, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Converts a JSON string into a JsonNode tree model.
     *
     * @param json The JSON string.
     * @return The root JsonNode of the parsed tree.
     * @throws JsonOperationException if parsing fails.
     */
    public static JsonNode toJsonNode(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readTree(json);
        } catch (Exception e) {
            throw new JsonOperationException("Failed to parse JSON string to JsonNode", e);
        }
    }
}
