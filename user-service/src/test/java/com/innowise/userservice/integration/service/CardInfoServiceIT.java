package com.innowise.userservice.integration.service;

import com.innowise.userservice.integration.BaseIntegrationTest;
import com.innowise.userservice.model.dto.CardInfoRequest;
import com.innowise.userservice.model.dto.CardInfoResponse;
import com.innowise.userservice.model.entity.CardInfo;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.CardInfoRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.CardInfoService;
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


class CardInfoServiceIT extends BaseIntegrationTest {

    @Autowired
    private CardInfoService cardInfoService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardInfoRepository cardInfoRepository;

    @Autowired
    private CacheManager cacheManager;

    private User testUser;
    private CardInfo testCard;

    @BeforeEach
    void setUp() {
        String uniqueEmail = "john.doe." + System.currentTimeMillis() + "@example.com";
        testUser = userRepository.save(User.builder()
                .name("John")
                .surname("Doe")
                .email(uniqueEmail)
                .birthDate(LocalDate.of(1990, 1, 1))
                .build());

        testCard = cardInfoRepository.save(CardInfo.builder()
                .number("1234567890123456")
                .holder("John Doe")
                .expirationDate(LocalDate.of(2025, 12, 31))
                .user(testUser)
                .build());
    }

    @Test
    @DisplayName("Should evict user cache when new card is saved")
    void save_ShouldEvictUserCache_WhenNewCardIsSaved() {
        Long userId = testUser.getId();
        
        userService.findById(userId);
        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(userId)).isNotNull();

        CardInfoRequest cardRequest = CardInfoRequest.builder()
                .number("9876543210987654")
                .holder("John Doe")
                .expirationDate(LocalDate.of(2026, 6, 30))
                .userId(userId)
                .build();

        CardInfoResponse savedCard = cardInfoService.save(cardRequest);

        assertThat(savedCard).isNotNull();
        assertThat(savedCard.getNumber()).isEqualTo("9876543210987654");
        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(userId)).isNull();
    }

    @Test
    @DisplayName("Should evict user cache when card is updated")
    void updateById_ShouldEvictUserCache_WhenCardIsUpdated() {
        Long userId = testUser.getId();
        Long cardId = testCard.getId();

        userService.findById(userId);
        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(userId)).isNotNull();

        CardInfoRequest updateRequest = CardInfoRequest.builder()
                .number("1111222233334444")
                .holder("John Updated Doe")
                .expirationDate(LocalDate.of(2027, 3, 15))
                .userId(userId)
                .build();

        CardInfoResponse updatedCard = cardInfoService.updateById(cardId, updateRequest);

        assertThat(updatedCard).isNotNull();
        assertThat(updatedCard.getNumber()).isEqualTo("1111222233334444");
        assertThat(updatedCard.getHolder()).isEqualTo("John Updated Doe");
        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(userId)).isNull();
    }

    @Test
    @DisplayName("Should evict user cache when card is deleted")
    void deleteById_ShouldEvictUserCache_WhenCardIsDeleted() {
        Long userId = testUser.getId();
        Long cardId = testCard.getId();

        userService.findById(userId);
        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(userId)).isNotNull();

        Long returnedUserId = cardInfoService.deleteById(cardId);

        assertThat(returnedUserId).isEqualTo(userId);
        assertThat(cardInfoRepository.existsById(cardId)).isFalse();
        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(userId)).isNull();
    }

    @Test
    @DisplayName("Should not affect cache when card operations fail")
    void save_ShouldNotAffectCache_WhenCardOperationFails() {
        Long userId = testUser.getId();
        
        userService.findById(userId);
        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(userId)).isNotNull();

        CardInfoRequest invalidRequest = CardInfoRequest.builder()
                .number("invalid")
                .holder("John Doe")
                .expirationDate(LocalDate.of(2025, 12, 31))
                .userId(999L)
                .build();

        assertThatThrownBy(() -> cardInfoService.save(invalidRequest))
                .isInstanceOf(Exception.class);

        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(userId)).isNotNull();
    }

    @Test
    @DisplayName("Should handle multiple card operations and cache evictions correctly")
    void multipleOperations_ShouldHandleCacheEvictions_WhenMultipleCardsAreModified() {
        Long userId = testUser.getId();
        
        userService.findById(userId);
        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(userId)).isNotNull();

        CardInfoRequest card1Request = CardInfoRequest.builder()
                .number("1111111111111111")
                .holder("John Doe")
                .expirationDate(LocalDate.of(2025, 12, 31))
                .userId(userId)
                .build();

        CardInfoRequest card2Request = CardInfoRequest.builder()
                .number("2222222222222222")
                .holder("John Doe")
                .expirationDate(LocalDate.of(2026, 6, 30))
                .userId(userId)
                .build();

        CardInfoResponse card1 = cardInfoService.save(card1Request);
        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(userId)).isNull();

        userService.findById(userId);
        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(userId)).isNotNull();

        CardInfoResponse card2 = cardInfoService.save(card2Request);
        assertThat(Objects.requireNonNull(cacheManager.getCache("users")).get(userId)).isNull();

        assertThat(card1).isNotNull();
        assertThat(card2).isNotNull();
        assertThat(card1.getNumber()).isNotEqualTo(card2.getNumber());
        assertThat(cardInfoRepository.existsById(card1.getId())).isTrue();
        assertThat(cardInfoRepository.existsById(card2.getId())).isTrue();
    }
}
