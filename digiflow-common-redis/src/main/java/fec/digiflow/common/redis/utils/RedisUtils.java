package fec.digiflow.common.redis.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setObject(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public <T> T getObject(String key, Class<T> clazz) {
        Object obj = redisTemplate.opsForValue().get(key);
        if (clazz.isInstance(obj)) {
            return clazz.cast(obj);
        }
        return null;
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
