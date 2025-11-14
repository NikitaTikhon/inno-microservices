package com.innowise.paymentservice.integration.service;

import com.innowise.paymentservice.config.KafkaConfig;
import com.innowise.paymentservice.integration.BaseIntegrationTest;
import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.model.dto.CreatePaymentEvent;
import com.innowise.paymentservice.model.dto.PaymentRequest;
import com.innowise.paymentservice.repository.PaymentRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.utils.ContainerTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class PaymentServiceIT extends BaseIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private KafkaTemplate<String, PaymentRequest> orderProducer;
    private KafkaMessageListenerContainer<String, CreatePaymentEvent> paymentConsumer;
    private BlockingQueue<ConsumerRecord<String, CreatePaymentEvent>> paymentEvents;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        paymentEvents = new LinkedBlockingQueue<>();
        
        orderProducer = createOrderProducer();
        
        paymentConsumer = createPaymentConsumer();
        paymentConsumer.start();
        ContainerTestUtils.waitForAssignment(paymentConsumer, 3);
    }

    @AfterEach
    void tearDown() {
        if (paymentConsumer != null) {
            paymentConsumer.stop();
        }
    }

    @Test
    @DisplayName("Should process CREATE_ORDER event and send CREATE_PAYMENT with SUCCESS status")
    void consumeCreateOrderEvent_ShouldCreatePaymentAndSendEvent_WhenRandomNumberIsEven() throws Exception {
        Long orderId = 1L;
        stubRandomNumbers(42L);

        sendOrderEvent(orderId, 100L, BigDecimal.valueOf(250.00));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(paymentRepository.findByOrderId(orderId))
                        .isPresent()
                        .get()
                        .satisfies(payment -> assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS))
        );

        CreatePaymentEvent event = pollPaymentEvent();
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    @DisplayName("Should process CREATE_ORDER event and send CREATE_PAYMENT with FAILED status")
    void consumeCreateOrderEvent_ShouldCreatePaymentAndSendEvent_WhenRandomNumberIsOdd() throws Exception {
        Long orderId = 2L;
        stubRandomNumbers(13L);

        sendOrderEvent(orderId, 200L, BigDecimal.valueOf(500.00));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(paymentRepository.findByOrderId(orderId))
                        .isPresent()
                        .get()
                        .satisfies(payment -> assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED))
        );

        CreatePaymentEvent event = pollPaymentEvent();
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("Should not create duplicate payment for same orderId (idempotence)")
    void consumeCreateOrderEvent_ShouldNotCreateDuplicate_WhenSameOrderIdSentTwice() throws Exception {
        Long orderId = 3L;
        stubRandomNumbers(42L);

        sendOrderEvent(orderId, 300L, BigDecimal.valueOf(100.00));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(paymentRepository.findAll()).hasSize(1);
            assertThat(paymentEvents).hasSize(1);
        });

        assertThat(paymentEvents.poll(1, TimeUnit.SECONDS)).isNotNull();

        sendOrderEvent(orderId, 300L, BigDecimal.valueOf(100.00));

        await().during(5, TimeUnit.SECONDS)
                .atMost(7, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(paymentRepository.findAll()).hasSize(1);
                    assertThat(paymentEvents).isEmpty();
                });
    }

    @Test
    @DisplayName("Should use orderId as Kafka message key for partitioning")
    void consumeCreateOrderEvent_ShouldUseOrderIdAsKey() throws Exception {
        Long orderId = 4L;
        stubRandomNumbers(20L);

        sendOrderEvent(orderId, 400L, BigDecimal.valueOf(150.00));

        ConsumerRecord<String, CreatePaymentEvent> consumerRecord = paymentEvents.poll(10, TimeUnit.SECONDS);
        assertThat(consumerRecord).isNotNull();
        assertThat(consumerRecord.key()).isEqualTo(orderId.toString());
    }

    private void sendOrderEvent(Long orderId, Long userId, BigDecimal amount) {
        PaymentRequest request = PaymentRequest.builder()
                .orderId(orderId)
                .userId(userId)
                .paymentAmount(amount)
                .build();

        orderProducer.send(KafkaConfig.ORDER_CREATED_TOPIC, orderId.toString(), request);
    }

    private CreatePaymentEvent pollPaymentEvent() throws InterruptedException {
        ConsumerRecord<String, CreatePaymentEvent> consumerRecord = paymentEvents.poll(10, TimeUnit.SECONDS);
        assertThat(consumerRecord).isNotNull();

        return consumerRecord.value();
    }

    private KafkaTemplate<String, PaymentRequest> createOrderProducer() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getKafkaBootstrapServers());
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configs));
    }

    private KafkaMessageListenerContainer<String, CreatePaymentEvent> createPaymentConsumer() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getKafkaBootstrapServers());
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "test-payment-consumer");
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configs.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configs.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CreatePaymentEvent.class);
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        ContainerProperties containerProps = new ContainerProperties(KafkaConfig.PAYMENT_CREATED_TOPIC);
        KafkaMessageListenerContainer<String, CreatePaymentEvent> container =
                new KafkaMessageListenerContainer<>(new DefaultKafkaConsumerFactory<>(configs), containerProps);

        container.setupMessageListener((MessageListener<String, CreatePaymentEvent>) paymentEvents::add);

        return container;
    }

    private void stubRandomNumbers(Long... randomNumbers) {
        String json = Arrays.toString(randomNumbers);

        stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(json)));
    }

}

