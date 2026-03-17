package com.example.workmanagement.domain.task.repository;

import com.example.workmanagement.domain.task.domain.model.Task;
import com.example.workmanagement.domain.task.domain.model.TaskStatus;
import com.example.workmanagement.domain.task.service.result.TaskDetailResult;
import com.example.workmanagement.domain.task.service.result.TaskSummaryResult;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
            select new com.example.workmanagement.domain.task.service.result.TaskDetailResult(
                t.id,
                t.projectId,
                t.authorId,
                t.title,
                t.description,
                t.status,
                t.priority,
                t.startDate,
                t.dueDate,
                t.createdAt,
                t.updatedAt
            )
            from Task t
            where t.id = :taskId
            """)
    Optional<TaskDetailResult> findDetailById(@Param("taskId") Long taskId);

    @Query("""
            select new com.example.workmanagement.domain.task.service.result.TaskSummaryResult(
                t.id,
                t.projectId,
                t.title,
                t.status,
                t.priority,
                t.startDate,
                t.dueDate,
                t.authorId,
                t.createdAt,
                t.updatedAt
            )
            from Task t
            where t.projectId = :projectId
            """)
    Page<TaskSummaryResult> findSummaryByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    @Query("""
            select new com.example.workmanagement.domain.task.service.result.TaskSummaryResult(
                t.id,
                t.projectId,
                t.title,
                t.status,
                t.priority,
                t.startDate,
                t.dueDate,
                t.authorId,
                t.createdAt,
                t.updatedAt
            )
            from Task t
            where t.projectId = :projectId
              and t.status in :statuses
            """)
    Page<TaskSummaryResult> findSummaryByProjectIdAndStatusIn(
            @Param("projectId") Long projectId,
            @Param("statuses") Collection<TaskStatus> statuses,
            Pageable pageable
    );
}
