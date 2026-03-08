package com.example.workmanagement.domain.task.repository;

import com.example.workmanagement.domain.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
