package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.request.CardInfoRequest;
import com.innowise.userservice.dto.response.CardInfoResponse;
import com.innowise.userservice.entity.CardInfo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * This interface defines the contract for mapping between a CardInfo entity and a DTO.
 * It's responsible for converting data from the domain model to the data transfer object
 */
@Mapper(componentModel = "spring")
public interface CardInfoMapper {

    CardInfo cardInfoRequestToCardInfo(CardInfoRequest cardInfoRequest);

    void updateCardInfoFromCardInfoRequest(CardInfoRequest cardInfoRequest, @MappingTarget CardInfo cardInfo);

    CardInfoResponse cardInfoToCardInfoResponse(CardInfo cardInfo);

    List<CardInfoResponse> cardsInfoToCardsInfoResponse(List<CardInfo> cardsInfo);

}
