--liquibase formatted sql

--changeset mikitatsikhan:1 context:dev
INSERT INTO roles (name) VALUES
('ROLE_USER'),
('ROLE_ADMIN');

--changeset mikitatsikhan:2 context:dev
INSERT INTO users (email, password, creation_date) VALUES
('user@gmail.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', CURRENT_DATE),
('admin@gmail.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', CURRENT_DATE),
('user1@gmail.com', '$2a$10$kQ8fZ4X5X5X5X5X5X5X5XOJ5fZ4X5X5X5X5X5X5X5XOJ5fZ4X5X5a', CURRENT_DATE),
('user2@gmail.com', '$2a$10$lR9gA6Y6Y6Y6Y6Y6Y6Y6YPK6gA6Y6Y6Y6Y6Y6Y6Y6YPK6gA6Y6Y6b', CURRENT_DATE),
('user3@gmail.com', '$2a$10$mS0hB7Z7Z7Z7Z7Z7Z7Z7ZQL7hB7Z7Z7Z7Z7Z7Z7Z7ZQL7hB7Z7Z7c', CURRENT_DATE),
('user4@gmail.com', '$2a$10$nT1iC8A8A8A8A8A8A8A8ARM8iC8A8A8A8A8A8A8A8ARM8iC8A8A8d', CURRENT_DATE),
('user5@gmail.com', '$2a$10$oU2jD9B9B9B9B9B9B9B9BSN9jD9B9B9B9B9B9B9B9BSN9jD9B9B9e', CURRENT_DATE),
('admin1@gmail.com', '$2a$10$pV3kE0C0C0C0C0C0C0C0CTO0kE0C0C0C0C0C0C0C0CTO0kE0C0C0f', CURRENT_DATE),
('admin2@gmail.com', '$2a$10$qW4lF1D1D1D1D1D1D1D1DUP1lF1D1D1D1D1D1D1D1DUP1lF1D1D1g', CURRENT_DATE),
('superuser@gmail.com', '$2a$10$rX5mG2E2E2E2E2E2E2E2EVQ2mG2E2E2E2E2E2E2E2EVQ2mG2E2E2h', CURRENT_DATE);

--changeset mikitatsikhan:3 context:dev
INSERT INTO users_roles (user_id, role_id) VALUES
(1, 1),
(3, 1),
(4, 1),
(5, 1),
(6, 1),
(7, 1),
(2, 2),
(8, 2),
(9, 2),
(10, 1),
(10, 2);
