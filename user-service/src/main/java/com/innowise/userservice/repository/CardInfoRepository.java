package com.innowise.userservice.repository;

import com.innowise.userservice.model.entity.CardInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link CardInfo} entities.
 * Provides standard CRUD operations and custom query methods.
 */
@Repository
public interface CardInfoRepository extends JpaRepository<CardInfo, Long> {

    /**
     * Saves a given card.
     *
     * @param cardInfo The card to save.
     * @return The saved card.
     */
    @Override
    CardInfo save(CardInfo cardInfo);

    /**
     * Retrieves a card by its ID.
     *
     * @param id The ID of the card to retrieve.
     * @return An {@link Optional} containing the card with the given ID, or {@link Optional#empty()} if not found.
     */
    @Override
    Optional<CardInfo> findById(Long id);

    /**
     * Deletes the card with the given ID.
     *
     * @param id The ID of the card to delete.
     */
    @Override
    void deleteById(Long id);

    /**
     * Finds a list of cards by the ID of the user they belong to.
     * This method uses a named query.
     *
     * @param userId The ID of the user.
     * @return A list of cards associated with the specified user.
     */
    List<CardInfo> findByUserId(Long userId);

    /**
     * Finds a list of cards by a given list of IDs using a JPQL query.
     *
     * @param ids A list of card IDs to search for.
     * @return A list of found cards.
     */
    @Query("SELECT ci FROM CardInfo ci WHERE ci.id IN :ids")
    List<CardInfo> findByIdIn(List<Long> ids);

}
