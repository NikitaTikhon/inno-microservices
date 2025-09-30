package com.innowise.userservice.service;

/**
 * Service interface defining cache management operations.
 */
public interface CacheService {

    /**
     * Explicitly evicts a user entry from the "users" cache.
     *
     * @param id The ID of the user that should be removed from the cache.
     */
    void evictUser(Long id);

}
