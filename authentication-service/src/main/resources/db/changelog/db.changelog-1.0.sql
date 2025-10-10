--liquibase formatted sql

--changeset mikitatsikhan:1
CREATE TABLE IF NOT EXISTS users
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    creation_date DATE NOT NULL
);

--changeset mikitatsikhan:2
CREATE TABLE IF NOT EXISTS roles
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(30) NOT NULL UNIQUE
);

--changeset mikitatsikhan:3
CREATE TABLE IF NOT EXISTS users_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

--changeset mikitatsikhan:4
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)
