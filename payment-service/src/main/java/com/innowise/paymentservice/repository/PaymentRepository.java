package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.model.document.Payment;
import com.innowise.paymentservice.model.projection.TotalAmountProjection;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Payment} entities in MongoDB.
 * Provides CRUD operations and custom queries for payment data access.
 */
@Repository
public interface PaymentRepository extends MongoRepository<Payment, ObjectId> {

    /**
     * Saves a given payment.
     *
     * @param payment The payment to save.
     * @return The saved payment.
     */
    @Override
    Payment save(Payment payment);

    /**
     * Retrieves payment associated with a specific order.
     *
     * @param id The order ID to search for.
     * @return A {@link Optional} of payment for the given order.
     */
    Optional<Payment> findByOrderId(Long id);

    /**
     * Retrieves all payments made by a specific user.
     *
     * @param id The user ID to search for.
     * @return A {@link List} of payments made by the user.
     */
    List<Payment> findByUserId(Long id);

    /**
     * Retrieves all payments that match any of the provided statuses.
     *
     * @param statuses The list of payment statuses to filter by.
     * @return A {@link List} of payments matching any of the provided statuses.
     */
    List<Payment> findByStatusIn(List<PaymentStatus> statuses);

    /**
     * Calculates the total amount of all payments within a specified time range.
     *
     * @param from The start of the time range (inclusive).
     * @param to The end of the time range (inclusive).
     * @return An {@link Optional} containing the total amount projection, empty if no payments found in range.
     */
    @Aggregation(pipeline = {
            "{ $match: { timestamp: { $gte: ?0, $lte: ?1 } } }",
            "{ $group: { _id: null, total: { $sum: '$payment_amount' } } }"
    })
    Optional<TotalAmountProjection> findTotalAmount(LocalDateTime from, LocalDateTime to);

    /**
     * Checks if a payment exists for the specified order.
     *
     * @param id The order ID to check for.
     * @return {@code true} if a payment exists for the given order, {@code false} otherwise.
     */
    boolean existsByOrderId(Long id);

}