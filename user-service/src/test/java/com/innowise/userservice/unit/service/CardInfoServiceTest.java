package com.innowise.userservice.unit.service;

import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.CardInfoMapper;
import com.innowise.userservice.model.dto.CardInfoRequest;
import com.innowise.userservice.model.dto.CardInfoResponse;
import com.innowise.userservice.model.entity.CardInfo;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.CardInfoRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.impl.CardInfoServiceImpl;
import com.innowise.userservice.unit.util.CardInfoUtil;
import com.innowise.userservice.unit.util.UserUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardInfoServiceTest {

    @InjectMocks
    private CardInfoServiceImpl cardInfoService;

    @Mock
    private CardInfoRepository cardInfoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardInfoMapper cardInfoMapper;

    @Test
    @DisplayName("Should create card info successfully")
    void save_ShouldCreateCardInfo_WhenUserExists() {
        Long cardId = 1L;
        Long userId = 1L;
        CardInfoRequest cardInfoRequest = CardInfoUtil.cardInfoRequest(userId);
        User user = UserUtil.user(userId);
        CardInfo cardInfo = CardInfoUtil.cardInfo();
        CardInfo createdCardInfo = CardInfoUtil.cardInfo(cardId, user);
        CardInfoResponse cardInfoResponse = CardInfoUtil.cardInfoResponse(cardId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardInfoMapper.cardInfoRequestToCardInfo(cardInfoRequest)).thenReturn(cardInfo);
        when(cardInfoRepository.save(cardInfo)).thenReturn(createdCardInfo);
        when(cardInfoMapper.cardInfoToCardInfoResponse(createdCardInfo)).thenReturn(cardInfoResponse);

        CardInfoResponse actualCardInfoResponse = cardInfoService.save(cardInfoRequest);

        assertThat(actualCardInfoResponse).isNotNull();
        assertThat(actualCardInfoResponse.getId()).isEqualTo(cardInfoResponse.getId());
        assertThat(actualCardInfoResponse.getNumber()).isEqualTo(cardInfoResponse.getNumber());
        assertThat(actualCardInfoResponse.getHolder()).isEqualTo(cardInfoResponse.getHolder());

        verify(userRepository).findById(userId);
        verify(cardInfoMapper).cardInfoRequestToCardInfo(cardInfoRequest);
        verify(cardInfoRepository).save(cardInfo);
        verify(cardInfoMapper).cardInfoToCardInfoResponse(createdCardInfo);
    }

    @Test
    @DisplayName("Should return card info by id successfully")
    void findById_ShouldReturnCardInfo_WhenCardExists() {
        Long cardId = 1L;
        CardInfo cardInfo = CardInfoUtil.cardInfo(cardId);
        CardInfoResponse cardInfoResponse = CardInfoUtil.cardInfoResponse(cardId);

        when(cardInfoRepository.findById(cardId)).thenReturn(Optional.of(cardInfo));
        when(cardInfoMapper.cardInfoToCardInfoResponse(cardInfo)).thenReturn(cardInfoResponse);

        CardInfoResponse actualCardInfoResponse = cardInfoService.findById(cardId);

        assertThat(actualCardInfoResponse).isNotNull();
        assertThat(actualCardInfoResponse.getId()).isEqualTo(cardInfoResponse.getId());
        assertThat(actualCardInfoResponse.getNumber()).isEqualTo(cardInfoResponse.getNumber());

        verify(cardInfoRepository).findById(cardId);
        verify(cardInfoMapper).cardInfoToCardInfoResponse(cardInfo);
    }

    @Test
    @DisplayName("Should return cards info by ids")
    void findByIds_ShouldReturnCardsInfo_WhenCardsExist() {
        List<Long> cardIds = Arrays.asList(1L, 2L, 3L);
        List<CardInfo> cardsInfo = CardInfoUtil.cardsInfo(3L);
        List<CardInfoResponse> cardsInfoResponse = CardInfoUtil.cardsInfoResponse(3L);

        when(cardInfoRepository.findByIdIn(cardIds)).thenReturn(cardsInfo);
        when(cardInfoMapper.cardsInfoToCardsInfoResponse(cardsInfo)).thenReturn(cardsInfoResponse);

        List<CardInfoResponse> actualCardsInfoResponse = cardInfoService.findByIds(cardIds);

        assertThat(actualCardsInfoResponse).isNotNull()
                .hasSameSizeAs(cardsInfoResponse);

        verify(cardInfoRepository).findByIdIn(cardIds);
        verify(cardInfoMapper).cardsInfoToCardsInfoResponse(cardsInfo);
    }

    @Test
    @DisplayName("Should update card info successfully")
    void updateById_ShouldUpdateCardInfo_WhenCardExists() {
        Long cardId = 1L;
        Long userId = 1L;
        Long newUserId = 2L;
        CardInfoRequest cardInfoRequest = CardInfoUtil.cardInfoRequest(newUserId);
        User oldUser = UserUtil.user(userId);
        User newUser = UserUtil.user(newUserId);
        CardInfo cardInfo = CardInfoUtil.cardInfo(cardId, oldUser);
        CardInfo updatedCardInfo = CardInfoUtil.cardInfo(cardId, newUser);
        CardInfoResponse cardInfoResponse = CardInfoUtil.cardInfoResponse(cardId);

        when(cardInfoRepository.findById(cardId)).thenReturn(Optional.of(cardInfo));
        when(userRepository.findById(newUserId)).thenReturn(Optional.of(newUser));
        when(cardInfoRepository.save(cardInfo)).thenReturn(updatedCardInfo);
        when(cardInfoMapper.cardInfoToCardInfoResponse(updatedCardInfo)).thenReturn(cardInfoResponse);

        CardInfoResponse actualCardInfoResponse = cardInfoService.updateById(cardId, cardInfoRequest);

        assertThat(actualCardInfoResponse).isNotNull();
        assertThat(actualCardInfoResponse.getId()).isEqualTo(cardInfoResponse.getId());
        assertThat(actualCardInfoResponse.getNumber()).isEqualTo(cardInfoResponse.getNumber());

        verify(cardInfoRepository).findById(cardId);
        verify(userRepository).findById(newUserId);
        verify(cardInfoMapper).updateCardInfoFromCardInfoRequest(cardInfoRequest, cardInfo);
        verify(cardInfoRepository).save(cardInfo);
        verify(cardInfoMapper).cardInfoToCardInfoResponse(updatedCardInfo);
    }

    @Test
    @DisplayName("Should update card info without changing user when user is the same")
    void updateById_ShouldUpdateCardInfo_WhenUserIsSame() {
        Long cardId = 1L;
        Long userId = 1L;
        CardInfoRequest cardInfoRequest = CardInfoUtil.cardInfoRequest(userId);
        User user = UserUtil.user(userId);
        CardInfo cardInfo = CardInfoUtil.cardInfo(cardId, user);
        CardInfo updatedCardInfo = CardInfoUtil.cardInfo(cardId, user);
        CardInfoResponse cardInfoResponse = CardInfoUtil.cardInfoResponse(cardId);

        when(cardInfoRepository.findById(cardId)).thenReturn(Optional.of(cardInfo));
        when(cardInfoRepository.save(cardInfo)).thenReturn(updatedCardInfo);
        when(cardInfoMapper.cardInfoToCardInfoResponse(updatedCardInfo)).thenReturn(cardInfoResponse);

        CardInfoResponse actualCardInfoResponse = cardInfoService.updateById(cardId, cardInfoRequest);

        assertThat(actualCardInfoResponse).isNotNull();
        assertThat(actualCardInfoResponse.getId()).isEqualTo(cardInfoResponse.getId());

        verify(cardInfoRepository).findById(cardId);
        verify(cardInfoMapper).updateCardInfoFromCardInfoRequest(cardInfoRequest, cardInfo);
        verify(cardInfoRepository).save(cardInfo);
        verify(cardInfoMapper).cardInfoToCardInfoResponse(updatedCardInfo);
    }

    @Test
    @DisplayName("Should delete card info successfully")
    void deleteById_ShouldDeleteCardInfo_WhenCardExists() {
        Long cardId = 1L;
        Long userId = 1L;
        User user = UserUtil.user(userId);
        CardInfo cardInfo = CardInfoUtil.cardInfo(cardId, user);

        when(cardInfoRepository.findById(cardId)).thenReturn(Optional.of(cardInfo));

        Long actualUserId = cardInfoService.deleteById(cardId);

        assertThat(actualUserId).isEqualTo(userId);

        verify(cardInfoRepository).findById(cardId);
        verify(cardInfoRepository).deleteById(cardId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found during save")
    void save_ShouldThrowException_WhenUserNotFound() {
        Long userId = 1L;
        CardInfoRequest cardInfoRequest = CardInfoUtil.cardInfoRequest(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardInfoService.save(cardInfoRequest))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw CardNotFoundException when card not found by id")
    void findById_ShouldThrowException_WhenCardNotFound() {
        Long cardId = 1L;

        when(cardInfoRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardInfoService.findById(cardId))
                .isInstanceOf(CardNotFoundException.class);

        verify(cardInfoRepository).findById(cardId);
    }

    @Test
    @DisplayName("Should throw CardNotFoundException when updating non-existent card")
    void updateById_ShouldThrowException_WhenCardNotFound() {
        Long cardId = 1L;
        CardInfoRequest cardInfoRequest = CardInfoUtil.cardInfoRequest();

        when(cardInfoRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardInfoService.updateById(cardId, cardInfoRequest))
                .isInstanceOf(CardNotFoundException.class);

        verify(cardInfoRepository).findById(cardId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when new user not found during update")
    void updateById_ShouldThrowException_WhenNewUserNotFound() {
        Long cardId = 1L;
        Long userId = 1L;
        Long newUserId = 2L;
        CardInfoRequest cardInfoRequest = CardInfoUtil.cardInfoRequest(newUserId);
        User oldUser = UserUtil.user(userId);
        CardInfo cardInfo = CardInfoUtil.cardInfo(cardId, oldUser);

        when(cardInfoRepository.findById(cardId)).thenReturn(Optional.of(cardInfo));
        when(userRepository.findById(newUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardInfoService.updateById(cardId, cardInfoRequest))
                .isInstanceOf(UserNotFoundException.class);

        verify(cardInfoRepository).findById(cardId);
        verify(userRepository).findById(newUserId);
    }

    @Test
    @DisplayName("Should throw CardNotFoundException when deleting non-existent card")
    void deleteById_ShouldThrowException_WhenCardNotFound() {
        Long cardId = 1L;

        when(cardInfoRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardInfoService.deleteById(cardId))
                .isInstanceOf(CardNotFoundException.class);

        verify(cardInfoRepository).findById(cardId);
    }

}
