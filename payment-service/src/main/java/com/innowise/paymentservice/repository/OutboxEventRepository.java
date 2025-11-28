package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.model.EventStatus;
import com.innowise.paymentservice.model.document.OutboxEvent;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends MongoRepository<OutboxEvent, ObjectId> {

    /**
     * Retrieves a list of outbox events filtered by their status, ordered by creation time.
     *
     * @param eventStatus the status of events to retrieve (e.g., PENDING, SENT, FAILED)
     * @param pageable    pagination parameters to limit the number of results
     * @return a list of {@link OutboxEvent} entities matching the given status,
     *         ordered by creation timestamp in ascending order (oldest first)
     */
    List<OutboxEvent> findByEventStatusOrderByCreatedAt(EventStatus eventStatus, Pageable pageable);

    /**
     * Checks whether an outbox event already exists for the given order ID.
     *
     * @param orderId the unique identifier of the order
     * @return {@code true} if an outbox event exists for this order, {@code false} otherwise
     */
    boolean existsByOrderId(Long orderId);

}
