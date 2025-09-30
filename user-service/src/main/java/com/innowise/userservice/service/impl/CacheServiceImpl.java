package com.innowise.userservice.service.impl;

import com.innowise.userservice.service.CacheService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class CacheServiceImpl implements CacheService {

    @Override
    @CacheEvict(cacheNames = "users", key = "#id")
    public void evictUser(Long id) {}

}
