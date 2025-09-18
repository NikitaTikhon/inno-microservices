--liquibase formatted sql

--changeset mikitatsikhan:1
CREATE TABLE IF NOT EXISTS users
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(50) NOT NULL,
    surname VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

--changeset mikitatsikhan:2
CREATE TABLE IF NOT EXISTS card_info
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    number VARCHAR(50) NOT NULL,
    holder VARCHAR(50) NOT NULL,
    expiration_date DATE NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_card_info_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

--changeset mikitatsikhan:3
CREATE INDEX IF NOT EXISTS idx_card_info_user_id ON card_info(user_id);
CREATE INDEX IF NOT EXISTS idx_card_info_number ON card_info(number);
