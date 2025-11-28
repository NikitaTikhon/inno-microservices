package com.innowise.paymentservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.paymentservice.exception.OutboxEventSerializationException;
import com.innowise.paymentservice.model.EventStatus;
import com.innowise.paymentservice.model.document.OutboxEvent;
import com.innowise.paymentservice.model.dto.CreatePaymentEvent;
import com.innowise.paymentservice.repository.OutboxEventRepository;
import com.innowise.paymentservice.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventServiceImpl implements OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void save(CreatePaymentEvent event) {
        String payload = serializeEvent(event);
        
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .orderId(event.getOrderId())
                .payload(payload)
                .eventStatus(EventStatus.PENDING)
                .build();

        outboxEventRepository.save(outboxEvent);
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return outboxEventRepository.existsByOrderId(orderId);
    }

    private String serializeEvent(CreatePaymentEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize CreatePaymentEvent for order {}", event.getOrderId(), e);
            throw new OutboxEventSerializationException(
                    "Failed to serialize event for order " + event.getOrderId());
        }
    }

}
