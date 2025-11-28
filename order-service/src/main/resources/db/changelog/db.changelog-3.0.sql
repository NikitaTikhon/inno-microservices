--changeset mikitatsikhan:1
CREATE TABLE IF NOT EXISTS outbox_events
(
    id           BIGSERIAL PRIMARY KEY NOT NULL,
    order_id     BIGINT UNIQUE         NOT NULL,
    payload      VARCHAR(255) NOT NULL,
    retry_count  INT NOT NULL,
    event_status VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP(6) NOT NULL
);

--changeset mikitatsikhan:2
CREATE INDEX idx_outbox_events_order_id ON outbox_events (order_id);