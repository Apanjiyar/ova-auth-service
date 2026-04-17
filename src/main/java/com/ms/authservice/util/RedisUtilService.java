package com.ms.authservice.util;

import java.util.concurrent.TimeUnit;

public interface RedisUtilService {

    void set(String key, Object value);

    void set(String key, Object value, long timeout, TimeUnit unit);

    Object get(String key);

    boolean delete(String key);

    boolean exists(String key);

    long increment(String key);

    long increment(String key, long delta);

    void expire(String key, long timeout, TimeUnit unit);
}