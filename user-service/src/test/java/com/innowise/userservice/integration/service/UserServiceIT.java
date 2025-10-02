package com.innowise.userservice.integration.service;

import com.innowise.userservice.integration.BaseIntegrationTest;
import com.innowise.userservice.model.dto.UserRequest;
import com.innowise.userservice.model.dto.UserResponse;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class UserServiceIT extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        String uniqueEmail = "jane.smith." + System.currentTimeMillis() + "@example.com";
        testUser = userRepository.save(User.builder()
                .name("Jane")
                .surname("Smith")
                .email(uniqueEmail)
                .birthDate(LocalDate.of(1995, 5, 15))
                .build());
    }

    @Test
    @DisplayName("Should cache user by ID on first call and return from cache on second call")
    void findById_ShouldCacheUser_WhenCalledMultipleTimes() {
        Long userId = testUser.getId();

        UserResponse firstCall = userService.findById(userId);
        UserResponse secondCall = userService.findById(userId);

        assertThat(firstCall).isNotNull();
        assertThat(secondCall).isNotNull();
        assertThat(firstCall.getId()).isEqualTo(secondCall.getId());
        assertThat(firstCall.getName()).isEqualTo(secondCall.getName());
        assertThat(firstCall.getEmail()).isEqualTo(secondCall.getEmail());
        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(userId)).isNotNull();
    }

    @Test
    @DisplayName("Should cache user by email on first call and return from cache on second call")
    void findByEmail_ShouldCacheUser_WhenCalledMultipleTimes() {
        String email = testUser.getEmail();

        UserResponse firstCall = userService.findByEmail(email);
        UserResponse secondCall = userService.findByEmail(email);

        assertThat(firstCall).isNotNull();
        assertThat(secondCall).isNotNull();
        assertThat(firstCall.getId()).isEqualTo(secondCall.getId());
        assertThat(firstCall.getEmail()).isEqualTo(secondCall.getEmail());
        assertThat(Objects.requireNonNull(cacheManager.getCache("usersByEmail")).get(email)).isNotNull();
    }

    @Test
    @DisplayName("Should update cache when user is updated")
    void updateById_ShouldUpdateCache_WhenUserIsUpdated() {
        Long userId = testUser.getId();
        UserRequest updateRequest = UserRequest.builder()
                .name("Updated Jane")
                .surname("Updated Smith")
                .email("updated.jane.smith@example.com")
                .birthDate(LocalDate.of(1995, 5, 15))
                .build();

        userService.findById(userId);
        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(userId)).isNotNull();

        UserResponse updatedUser = userService.updateById(userId, updateRequest);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getName()).isEqualTo("Updated Jane");
        assertThat(updatedUser.getEmail()).isEqualTo("updated.jane.smith@example.com");

        UserResponse cachedUser = Objects.requireNonNull(cacheManager.getCache("users")).get(userId, UserResponse.class);
        assertThat(cachedUser).isNotNull();
        assertThat(cachedUser.getName()).isEqualTo("Updated Jane");
        assertThat(cachedUser.getEmail()).isEqualTo("updated.jane.smith@example.com");
    }

    @Test
    @DisplayName("Should evict user from cache when user is deleted")
    void deleteById_ShouldEvictUserFromCache_WhenUserIsDeleted() {
        Long userId = testUser.getId();

        userService.findById(userId);
        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(userId)).isNotNull();

        userService.deleteById(userId);

        assertThat(userRepository.existsById(userId)).isFalse();
        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(userId)).isNull();
    }

    @Test
    @DisplayName("Should not cache when user is not found")
    void findById_ShouldNotCache_WhenUserNotFound() {
        Long nonExistentUserId = 999L;

        assertThatThrownBy(() -> userService.findById(nonExistentUserId))
                .isInstanceOf(Exception.class);

        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(nonExistentUserId)).isNull();
    }

    @Test
    @DisplayName("Should handle multiple users caching independently")
    void findById_ShouldCacheMultipleUsers_Independently() {
        String secondUserEmail = "bob.johnson." + System.currentTimeMillis() + "@example.com";
        User secondUser = userRepository.save(User.builder()
                .name("Bob")
                .surname("Johnson")
                .email(secondUserEmail)
                .birthDate(LocalDate.of(1988, 3, 20))
                .build());

        UserResponse user1 = userService.findById(testUser.getId());
        UserResponse user2 = userService.findById(secondUser.getId());

        assertThat(user1).isNotNull();
        assertThat(user2).isNotNull();
        assertThat(user1.getId()).isNotEqualTo(user2.getId());
        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(testUser.getId())).isNotNull();
        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(secondUser.getId())).isNotNull();
        assertThat(Objects.requireNonNull(Objects.requireNonNull(cacheManager.getCache("users")).get(testUser.getId())).get())
                .isNotEqualTo(Objects.requireNonNull(Objects.requireNonNull(cacheManager.getCache("users")).get(secondUser.getId())).get());
    }
}
