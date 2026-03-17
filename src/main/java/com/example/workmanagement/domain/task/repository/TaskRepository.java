package com.example.workmanagement.domain.task.repository;

import com.example.workmanagement.domain.task.domain.model.Task;
import com.example.workmanagement.domain.task.domain.model.TaskStatus;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findAllByProjectId(Long projectId, Pageable pageable);

    Page<Task> findAllByProjectIdAndStatusIn(Long projectId, Collection<TaskStatus> statuses, Pageable pageable);
}
