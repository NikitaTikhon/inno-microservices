package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.CardInfoRequest;
import com.innowise.userservice.model.dto.CardInfoResponse;
import com.innowise.userservice.service.CardInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for managing bank card information.
 * Provides REST endpoints for creating, retrieving, updating, and deleting card data.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cards")
public class CardInfoController {

    private final CardInfoService cardInfoService;

    /**
     * Creates a new bank card record.
     *
     * @param cardInfoRequest The {@link CardInfoRequest} object containing data for the new card.
     * @return A {@link ResponseEntity} with the created {@link CardInfoResponse} object and an HTTP status of OK (200).
     */
    @PostMapping
    public ResponseEntity<CardInfoResponse> save(@RequestBody @Valid CardInfoRequest cardInfoRequest) {
        CardInfoResponse cardInfo = cardInfoService.save(cardInfoRequest);

        return ResponseEntity.ok(cardInfo);
    }

    /**
     * Finds card information by its unique ID.
     *
     * @param id The unique ID of the card.
     * @return A {@link ResponseEntity} with the {@link CardInfoResponse} object and an HTTP status of OK (200).
     */
    @GetMapping("/{id}")
    public ResponseEntity<CardInfoResponse> findById(@PathVariable Long id) {
        CardInfoResponse cardInfo = cardInfoService.findById(id);

        return ResponseEntity.ok(cardInfo);
    }

    /**
     * Finds a list of card information records by their IDs.
     *
     * @param ids A list of unique card IDs.
     * @return A {@link ResponseEntity} with a list of {@link CardInfoResponse} objects and an HTTP status of OK (200).
     */
    @GetMapping
    public ResponseEntity<List<CardInfoResponse>> findByIds(@RequestParam List<Long> ids) {
        List<CardInfoResponse> cardsInfo = cardInfoService.findByIds(ids);

        return ResponseEntity.ok(cardsInfo);
    }

    /**
     * Updates card information by its ID.
     *
     * @param id The unique ID of the card.
     * @param cardInfoRequest The {@link CardInfoRequest} object with the updated card data.
     * @return A {@link ResponseEntity} with the updated {@link CardInfoResponse} object and an HTTP status of OK (200).
     */
    @PatchMapping("/{id}")
    public ResponseEntity<CardInfoResponse> updateById(@PathVariable("id") Long id, @RequestBody @Valid CardInfoRequest cardInfoRequest) {
        CardInfoResponse cardInfo = cardInfoService.updateById(id, cardInfoRequest);

        return ResponseEntity.ok(cardInfo);
    }

    /**
     * Deletes card information by its ID.
     *
     * @param id The unique ID of the card.
     * @return A {@link ResponseEntity} with no body and an HTTP status of No Content (204).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") Long id) {
        cardInfoService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

}
