package com.innowise.userservice.unit.util;

import com.innowise.userservice.model.dto.CardInfoRequest;
import com.innowise.userservice.model.dto.CardInfoResponse;
import com.innowise.userservice.model.entity.CardInfo;
import com.innowise.userservice.model.entity.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CardInfoUtil {

    private CardInfoUtil() {
    }

    public static CardInfoRequest cardInfoRequest() {
        return CardInfoRequest.builder()
                .userId(1L)
                .number("1234567890123456")
                .holder("Michael Walter")
                .expirationDate(LocalDate.of(2025, 12, 31))
                .build();
    }

    public static CardInfoRequest cardInfoRequest(Long userId) {
        return CardInfoRequest.builder()
                .userId(userId)
                .number("1234567890123456")
                .holder("Michael Walter")
                .expirationDate(LocalDate.of(2025, 12, 31))
                .build();
    }

    public static CardInfoRequest cardInfoRequest(String number, String holder) {
        return CardInfoRequest.builder()
                .userId(1L)
                .number(number)
                .holder(holder)
                .expirationDate(LocalDate.of(2025, 12, 31))
                .build();
    }

    public static CardInfoResponse cardInfoResponse(Long id) {
        return CardInfoResponse.builder()
                .id(id)
                .number("1234567890123456")
                .holder("Michael Walter")
                .expirationDate(LocalDate.of(2025, 12, 31))
                .build();
    }

    public static CardInfoResponse cardInfoResponse(Long id, String number, String holder) {
        return CardInfoResponse.builder()
                .id(id)
                .number(number)
                .holder(holder)
                .expirationDate(LocalDate.of(2025, 12, 31))
                .build();
    }

    public static CardInfo cardInfo() {
        return CardInfo.builder()
                .number("1234567890123456")
                .holder("Michael Walter")
                .expirationDate(LocalDate.of(2025, 12, 31))
                .build();
    }

    public static CardInfo cardInfo(Long id) {
        CardInfo cardInfo = cardInfo();
        cardInfo.setId(id);
        return cardInfo;
    }

    public static CardInfo cardInfo(Long id, User user) {
        CardInfo cardInfo = cardInfo(id);
        cardInfo.setUser(user);
        return cardInfo;
    }

    public static CardInfo cardInfo(String number, String holder) {
        return CardInfo.builder()
                .number(number)
                .holder(holder)
                .expirationDate(LocalDate.of(2025, 12, 31))
                .build();
    }

    public static List<CardInfoResponse> cardsInfoResponse(Long count) {
        List<CardInfoResponse> cardsInfo = new ArrayList<>();

        for (long i = 1; i <= count; i++) {
            cardsInfo.add(
                    CardInfoResponse.builder()
                            .id(i)
                            .number("123456789012345" + i)
                            .holder("Michael Walter" + i)
                            .expirationDate(LocalDate.of(2025, 12, 31))
                            .build()
            );
        }

        return cardsInfo;
    }

    public static List<CardInfo> cardsInfo(Long count) {
        List<CardInfo> cardsInfo = new ArrayList<>();

        for (long i = 1; i <= count; i++) {
            cardsInfo.add(
                    CardInfo.builder()
                            .id(i)
                            .number("123456789012345" + i)
                            .holder("Michael Walter" + i)
                            .expirationDate(LocalDate.of(2025, 12, 31))
                            .build()
            );
        }

        return cardsInfo;
    }

    public static List<CardInfo> cardsInfo(Long count, User user) {
        List<CardInfo> cardsInfo = new ArrayList<>();

        for (long i = 1; i <= count; i++) {
            CardInfo cardInfo = CardInfo.builder()
                    .id(i)
                    .number("123456789012345" + i)
                    .holder("Michael Walter" + i)
                    .expirationDate(LocalDate.of(2025, 12, 31))
                    .user(user)
                    .build();
            cardsInfo.add(cardInfo);
        }

        return cardsInfo;
    }

}
