package com.example.workmanagement.domain.task.repository;

import com.example.workmanagement.domain.task.domain.model.TaskAssignee;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, Long> {

    boolean existsByTaskIdAndUserId(Long taskId, Long userId);

    Optional<TaskAssignee> findByTaskIdAndUserId(Long taskId, Long userId);

    long countByTaskId(Long taskId);
}
