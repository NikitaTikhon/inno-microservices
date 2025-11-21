package com.innowise.paymentservice.model.document;

import com.innowise.paymentservice.model.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "outbox_events")
public class OutboxEvent {

    @Id
    private ObjectId id;

    @Field("order_id")
    private Long orderId;

    private String payload;

    @Builder.Default
    @Field("retry_count")
    private Integer retryCount = 0;

    @Field("event_status")
    private EventStatus eventStatus;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

}
