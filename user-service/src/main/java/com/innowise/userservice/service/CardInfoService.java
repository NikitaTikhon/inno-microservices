package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.CardInfoRequest;
import com.innowise.userservice.model.dto.CardInfoResponse;

import java.util.List;

/**
 * Service interface for managing card-related business logic.
 * Defines the contract for CRUD operations on CardInfo entities.
 */
public interface CardInfoService {

    /**
     * Saves a new CardInfo based on the provided DTO.
     *
     * @param cardInfoRequest The DTO containing the card's data.
     * @return The DTO of the newly created CardInfo.
     */
    CardInfoResponse save(CardInfoRequest cardInfoRequest);

    /**
     * Finds a CardInfo by its unique ID.
     *
     * @param id The ID of the card to find.
     * @return The DTO of the found CardInfo.
     * @throws com.innowise.userservice.exception.CardNotFoundException if the card with the given ID is not found.
     */
    CardInfoResponse findById(Long id);

    /**
     * Finds a list of CardInfo by a list of their IDs.
     *
     * @param ids A list of IDs of the cards to find.
     * @return A list of DTOs for the found CardInfo.
     */
    List<CardInfoResponse> findByIds(List<Long> ids);

    /**
     * Updates an existing CardInfo based on the provided DTO and ID.
     *
     * @param id                 The ID of the card to update.
     * @param cardInfoRequest The DTO containing the updated card data.
     * @return The DTO of the updated CardInfo.
     * @throws com.innowise.userservice.exception.CardNotFoundException if the card with the given ID is not found.
     */
    CardInfoResponse updateById(Long id, CardInfoRequest cardInfoRequest);

    /**
     * Deletes a CardInfo by its unique ID.
     *
     * @param id The ID of the card to delete.
     * @throws com.innowise.userservice.exception.CardNotFoundException if the card with the given ID is not found.
     */
    void deleteById(Long id);

}
