package com.ms.authservice.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisUtilServiceImpl implements RedisUtilService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public static String buildKey(String prefix, String key) {
        return prefix + ":" + key;
    }

    // SET without TTL
    @Override
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // SET with TTL
    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    // GET
    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // DELETE
    @Override
    public boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    // EXISTS
    @Override
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    // INCREMENT by 1
    @Override
    public long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    // INCREMENT by delta
    @Override
    public long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // SET TTL on existing key
    @Override
    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }
}