package com.innowise.orderservice.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.model.EventStatus;
import com.innowise.orderservice.model.dto.CreateOrderEvent;
import com.innowise.orderservice.model.dto.CreatePaymentEvent;
import com.innowise.orderservice.model.entity.OutboxEvent;
import com.innowise.orderservice.repository.OutboxEventRepository;
import com.innowise.orderservice.service.KafkaService;
import com.innowise.orderservice.service.OutboxEventScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventSchedulerImpl implements OutboxEventScheduler {

    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY_COUNT = 5;
    private static final long FIXED_DELAY = 3000;

    private final KafkaService kafkaService;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Scheduled(fixedDelay = FIXED_DELAY)
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByEventStatusOrderByCreatedAt(
                EventStatus.PENDING,
                PageRequest.of(0, BATCH_SIZE)
        );

        if (events.isEmpty()) {
            return;
        }

        events.forEach(this::processEvent);
    }

    private void processEvent(OutboxEvent event) {
        try {
            CreateOrderEvent createOrderEvent = deserializeEvent(event);
            kafkaService.sendCreateOrderEvent(createOrderEvent);

            markAsSent(event);
        } catch (Exception e) {
            handleEventFailure(event, e);
        }
    }

    private CreateOrderEvent deserializeEvent(OutboxEvent event) throws JsonProcessingException {
        return objectMapper.readValue(event.getPayload(), CreateOrderEvent.class);
    }

    private void markAsSent(OutboxEvent event) {
        event.setEventStatus(EventStatus.SENT);
        outboxEventRepository.save(event);
    }

    private void handleEventFailure(OutboxEvent event, Exception e) {
        int retryCount = event.getRetryCount() + 1;
        event.setRetryCount(retryCount);

        if (retryCount >= MAX_RETRY_COUNT) {
            event.setEventStatus(EventStatus.FAILED);
            log.error("CREATE_ORDER for order {} FAILED after {} retries: {}",
                    event.getOrderId(), MAX_RETRY_COUNT, e.getMessage(), e);
        }

        outboxEventRepository.save(event);
    }

}
