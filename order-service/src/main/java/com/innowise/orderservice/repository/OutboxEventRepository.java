package com.innowise.orderservice.repository;


import com.innowise.orderservice.model.EventStatus;
import com.innowise.orderservice.model.entity.OutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Retrieves a list of outbox events filtered by their status, ordered by creation time.
     *
     * @param eventStatus the status of events to retrieve (e.g., PENDING, SENT, FAILED)
     * @param pageable    pagination parameters to limit the number of results
     * @return a list of {@link OutboxEvent} entities matching the given status,
     *         ordered by creation timestamp in ascending order (oldest first)
     */
    List<OutboxEvent> findByEventStatusOrderByCreatedAt(EventStatus eventStatus, Pageable pageable);

}
