--liquibase formatted sql

--changeset mgichevsky:002-create-tasks-table
--comment: Создание таблицы задач
CREATE TABLE IF NOT EXISTS tasks
(
    id          UUID                     NOT NULL,
    title       VARCHAR(255)             NOT NULL,
    description VARCHAR(1000),
    status      VARCHAR(32)              NOT NULL,
    priority    VARCHAR(32)              NOT NULL,
    author_id   UUID                     NOT NULL,
    assignee_id UUID,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT pk_tasks PRIMARY KEY (id),
    CONSTRAINT fk_tasks_author FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_tasks_assignee FOREIGN KEY (assignee_id) REFERENCES users (id) ON DELETE SET NULL
);
COMMENT ON TABLE tasks IS 'Задачи';
COMMENT ON COLUMN tasks.id IS 'Уникальный идентификатор задачи';
COMMENT ON COLUMN tasks.title IS 'Название задачи';
COMMENT ON COLUMN tasks.description IS 'Описание задачи';
COMMENT ON COLUMN tasks.status IS 'Статус задачи';
COMMENT ON COLUMN tasks.priority IS 'Приоритет задачи';
COMMENT ON COLUMN tasks.author_id IS 'Автор задачи (пользователь)';
COMMENT ON COLUMN tasks.assignee_id IS 'Исполнитель задачи (может быть null)';
COMMENT ON COLUMN tasks.created_at IS 'Дата создания задачи';
COMMENT ON COLUMN tasks.updated_at IS 'Дата обновления задачи';

CREATE INDEX idx_tasks_status ON tasks (status);
CREATE INDEX idx_tasks_author_id ON tasks (author_id);
CREATE INDEX idx_tasks_assignee_id ON tasks (assignee_id);
--rollback DROP TABLE IF EXISTS tasks;
