package com.innowise.userservice.service.impl;

import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.CardInfoMapper;
import com.innowise.userservice.model.dto.CardInfoRequest;
import com.innowise.userservice.model.dto.CardInfoResponse;
import com.innowise.userservice.model.entity.CardInfo;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.CardInfoRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.CacheService;
import com.innowise.userservice.service.CardInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardInfoServiceImpl implements CardInfoService {

    private final CacheService cacheService;
    private final CardInfoRepository cardInfoRepository;
    private final UserRepository userRepository;
    private final CardInfoMapper cardInfoMapper;

    @Override
    @Transactional
    @CacheEvict(cacheNames = "users", key = "#cardInfoRequest.userId")
    public CardInfoResponse save(CardInfoRequest cardInfoRequest) {
        User user = userRepository.findById(cardInfoRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User with id: %s not found".formatted(cardInfoRequest.getUserId())));

        CardInfo cardInfo = cardInfoMapper.cardInfoRequestToCardInfo(cardInfoRequest);
        user.addCardInfo(cardInfo);

        return cardInfoMapper.cardInfoToCardInfoResponse(cardInfoRepository.save(cardInfo));
    }

    @Override
    @Transactional(readOnly = true)
    public CardInfoResponse findById(Long id) {
        CardInfo cardInfo = cardInfoRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("CardInfo with id: %s not found".formatted(id)));

        return cardInfoMapper.cardInfoToCardInfoResponse(cardInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardInfoResponse> findByIds(List<Long> ids) {
        List<CardInfo> cardsInfo = cardInfoRepository.findByIdIn(ids);

        return cardInfoMapper.cardsInfoToCardsInfoResponse(cardsInfo);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "users", key = "#cardInfoRequest.userId")
    public CardInfoResponse updateById(Long id, CardInfoRequest cardInfoRequest) {
        CardInfo cardInfo = cardInfoRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("CardInfo with id: %s not found".formatted(id)));

        cardInfoMapper.updateCardInfoFromCardInfoRequest(cardInfoRequest, cardInfo);

        return cardInfoMapper.cardInfoToCardInfoResponse(cardInfoRepository.save(cardInfo));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        CardInfo cardInfo = cardInfoRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("CardInfo with id: %s not found".formatted(id)));

        cacheService.evictUser(cardInfo.getUser().getId());

        cardInfoRepository.deleteById(id);
    }

}
