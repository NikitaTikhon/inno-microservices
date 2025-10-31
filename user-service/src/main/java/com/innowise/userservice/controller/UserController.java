package com.innowise.userservice.controller;

import com.innowise.userservice.exception.MissingRequestParameterException;
import com.innowise.userservice.model.dto.PageableFilter;
import com.innowise.userservice.model.dto.UserRequest;
import com.innowise.userservice.model.dto.UserResponse;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.util.ExceptionMessageGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('INTERNAL_SERVICE')")
    public ResponseEntity<UserResponse> save(@RequestBody @Valid UserRequest userRequest) {
        UserResponse user = userService.save(userRequest);

        return ResponseEntity.ok(user);
    }

    /**
     * Finds a user by their unique ID and returns it along with their associated card information.
     *
     * @param id The unique ID of the user.
     * @return A {@link ResponseEntity} with the {@link UserResponse} object and an HTTP status of OK (200).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> findById(@PathVariable("id") Long id) {
        UserResponse user = userService.findById(id);

        return ResponseEntity.ok(user);
    }

    /**
     * Retrieves a list of users based on the specified filtering criteria.
     *
     * @param filter A string that determines the filtering logic to be applied.
     * Defaults to "pageable" if not provided.
     * @param ids A list of user IDs to filter by, used when {@code filter="ids"}.
     * @param email The email address of the user to retrieve, used when {@code filter="email"}.
     * @param pageableFilter Pagination, used when {@code filter="pageable"}.
     * @return A {@link ResponseEntity} containing a {@link List} of {@link UserResponse} DTOs and an HTTP status of OK (200).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> findByFilter(
            @RequestParam(required = false, defaultValue = "pageable") String filter,
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false) String email,
            @Valid PageableFilter pageableFilter) {

        return switch (filter) {
            case "ids" -> {
                if (ids == null) {
                    throw new MissingRequestParameterException(ExceptionMessageGenerator.missingRequestParameter("ids"));
                }
                yield ResponseEntity.ok(userService.findByIds(ids));
            }
            case "email" -> {
                if (email == null) {
                    throw new MissingRequestParameterException(ExceptionMessageGenerator.missingRequestParameter("email"));
                }
                yield ResponseEntity.ok(List.of(userService.findByEmail(email)));
            }
            default -> ResponseEntity.ok(userService.findAll(pageableFilter));
        };
    }

    /**
     * Updates a user's information by their ID.
     *
     * @param id The unique ID of the user.
     * @param userRequest The {@link UserRequest} object with the updated user data.
     * @return A {@link ResponseEntity} with the updated {@link UserResponse} object and an HTTP status of OK (200).
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('INTERNAL_SERVICE')")
    public ResponseEntity<Void> deleteById(@PathVariable("id") Long id) {
        userService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

}
