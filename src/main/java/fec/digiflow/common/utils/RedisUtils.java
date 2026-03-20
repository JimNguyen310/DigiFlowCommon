package fec.digiflow.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * A utility class for interacting with a Redis server.
 * This class abstracts the underlying Redis operations for setting, getting, and deleting keys.
 */
@Component
@ConditionalOnBean(RedisTemplate.class)
@RequiredArgsConstructor
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Stores a string value in Redis with a specified time-to-live (TTL).
     *
     * @param key      The key to store.
     * @param value    The string value to associate with the key.
     * @param timeout  The duration for which the key should be stored.
     * @param timeUnit The unit of time for the timeout.
     */
    public void setString(String key, String value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * Retrieves a string value associated with a given key.
     *
     * @param key The key to retrieve.
     * @return The string value, or null if the key does not exist or the value is not a string.
     */
    public String getString(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value instanceof String ? (String) value : null;
    }

    /**
     * Stores an object in Redis with a specified time-to-live (TTL). The object will be serialized to JSON.
     *
     * @param key      The key to store.
     * @param value    The object to associate with the key.
     * @param timeout  The duration for which the key should be stored.
     * @param timeUnit The unit of time for the timeout.
     * @param <T>      The type of the value.
     */
    public <T> void setObject(String key, T value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * Retrieves an object from Redis and converts it to the specified type.
     *
     * @param key   The key to retrieve.
     * @param clazz The class of the object to be returned.
     * @param <T>   The type of the object.
     * @return The deserialized object, or null if the key does not exist or conversion fails.
     */
    public <T> T getObject(String key, Class<T> clazz) {
        Object valueFromRedis = redisTemplate.opsForValue().get(key);
        if (valueFromRedis == null) {
            return null;
        }
        // Redis may return a Map, so we use ObjectMapper to convert it to the desired POJO.
        return JacksonUtils.convertValue(valueFromRedis, clazz);
    }

    /**
     * Deletes a key from Redis.
     *
     * @param key The key to delete.
     * @return true if the key was deleted, false otherwise.
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * Checks if a key exists in Redis.
     *
     * @param key The key to check.
     * @return true if the key exists, false otherwise.
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
}
