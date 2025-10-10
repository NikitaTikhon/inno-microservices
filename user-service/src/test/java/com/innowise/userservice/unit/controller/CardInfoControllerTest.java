package com.innowise.userservice.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.controller.CardInfoController;
import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.model.dto.CardInfoRequest;
import com.innowise.userservice.model.dto.CardInfoResponse;
import com.innowise.userservice.service.CardInfoService;
import com.innowise.userservice.unit.util.CardInfoUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = CardInfoController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class CardInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardInfoService cardInfoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create card info successfully")
    void save_ShouldCreateCardInfo_WhenValidRequest() throws Exception {
        CardInfoRequest cardInfoRequest = CardInfoUtil.cardInfoRequest();
        CardInfoResponse cardInfoResponse = CardInfoUtil.cardInfoResponse(1L);

        when(cardInfoService.save(any(CardInfoRequest.class))).thenReturn(cardInfoResponse);

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardInfoRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.number").value("1234567890123456"))
                .andExpect(jsonPath("$.holder").value("Michael Walter"))
                .andExpect(jsonPath("$.expirationDate").value("2025-12-31"));
    }

    @Test
    @DisplayName("Should return 400 when creating card info with invalid data")
    void save_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        CardInfoRequest cardInfoRequest = CardInfoRequest.builder()
                .userId(1L)
                .number("")
                .holder("")
                .expirationDate(null)
                .build();

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardInfoRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when creating card info with non-existent user")
    void save_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        CardInfoRequest cardInfoRequest = CardInfoUtil.cardInfoRequest();

        when(cardInfoService.save(any(CardInfoRequest.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardInfoRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return card info by id successfully")
    void findById_ShouldReturnCardInfo_WhenCardExists() throws Exception {
        Long cardId = 1L;
        CardInfoResponse cardInfoResponse = CardInfoUtil.cardInfoResponse(cardId);

        when(cardInfoService.findById(cardId)).thenReturn(cardInfoResponse);

        mockMvc.perform(get("/api/v1/cards/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.number").value("1234567890123456"))
                .andExpect(jsonPath("$.holder").value("Michael Walter"));
    }

    @Test
    @DisplayName("Should return 404 when card info not found by id")
    void findById_ShouldReturnNotFound_WhenCardNotFound() throws Exception {
        Long cardId = 1L;

        when(cardInfoService.findById(cardId))
                .thenThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(get("/api/v1/cards/{id}", cardId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return cards info by ids successfully")
    void findByIds_ShouldReturnCardsInfo_WhenCardsExist() throws Exception {
        List<Long> cardIds = Arrays.asList(1L, 2L, 3L);
        List<CardInfoResponse> cardsInfoResponse = CardInfoUtil.cardsInfoResponse(3L);

        when(cardInfoService.findByIds(cardIds)).thenReturn(cardsInfoResponse);

        mockMvc.perform(get("/api/v1/cards")
                        .param("ids", "1", "2", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[2].id").value(3L));
    }

    @Test
    @DisplayName("Should return empty list when no cards found by ids")
    void findByIds_ShouldReturnEmptyList_WhenNoCardsFound() throws Exception {
        List<Long> cardIds = Arrays.asList(999L, 998L);

        when(cardInfoService.findByIds(cardIds)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/cards")
                        .param("ids", "999", "998"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should return 400 when ids parameter is missing")
    void findByIds_ShouldReturnBadRequest_WhenIdsParameterMissing() throws Exception {
        mockMvc.perform(get("/api/v1/cards"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should update card info successfully")
    void updateById_ShouldUpdateCardInfo_WhenValidRequest() throws Exception {
        Long cardId = 1L;
        CardInfoRequest cardInfoRequest = CardInfoUtil.cardInfoRequest();
        CardInfoResponse cardInfoResponse = CardInfoUtil.cardInfoResponse(cardId);

        when(cardInfoService.updateById(cardId, cardInfoRequest)).thenReturn(cardInfoResponse);

        mockMvc.perform(patch("/api/v1/cards/{id}", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardInfoRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 400 when updating card info with invalid data")
    void updateById_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        Long cardId = 1L;
        CardInfoRequest cardInfoRequest = CardInfoRequest.builder()
                .userId(1L)
                .number("")
                .holder("")
                .expirationDate(null)
                .build();

        mockMvc.perform(patch("/api/v1/cards/{id}", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardInfoRequest)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Should delete card info successfully")
    void deleteById_ShouldDeleteCardInfo_WhenCardExists() throws Exception {
        Long cardId = 1L;

        when(cardInfoService.deleteById(cardId)).thenReturn(1L);

        mockMvc.perform(delete("/api/v1/cards/{id}", cardId))
                .andExpect(status().isNoContent());
    }


}
