package com.gimax.taskmanager.repository;

import com.gimax.taskmanager.domain.Task;
import com.gimax.taskmanager.domain.TaskStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Спецификации для фильтрации задач.
 */
public class TaskSpecifications {

    public static Specification<Task> withFilters(TaskStatus status, UUID authorId, UUID assigneeId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (authorId != null) {
                predicates.add(cb.equal(root.get("author").get("id"), authorId));
            }
            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("assignee").get("id"), assigneeId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
