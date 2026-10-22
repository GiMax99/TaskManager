--liquibase formatted sql

--changeset mgichevsky:003-add-unique-username
--comment: Уникальность логина пользователя
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_username ON users (username);
--rollback DROP INDEX IF EXISTS uq_users_username;
