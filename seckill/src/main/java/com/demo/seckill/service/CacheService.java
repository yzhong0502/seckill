package com.demo.seckill.service;

public interface CacheService {
    void setCommonCache(String key, Object value);
    Object getFromCommonCache(String key);
}
