package com.gimax.taskmanager.repository;

import com.gimax.taskmanager.domain.Task;
import io.micrometer.common.lang.NonNull;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий задач.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    @NonNull
    @Override
    @EntityGraph(attributePaths = {"author", "assignee"})
    Page<Task> findAll(Specification<Task> spec, @NonNull Pageable pageable);
}
