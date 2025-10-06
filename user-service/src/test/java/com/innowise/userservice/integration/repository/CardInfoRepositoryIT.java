package com.innowise.userservice.integration.repository;

import com.innowise.userservice.integration.BaseRepositoryIntegrationTest;
import com.innowise.userservice.model.entity.CardInfo;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.CardInfoRepository;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CardInfoRepositoryIT extends BaseRepositoryIntegrationTest {

    @Autowired
    private CardInfoRepository cardInfoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser1;
    private User testUser2;
    private CardInfo testCard1;
    private CardInfo testCard2;
    private CardInfo testCard3;

    @BeforeEach
    void setUp() {
        cardInfoRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();

        testUser1 = User.builder()
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .birthDate(LocalDate.of(1990, 5, 15))
                .build();

        testUser2 = User.builder()
                .name("Jane")
                .surname("Smith")
                .email("jane.smith@example.com")
                .birthDate(LocalDate.of(1985, 8, 22))
                .build();

        testCard1 = CardInfo.builder()
                .number("1234567890123456")
                .holder("John Doe")
                .expirationDate(LocalDate.of(2025, 12, 31))
                .user(testUser1)
                .build();

        testCard2 = CardInfo.builder()
                .number("9876543210987654")
                .holder("John Doe")
                .expirationDate(LocalDate.of(2026, 6, 30))
                .user(testUser1)
                .build();

        testCard3 = CardInfo.builder()
                .number("1111222233334444")
                .holder("Jane Smith")
                .expirationDate(LocalDate.of(2025, 3, 15))
                .user(testUser2)
                .build();
    }

    @Test
    @DisplayName("Should save card info successfully")
    void save_ShouldSaveCardInfo_WhenValidCardInfo() {
        User savedUser = userRepository.save(testUser1);
        testCard1.setUser(savedUser);

        CardInfo savedCard = cardInfoRepository.save(testCard1);

        assertThat(savedCard).isNotNull();
        assertThat(savedCard.getId()).isNotNull();
        assertThat(savedCard.getNumber()).isEqualTo("1234567890123456");
        assertThat(savedCard.getHolder()).isEqualTo("John Doe");
        assertThat(savedCard.getExpirationDate()).isEqualTo(LocalDate.of(2025, 12, 31));
        assertThat(savedCard.getUser()).isEqualTo(savedUser);
    }

    @Test
    @DisplayName("Should find card info by id successfully")
    void findById_ShouldReturnCardInfo_WhenCardExists() {
        User savedUser = userRepository.save(testUser1);
        testCard1.setUser(savedUser);
        CardInfo savedCard = cardInfoRepository.save(testCard1);
        entityManager.flush();

        Optional<CardInfo> foundCard = cardInfoRepository.findById(savedCard.getId());

        assertThat(foundCard).isPresent();
        assertThat(foundCard.get().getNumber()).isEqualTo("1234567890123456");
        assertThat(foundCard.get().getHolder()).isEqualTo("John Doe");
        assertThat(foundCard.get().getUser()).isNotNull();
    }

    @Test
    @DisplayName("Should return empty when card info not found by id")
    void findById_ShouldReturnEmpty_WhenCardNotFound() {
        Optional<CardInfo> foundCard = cardInfoRepository.findById(999L);

        assertThat(foundCard).isEmpty();
    }

    @Test
    @DisplayName("Should find cards by ids successfully")
    void findByIdIn_ShouldReturnCards_WhenCardsExist() {
        User savedUser1 = userRepository.save(testUser1);
        User savedUser2 = userRepository.save(testUser2);
        
        testCard1.setUser(savedUser1);
        testCard2.setUser(savedUser1);
        testCard3.setUser(savedUser2);
        
        CardInfo savedCard1 = cardInfoRepository.save(testCard1);
        CardInfo savedCard3 = cardInfoRepository.save(testCard3);
        entityManager.flush();

        List<Long> cardIds = Arrays.asList(savedCard1.getId(), savedCard3.getId());

        List<CardInfo> foundCards = cardInfoRepository.findByIdIn(cardIds);

        assertThat(foundCards).hasSize(2);
        assertThat(foundCards).extracting(CardInfo::getNumber)
                .containsExactlyInAnyOrder("1234567890123456", "1111222233334444");
    }

    @Test
    @DisplayName("Should return empty list when no cards found by ids")
    void findByIdIn_ShouldReturnEmptyList_WhenNoCardsFound() {
        List<CardInfo> foundCards = cardInfoRepository.findByIdIn(Arrays.asList(999L, 998L));

        assertThat(foundCards).isEmpty();
    }

    @Test
    @DisplayName("Should find cards by user id successfully")
    void findByUserId_ShouldReturnCards_WhenUserHasCards() {
        User savedUser1 = userRepository.save(testUser1);
        User savedUser2 = userRepository.save(testUser2);
        
        testCard1.setUser(savedUser1);
        testCard2.setUser(savedUser1);
        testCard3.setUser(savedUser2);
        
        cardInfoRepository.save(testCard1);
        cardInfoRepository.save(testCard2);
        cardInfoRepository.save(testCard3);
        entityManager.flush();

        List<CardInfo> userCards = cardInfoRepository.findByUserId(savedUser1.getId());

        assertThat(userCards).hasSize(2);
        assertThat(userCards).extracting(CardInfo::getNumber)
                .containsExactlyInAnyOrder("1234567890123456", "9876543210987654");
        assertThat(userCards).extracting(card -> card.getUser().getId())
                .containsOnly(savedUser1.getId());
    }

    @Test
    @DisplayName("Should return empty list when user has no cards")
    void findByUserId_ShouldReturnEmptyList_WhenUserHasNoCards() {
        User savedUser = userRepository.save(testUser1);
        entityManager.flush();

        List<CardInfo> userCards = cardInfoRepository.findByUserId(savedUser.getId());

        assertThat(userCards).isEmpty();
    }

    @Test
    @DisplayName("Should delete card info by id successfully")
    void deleteById_ShouldDeleteCardInfo_WhenCardExists() {
        User savedUser = userRepository.save(testUser1);
        testCard1.setUser(savedUser);
        CardInfo savedCard = cardInfoRepository.save(testCard1);
        entityManager.flush();

        cardInfoRepository.deleteById(savedCard.getId());
        entityManager.flush();

        Optional<CardInfo> foundCard = cardInfoRepository.findById(savedCard.getId());
        assertThat(foundCard).isEmpty();
    }

    @Test
    @DisplayName("Should find all cards")
    void findAll_ShouldReturnAllCards_WhenCardsExist() {
        User savedUser1 = userRepository.save(testUser1);
        User savedUser2 = userRepository.save(testUser2);
        
        testCard1.setUser(savedUser1);
        testCard2.setUser(savedUser1);
        testCard3.setUser(savedUser2);
        
        cardInfoRepository.save(testCard1);
        cardInfoRepository.save(testCard2);
        cardInfoRepository.save(testCard3);
        entityManager.flush();

        List<CardInfo> allCards = cardInfoRepository.findAll();

        assertThat(allCards).hasSize(3);
        assertThat(allCards).extracting(CardInfo::getNumber)
                .containsExactlyInAnyOrder("1234567890123456", "9876543210987654", "1111222233334444");
    }

    @Test
    @DisplayName("Should maintain relationship with user when saving card")
    void save_ShouldMaintainUserRelationship_WhenSavingCard() {
        User savedUser = userRepository.save(testUser1);
        testCard1.setUser(savedUser);

        CardInfo savedCard = cardInfoRepository.save(testCard1);
        entityManager.flush();
        entityManager.clear();

        Optional<CardInfo> foundCard = cardInfoRepository.findById(savedCard.getId());
        assertThat(foundCard).isPresent();
        assertThat(foundCard.get().getUser()).isNotNull();
        assertThat(foundCard.get().getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(foundCard.get().getUser().getName()).isEqualTo("John");
    }

    @Test
    @DisplayName("Should cascade delete when user is deleted")
    void deleteUser_ShouldCascadeDeleteCards_WhenUserHasCards() {
        User savedUser = userRepository.save(testUser1);
        
        testCard1.setUser(savedUser);
        testCard2.setUser(savedUser);
        savedUser.addCardInfo(testCard1);
        savedUser.addCardInfo(testCard2);
        
        CardInfo savedCard1 = cardInfoRepository.save(testCard1);
        CardInfo savedCard2 = cardInfoRepository.save(testCard2);
        entityManager.flush();

        userRepository.deleteById(savedUser.getId());
        entityManager.flush();

        Optional<CardInfo> foundCard1 = cardInfoRepository.findById(savedCard1.getId());
        Optional<CardInfo> foundCard2 = cardInfoRepository.findById(savedCard2.getId());
        
        assertThat(foundCard1).isEmpty();
        assertThat(foundCard2).isEmpty();
    }

    @Test
    @DisplayName("Should allow duplicate card numbers since no unique constraint exists")
    void save_ShouldAllowDuplicateCardNumbers_WhenNoUniqueConstraint() {
        User savedUser = userRepository.save(testUser1);
        testCard1.setUser(savedUser);
        cardInfoRepository.save(testCard1);
        entityManager.flush();

        CardInfo duplicateCard = CardInfo.builder()
                .number("1234567890123456")
                .holder("Another Holder")
                .expirationDate(LocalDate.of(2026, 1, 1))
                .user(savedUser)
                .build();

        CardInfo savedCard2 = cardInfoRepository.save(duplicateCard);
        entityManager.flush();

        assertThat(savedCard2).isNotNull();
        assertThat(savedCard2.getId()).isNotNull();
        assertThat(savedCard2.getNumber()).isEqualTo("1234567890123456");
        assertThat(savedCard2.getHolder()).isEqualTo("Another Holder");
        
        List<CardInfo> cardsWithSameNumber = cardInfoRepository.findAll().stream()
                .filter(card -> "1234567890123456".equals(card.getNumber()))
                .toList();
        assertThat(cardsWithSameNumber).hasSize(2);
    }

}
