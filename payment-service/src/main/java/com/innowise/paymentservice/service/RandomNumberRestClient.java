package com.innowise.paymentservice.service;

/**
 * Client interface for retrieving random numbers from an external REST API.
 * Provides methods to fetch random number values for payment processing.
 */
public interface RandomNumberRestClient {

    /**
     * Retrieves a random number from the external API.
     *
     * @return A random {@link Long} value.
     */
    Long getRandomNumber();

}
