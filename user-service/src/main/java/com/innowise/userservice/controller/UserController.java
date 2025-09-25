package com.innowise.userservice.controller;

import com.innowise.userservice.dto.request.UserRequest;
import com.innowise.userservice.dto.response.UserResponse;
import com.innowise.userservice.dto.response.UserWithCardInfoResponse;
import com.innowise.userservice.service.UserService;
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
 * Controller for managing user-related operations.
 * Provides REST endpoints for creating, retrieving, updating, and deleting user information.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    /**
     * Creates a new user.
     *
     * @param userRequest The {@link UserRequest} object containing the data for the new user.
     * @return A {@link ResponseEntity} with the created {@link UserResponse} object and an HTTP status of OK (200).
     */
    @PostMapping
    public ResponseEntity<UserResponse> save(@RequestBody @Valid UserRequest userRequest) {
        UserResponse user = userService.save(userRequest);

        return ResponseEntity.ok(user);
    }

    /**
     * Finds a user by their unique ID and returns it along with their associated card information.
     *
     * @param id The unique ID of the user.
     * @return A {@link ResponseEntity} with the {@link UserWithCardInfoResponse} object and an HTTP status of OK (200).
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserWithCardInfoResponse> findById(@PathVariable("id") Long id) {
        UserWithCardInfoResponse user = userService.findById(id);

        return ResponseEntity.ok(user);
    }

    /**
     * Finds a list of users by their IDs.
     *
     * @param ids A list of unique user IDs.
     * @return A {@link ResponseEntity} with a list of {@link UserResponse} objects and an HTTP status of OK (200).
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> findByIds(@RequestParam List<Long> ids) {
        List<UserResponse> users = userService.findByIds(ids);

        return ResponseEntity.ok(users);
    }

    /**
     * Finds a user by their email address.
     *
     * @param email The email address of the user.
     * @return A {@link ResponseEntity} with the {@link UserResponse} object and an HTTP status of OK (200).
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> findByEmail(@PathVariable("email") String email) {
        UserResponse user = userService.findByEmail(email);

        return ResponseEntity.ok(user);
    }

    /**
     * Updates a user's information by their ID.
     *
     * @param id The unique ID of the user.
     * @param userRequest The {@link UserRequest} object with the updated user data.
     * @return A {@link ResponseEntity} with the updated {@link UserResponse} object and an HTTP status of OK (200).
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateById(@PathVariable("id") Long id, @RequestBody @Valid UserRequest userRequest) {
        UserResponse user = userService.updateById(id, userRequest);

        return ResponseEntity.ok(user);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id The unique ID of the user.
     * @return A {@link ResponseEntity} with no body and an HTTP status of No Content (204).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") Long id) {
        userService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

}
