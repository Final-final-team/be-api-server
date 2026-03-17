package com.example.workmanagement.domain.task.repository;

import com.example.workmanagement.domain.task.domain.model.Task;
import com.example.workmanagement.domain.task.domain.model.TaskPriority;
import com.example.workmanagement.domain.task.domain.model.TaskStatus;
import com.example.workmanagement.domain.task.service.result.TaskDetailResult;
import com.example.workmanagement.domain.task.service.result.TaskSummaryResult;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class TaskRepositoryDataJpaTest {

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void findDetailById_shouldReturnProjectedDetail() {
        taskRepository.save(Task.createNew(
                10L,
                101L,
                "업무 제목",
                "업무 설명",
                LocalDate.of(2026, 3, 17),
                LocalDate.of(2026, 3, 20),
                TaskPriority.HIGH
        ));

        Long taskId = taskRepository.findSummaryByProjectId(10L, PageRequest.of(0, 1))
                .getContent()
                .getFirst()
                .taskId();

        TaskDetailResult detail = taskRepository.findDetailById(taskId).orElseThrow();

        assertEquals(taskId, detail.taskId());
        assertEquals(10L, detail.projectId());
        assertEquals(101L, detail.authorId());
        assertEquals("업무 제목", detail.title());
        assertEquals("업무 설명", detail.description());
        assertEquals(TaskStatus.PENDING, detail.status());
        assertEquals(TaskPriority.HIGH, detail.priority());
    }

    @Test
    void findSummaryByProjectId_shouldReturnOnlyProjectTasks() {
        taskRepository.save(Task.createNew(10L, 101L, "업무 A", null, null, null, TaskPriority.HIGH));
        taskRepository.save(Task.createNew(10L, 102L, "업무 B", null, null, null, TaskPriority.MEDIUM));
        taskRepository.save(Task.createNew(20L, 103L, "업무 C", null, null, null, TaskPriority.LOW));

        Page<TaskSummaryResult> page = taskRepository.findSummaryByProjectId(10L, PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
        assertTrue(page.getContent().stream().allMatch(summary -> summary.projectId().equals(10L)));
    }

    @Test
    void findSummaryByProjectIdAndStatusIn_shouldFilterByStatusAndProject() {
        taskRepository.save(Task.createNew(10L, 101L, "업무 A", null, null, null, TaskPriority.HIGH));
        taskRepository.save(Task.createNew(10L, 102L, "업무 B", null, null, null, TaskPriority.MEDIUM));
        taskRepository.save(Task.createNew(20L, 103L, "업무 C", null, null, null, TaskPriority.LOW));

        Page<TaskSummaryResult> pendingPage = taskRepository.findSummaryByProjectIdAndStatusIn(
                10L,
                List.of(TaskStatus.PENDING),
                PageRequest.of(0, 10)
        );

        Page<TaskSummaryResult> inReviewPage = taskRepository.findSummaryByProjectIdAndStatusIn(
                10L,
                List.of(TaskStatus.IN_REVIEW),
                PageRequest.of(0, 10)
        );

        assertEquals(2, pendingPage.getTotalElements());
        assertTrue(pendingPage.getContent().stream().allMatch(summary -> summary.projectId().equals(10L)));
        assertEquals(0, inReviewPage.getTotalElements());
    }
}
