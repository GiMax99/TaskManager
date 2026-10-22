--liquibase formatted sql

--changeset mgichevsky:001-create-users-table
--comment: Создание таблицы пользователей
CREATE TABLE IF NOT EXISTS users
(
    id       UUID         NOT NULL,
    username VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    password VARCHAR(60) NOT NULL,
    role     VARCHAR(32)  NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);
COMMENT ON TABLE users IS 'Пользователи системы';
COMMENT ON COLUMN users.id IS 'Уникальный идентификатор пользователя';
COMMENT ON COLUMN users.username IS 'Логин пользователя';
COMMENT ON COLUMN users.email IS 'Электронная почта (уникальная)';
COMMENT ON COLUMN users.password IS 'Хешированный пароль (BCrypt)';
COMMENT ON COLUMN users.role IS 'Роль пользователя';
--rollback DROP TABLE IF EXISTS users;
