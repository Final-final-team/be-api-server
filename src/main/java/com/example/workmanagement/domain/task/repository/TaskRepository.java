package com.example.workmanagement.domain.task.repository;

import com.example.workmanagement.domain.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

// 실제 task 도메인 연동이 복구되기 전까지
// review 도메인 컴파일 유지를 위한 임시 호환 repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}
