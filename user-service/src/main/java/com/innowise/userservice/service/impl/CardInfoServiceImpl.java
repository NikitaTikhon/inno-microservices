package com.innowise.userservice.service.impl;

import com.innowise.userservice.model.dto.CardInfoRequest;
import com.innowise.userservice.model.dto.CardInfoResponse;
import com.innowise.userservice.model.entity.CardInfo;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.CardInfoMapper;
import com.innowise.userservice.repository.CardInfoRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.CardInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CardInfoServiceImpl implements CardInfoService {

    private final CardInfoRepository cardInfoRepository;
    private final UserRepository userRepository;
    private final CardInfoMapper cardInfoMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public CardInfoResponse save(CardInfoRequest cardInfoRequest) {
        User user = userRepository.findById(cardInfoRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException(cardInfoRequest.getUserId()));

        CardInfo cardInfo = cardInfoMapper.cardInfoRequestToCardInfo(cardInfoRequest);
        user.addCardInfo(cardInfo);

        return cardInfoMapper.cardInfoToCardInfoResponse(cardInfoRepository.save(cardInfo));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public CardInfoResponse findById(Long id) {
        CardInfo cardInfo = cardInfoRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        return cardInfoMapper.cardInfoToCardInfoResponse(cardInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<CardInfoResponse> findByIds(List<Long> ids) {
        List<CardInfo> cardsInfo = cardInfoRepository.findByIdIn(ids);

        return cardInfoMapper.cardsInfoToCardsInfoResponse(cardsInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardInfoResponse updateById(Long id, CardInfoRequest cardInfoRequest) {
        CardInfo cardInfo = cardInfoRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        cardInfoMapper.updateCardInfoFromCardInfoRequest(cardInfoRequest, cardInfo);

        return cardInfoMapper.cardInfoToCardInfoResponse(cardInfoRepository.save(cardInfo));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(Long id) {
        if (!cardInfoRepository.existsById(id)) {
            throw new CardNotFoundException(id);
        }

        cardInfoRepository.deleteByIdNative(id);
    }

}
