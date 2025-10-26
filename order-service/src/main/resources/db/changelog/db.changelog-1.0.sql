--liquibase formatted sql

--changeset mikitatsikhan:1
CREATE TABLE IF NOT EXISTS orders
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    creation_date TIMESTAMP(6) NOT NULL
);

--changeset mikitatsikhan:2
CREATE TABLE IF NOT EXISTS items
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(50) NOT NULL,
    price DECIMAL NOT NULL
);

--changeset mikitatsikhan:3
CREATE TABLE IF NOT EXISTS order_items
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    order_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity BIGINT NOT NULL,
    CONSTRAINT fk_order_items_orders FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_items FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

--changeset mikitatsikhan:4
CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_item_id ON order_items (item_id);