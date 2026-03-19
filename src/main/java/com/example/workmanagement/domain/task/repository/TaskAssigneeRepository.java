package com.example.workmanagement.domain.task.repository;

import com.example.workmanagement.domain.task.domain.model.TaskAssignee;
import com.example.workmanagement.domain.task.service.result.TaskAssigneeRowResult;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, Long> {

    boolean existsByTaskIdAndUserId(Long taskId, Long userId);

    Optional<TaskAssignee> findByTaskIdAndUserId(Long taskId, Long userId);

    long countByTaskId(Long taskId);

    @Query("""
            select new com.example.workmanagement.domain.task.service.result.TaskAssigneeRowResult(
                ta.taskId,
                ta.userId,
                u.nickname
            )
            from TaskAssignee ta
            join com.example.workmanagement.domain.user.domain.model.User u on u.id = ta.userId
            where ta.taskId in :taskIds
            order by ta.taskId asc, ta.createdAt asc, ta.id asc
            """)
    List<TaskAssigneeRowResult> findAssigneeRowsByTaskIdIn(@Param("taskIds") Collection<Long> taskIds);
}
