package com.gimax.taskmanager.domain;

/**
 * Статус задачи.
 */
public enum TaskStatus {

    /**
     * Задача создана и ожидает выполнения.
     */
    TODO,

    /**
     * Задача находится в работе.
     */
    IN_PROGRESS,

    /**
     * Задача выполнена.
     */
    DONE
}
