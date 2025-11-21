package com.innowise.paymentservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.paymentservice.model.EventStatus;
import com.innowise.paymentservice.model.document.OutboxEvent;
import com.innowise.paymentservice.model.dto.CreatePaymentEvent;
import com.innowise.paymentservice.repository.OutboxEventRepository;
import com.innowise.paymentservice.service.KafkaService;
import com.innowise.paymentservice.service.OutboxEventScheduler;
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
            CreatePaymentEvent createPaymentEvent = deserializeEvent(event);
            kafkaService.sendCreatePaymentEvent(createPaymentEvent);

            markAsSent(event);
            log.info("Successfully sent event for order {}", event.getOrderId());
        } catch (Exception e) {
            handleEventFailure(event, e);
        }
    }

    private CreatePaymentEvent deserializeEvent(OutboxEvent event) throws JsonProcessingException {
        return objectMapper.readValue(event.getPayload(), CreatePaymentEvent.class);
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
            log.error("CREATE_PAYMENT event for order {} FAILED after {} retries: {}",
                    event.getOrderId(), MAX_RETRY_COUNT, e.getMessage(), e);
        }

        outboxEventRepository.save(event);
    }

}
