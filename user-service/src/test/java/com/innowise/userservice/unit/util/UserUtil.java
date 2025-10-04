package com.innowise.userservice.unit.util;

import com.innowise.userservice.model.dto.UserRequest;
import com.innowise.userservice.model.dto.UserResponse;
import com.innowise.userservice.model.entity.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserUtil {

    private UserUtil() {
    }

    public static UserRequest userRequest() {
        return UserRequest.builder()
                .name("Michael")
                .surname("Walter")
                .birthDate(LocalDate.of(2000, 2, 10))
                .email("michael.walter@gmail.com")
                .build();
    }

    public static UserResponse userResponse(Long id) {
        return UserResponse.builder()
                .id(id)
                .name("Michael")
                .surname("Walter")
                .birthDate(LocalDate.of(2000, 2, 10))
                .email("michael.walter@gmail.com")
                .build();
    }

    public static User user() {
        return User.builder()
                .name("Michael")
                .surname("Walter")
                .birthDate(LocalDate.of(2000, 2, 10))
                .build();
    }

    public static User user(Long id) {
        User user = user();
        user.setId(id);

        return user;
    }

    public static User user(String email) {
        User user = user();
        user.setEmail(email);

        return user;
    }

    public static List<UserResponse> usersResponse(Long count) {
        List<UserResponse> users = new ArrayList<>();

        for (long i = 1; i <= count; i++) {
            users.add(
                    UserResponse.builder()
                            .id(i)
                            .name("Michael" + i)
                            .surname("Walter" + i)
                            .birthDate(LocalDate.of(2000, 2, 10))
                            .email("michael" + i + ".walter" + i + "@gmail.com")
                            .build()
            );
        }

        return users;
    }

    public static List<User> users(Long count) {
        List<User> users = new ArrayList<>();

        for (long i = 1; i <= count; i++) {
            users.add(
                    User.builder()
                            .id(i)
                            .name("Michael" + i)
                            .surname("Walter" + i)
                            .birthDate(LocalDate.of(2000, 2, 10))
                            .email("michael" + i + ".walter" + i + "@gmail.com")
                            .build()
            );
        }

        return users;
    }

}
