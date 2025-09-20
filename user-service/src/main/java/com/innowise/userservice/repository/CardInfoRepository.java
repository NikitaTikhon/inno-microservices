package com.innowise.userservice.repository;

import com.innowise.userservice.entity.CardInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing {@link CardInfo} entities.
 * Provides standard CRUD operations and custom query methods.
 */
@Repository
public interface CardInfoRepository extends JpaRepository<CardInfo, Long> {

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

    /**
     * Deletes a card by ID using a native SQL query.
     * Note: This method is less preferred than using the built-in JpaRepository.deleteById().
     *
     * @param id The ID of the card to delete.
     */
    @Modifying
    @Query(value = "DELETE FROM card_info AS ci WHERE ci.id = :id", nativeQuery = true)
    void deleteByIdNative(Long id);

}
