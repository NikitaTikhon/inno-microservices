package com.innowise.userservice.integration.repository;

import com.innowise.userservice.integration.BaseRepositoryIntegrationTest;
import com.innowise.userservice.model.entity.User;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRepositoryIT extends BaseRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser1;
    private User testUser2;
    private User testUser3;

    @BeforeEach
    void setUp() {
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

        testUser3 = User.builder()
                .name("Bob")
                .surname("Johnson")
                .email("bob.johnson@example.com")
                .birthDate(LocalDate.of(1992, 12, 3))
                .build();
    }

    @Test
    @DisplayName("Should save user successfully")
    void save_ShouldSaveUser_WhenValidUser() {
        User savedUser = userRepository.save(testUser1);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("John");
        assertThat(savedUser.getSurname()).isEqualTo("Doe");
        assertThat(savedUser.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(savedUser.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 15));
    }

    @Test
    @DisplayName("Should find user by id successfully")
    void findById_ShouldReturnUser_WhenUserExists() {
        User savedUser = userRepository.save(testUser1);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("John");
        assertThat(foundUser.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should return empty when user not found by id")
    void findById_ShouldReturnEmpty_WhenUserNotFound() {
        Optional<User> foundUser = userRepository.findById(999L);

        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should find user by email successfully")
    void findByEmail_ShouldReturnUser_WhenUserExists() {
        userRepository.save(testUser1);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findByEmail("john.doe@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("John");
        assertThat(foundUser.get().getSurname()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void findByEmail_ShouldReturnEmpty_WhenUserNotFound() {
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should find users by ids successfully")
    void findByIdIn_ShouldReturnUsers_WhenUsersExist() {
        User savedUser1 = userRepository.save(testUser1);
        User savedUser3 = userRepository.save(testUser3);
        entityManager.flush();

        List<Long> userIds = Arrays.asList(savedUser1.getId(), savedUser3.getId());

        List<User> foundUsers = userRepository.findByIdIn(userIds);

        assertThat(foundUsers).hasSize(2);
        assertThat(foundUsers).extracting(User::getName)
                .containsExactlyInAnyOrder("John", "Bob");
    }

    @Test
    @DisplayName("Should return empty list when no users found by ids")
    void findByIdIn_ShouldReturnEmptyList_WhenNoUsersFound() {
        List<User> foundUsers = userRepository.findByIdIn(Arrays.asList(999L, 998L));

        assertThat(foundUsers).isEmpty();
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void existsByEmail_ShouldReturnTrue_WhenUserExists() {
        userRepository.save(testUser1);
        entityManager.flush();

        boolean exists = userRepository.existsByEmail("john.doe@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when user does not exist by email")
    void existsByEmail_ShouldReturnFalse_WhenUserNotExists() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should find user with cards by id")
    void findByIdWithCards_ShouldReturnUserWithCards_WhenUserExists() {
        User savedUser = userRepository.save(testUser1);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findByIdWithCards(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("John");
        assertThat(foundUser.get().getCardsInfo()).isNotNull();
    }

    @Test
    @DisplayName("Should delete user by id successfully")
    void deleteById_ShouldDeleteUser_WhenUserExists() {
        User savedUser = userRepository.save(testUser1);
        entityManager.flush();

        userRepository.deleteById(savedUser.getId());
        entityManager.flush();

        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should find all users")
    void findAll_ShouldReturnAllUsers_WhenUsersExist() {
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);
        entityManager.flush();

        List<User> allUsers = userRepository.findAll();

        assertThat(allUsers).hasSize(3);
        assertThat(allUsers).extracting(User::getName)
                .containsExactlyInAnyOrder("John", "Jane", "Bob");
    }

    @Test
    @DisplayName("Should handle unique email constraint")
    void save_ShouldThrowException_WhenEmailAlreadyExists() {
        userRepository.save(testUser1);
        entityManager.flush();

        User duplicateEmailUser = User.builder()
                .name("Another")
                .surname("User")
                .email("john.doe@example.com")
                .birthDate(LocalDate.of(1995, 1, 1))
                .build();

        assertThatThrownBy(() -> {
            userRepository.save(duplicateEmailUser);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

}
