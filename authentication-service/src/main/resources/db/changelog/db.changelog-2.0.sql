--liquibase formatted sql

--changeset mikitatsikhan:1
INSERT INTO roles (name) VALUES
('ROLE_USER'),
('ROLE_ADMIN')
