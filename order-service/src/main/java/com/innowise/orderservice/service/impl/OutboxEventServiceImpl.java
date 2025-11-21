package com.innowise.orderservice.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.exception.OutboxEventSerializationException;
import com.innowise.orderservice.model.EventStatus;
import com.innowise.orderservice.model.dto.CreateOrderEvent;
import com.innowise.orderservice.model.entity.OutboxEvent;
import com.innowise.orderservice.repository.OutboxEventRepository;
import com.innowise.orderservice.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventServiceImpl implements OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void save(CreateOrderEvent event) {
        String payload = serializeEvent(event);
        
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .orderId(event.getOrderId())
                .payload(payload)
                .eventStatus(EventStatus.PENDING)
                .build();

        outboxEventRepository.save(outboxEvent);
    }

    private String serializeEvent(CreateOrderEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize CreateOrderEvent for order {}", event.getOrderId(), e);
            throw new OutboxEventSerializationException(
                    "Failed to serialize event for order " + event.getOrderId());
        }
    }

}
