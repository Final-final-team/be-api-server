package com.example.workmanagement.domain.task.repository;

import com.example.workmanagement.domain.task.domain.model.TaskAssignee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class TaskAssigneeRepositoryDataJpaTest {

    @Autowired
    private TaskAssigneeRepository taskAssigneeRepository;

    @BeforeEach
    void setUp() {
        taskAssigneeRepository.deleteAll();
    }

    @Test
    void existsAndFindByTaskIdAndUserId_shouldReturnAssignee() {
        taskAssigneeRepository.saveAndFlush(TaskAssignee.assign(100L, 101L, 201L));

        boolean exists = taskAssigneeRepository.existsByTaskIdAndUserId(100L, 101L);

        assertTrue(exists);
        assertTrue(taskAssigneeRepository.findByTaskIdAndUserId(100L, 101L).isPresent());
    }

    @Test
    void countByTaskId_shouldCountOnlySameTask() {
        taskAssigneeRepository.saveAndFlush(TaskAssignee.assign(100L, 101L, 201L));
        taskAssigneeRepository.saveAndFlush(TaskAssignee.assign(100L, 102L, 201L));
        taskAssigneeRepository.saveAndFlush(TaskAssignee.assign(200L, 103L, 201L));

        long count = taskAssigneeRepository.countByTaskId(100L);

        assertEquals(2L, count);
    }

    @Test
    void saveDuplicateTaskAndUser_shouldThrowDataIntegrityViolationException() {
        taskAssigneeRepository.saveAndFlush(TaskAssignee.assign(100L, 101L, 201L));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> taskAssigneeRepository.saveAndFlush(TaskAssignee.assign(100L, 101L, 202L))
        );
    }
}
