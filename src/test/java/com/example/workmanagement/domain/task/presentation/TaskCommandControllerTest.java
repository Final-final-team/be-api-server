package com.example.workmanagement.domain.task.presentation;

import com.example.workmanagement.domain.task.domain.model.TaskPriority;
import com.example.workmanagement.domain.task.domain.model.TaskStatus;
import com.example.workmanagement.domain.task.service.TaskCommandService;
import com.example.workmanagement.domain.task.service.command.TaskAssignCommand;
import com.example.workmanagement.domain.task.service.command.TaskAssignMeCommand;
import com.example.workmanagement.domain.task.service.command.TaskCancelStartCommand;
import com.example.workmanagement.domain.task.service.command.TaskCreateCommand;
import com.example.workmanagement.domain.task.service.command.TaskForceCompleteCommand;
import com.example.workmanagement.domain.task.service.command.TaskStartCommand;
import com.example.workmanagement.domain.task.service.command.TaskUnassignCommand;
import com.example.workmanagement.domain.task.service.command.TaskUnassignMeCommand;
import com.example.workmanagement.domain.task.service.command.TaskUpdateDescriptionCommand;
import com.example.workmanagement.domain.task.service.command.TaskUpdateDueDateCommand;
import com.example.workmanagement.domain.task.service.command.TaskUpdatePriorityCommand;
import com.example.workmanagement.domain.task.service.command.TaskUpdateStartDateCommand;
import com.example.workmanagement.domain.task.service.command.TaskUpdateTitleCommand;
import com.example.workmanagement.domain.task.service.result.TaskDetailResult;
import com.example.workmanagement.global.error.GlobalExceptionHandler;
import com.example.workmanagement.global.security.resolver.AuthenticatedUserIdArgumentResolver;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskCommandControllerTest {

    @Mock
    private TaskCommandService taskCommandService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TaskCommandController controller = new TaskCommandController(taskCommandService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticatedUserIdArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTask_shouldResolveActorAndReturnCreated() throws Exception {
        authenticateAs(101L);
        TaskDetailResult detailResult = createTaskDetail(100L, 10L, TaskStatus.PENDING);

        when(taskCommandService.createTask(any(TaskCreateCommand.class))).thenReturn(detailResult);

        mockMvc.perform(post("/api/projects/{projectId}/tasks", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "업무 제목",
                                  "description": "업무 설명",
                                  "startDate": "2026-03-17",
                                  "dueDate": "2026-03-20",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.taskId").value(100))
                .andExpect(jsonPath("$.data.projectId").value(10));

        ArgumentCaptor<TaskCreateCommand> captor = ArgumentCaptor.forClass(TaskCreateCommand.class);
        verify(taskCommandService).createTask(captor.capture());
        assertEquals(10L, captor.getValue().projectId());
        assertEquals(101L, captor.getValue().actorId());
        assertEquals("업무 제목", captor.getValue().title());
    }

    @Test
    void assignTask_shouldMapRequestAndReturnOk() throws Exception {
        authenticateAs(101L);
        TaskDetailResult detailResult = createTaskDetail(100L, 10L, TaskStatus.PENDING);

        when(taskCommandService.assignTask(any(TaskAssignCommand.class))).thenReturn(detailResult);

        mockMvc.perform(post("/api/projects/{projectId}/tasks/{taskId}/assign", 10L, 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 102
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(100));

        ArgumentCaptor<TaskAssignCommand> captor = ArgumentCaptor.forClass(TaskAssignCommand.class);
        verify(taskCommandService).assignTask(captor.capture());
        assertEquals(10L, captor.getValue().projectId());
        assertEquals(100L, captor.getValue().taskId());
        assertEquals(101L, captor.getValue().actorId());
        assertEquals(102L, captor.getValue().userId());
    }

    @Test
    void assignMe_shouldMapPathAndActorAndReturnOk() throws Exception {
        authenticateAs(101L);
        TaskDetailResult detailResult = createTaskDetail(100L, 10L, TaskStatus.PENDING);

        when(taskCommandService.assignMe(any(TaskAssignMeCommand.class))).thenReturn(detailResult);

        mockMvc.perform(post("/api/projects/{projectId}/tasks/{taskId}/assign/me", 10L, 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(100));

        ArgumentCaptor<TaskAssignMeCommand> captor = ArgumentCaptor.forClass(TaskAssignMeCommand.class);
        verify(taskCommandService).assignMe(captor.capture());
        assertEquals(10L, captor.getValue().projectId());
        assertEquals(100L, captor.getValue().taskId());
        assertEquals(101L, captor.getValue().actorId());
    }

    @Test
    void unassignTask_shouldMapRequestAndReturnOk() throws Exception {
        authenticateAs(101L);
        TaskDetailResult detailResult = createTaskDetail(100L, 10L, TaskStatus.PENDING);

        when(taskCommandService.unassignTask(any(TaskUnassignCommand.class))).thenReturn(detailResult);

        mockMvc.perform(post("/api/projects/{projectId}/tasks/{taskId}/unassign", 10L, 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 102
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(100));

        ArgumentCaptor<TaskUnassignCommand> captor = ArgumentCaptor.forClass(TaskUnassignCommand.class);
        verify(taskCommandService).unassignTask(captor.capture());
        assertEquals(10L, captor.getValue().projectId());
        assertEquals(100L, captor.getValue().taskId());
        assertEquals(101L, captor.getValue().actorId());
        assertEquals(102L, captor.getValue().userId());
    }

    @Test
    void unassignMe_shouldMapPathAndActorAndReturnOk() throws Exception {
        authenticateAs(101L);
        TaskDetailResult detailResult = createTaskDetail(100L, 10L, TaskStatus.PENDING);

        when(taskCommandService.unassignMe(any(TaskUnassignMeCommand.class))).thenReturn(detailResult);

        mockMvc.perform(post("/api/projects/{projectId}/tasks/{taskId}/unassign/me", 10L, 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(100));

        ArgumentCaptor<TaskUnassignMeCommand> captor = ArgumentCaptor.forClass(TaskUnassignMeCommand.class);
        verify(taskCommandService).unassignMe(captor.capture());
        assertEquals(10L, captor.getValue().projectId());
        assertEquals(100L, captor.getValue().taskId());
        assertEquals(101L, captor.getValue().actorId());
    }

    @Test
    void updateTaskTitle_shouldMapRequestAndReturnOk() throws Exception {
        authenticateAs(101L);
        TaskDetailResult detailResult = createTaskDetail(100L, 10L, TaskStatus.PENDING);

        when(taskCommandService.updateTitle(any(TaskUpdateTitleCommand.class))).thenReturn(detailResult);

        mockMvc.perform(patch("/api/projects/{projectId}/tasks/{taskId}/title", 10L, 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "수정 제목"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(100));

        verify(taskCommandService).updateTitle(any(TaskUpdateTitleCommand.class));
    }

    @Test
    void updateTaskDescription_shouldMapRequestAndReturnOk() throws Exception {
        authenticateAs(101L);
        TaskDetailResult detailResult = createTaskDetail(100L, 10L, TaskStatus.PENDING);

        when(taskCommandService.updateDescription(any(TaskUpdateDescriptionCommand.class))).thenReturn(detailResult);

        mockMvc.perform(patch("/api/projects/{projectId}/tasks/{taskId}/description", 10L, 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "수정 설명"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(100));

        verify(taskCommandService).updateDescription(any(TaskUpdateDescriptionCommand.class));
    }

    @Test
    void updateTaskStartDate_shouldMapRequestAndReturnOk() throws Exception {
        authenticateAs(101L);
        TaskDetailResult detailResult = createTaskDetail(100L, 10L, TaskStatus.PENDING);

        when(taskCommandService.updateStartDate(any(TaskUpdateStartDateCommand.class))).thenReturn(detailResult);

        mockMvc.perform(patch("/api/projects/{projectId}/tasks/{taskId}/start-date", 10L, 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startDate": "2026-03-21"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(100));

        verify(taskCommandService).updateStartDate(any(TaskUpdateStartDateCommand.class));
    }

    @Test
    void updateTaskDueDate_shouldMapRequestAndReturnOk() throws Exception {
        authenticateAs(101L);
        TaskDetailResult detailResult = createTaskDetail(100L, 10L, TaskStatus.PENDING);

        when(taskCommandService.updateDueDate(any(TaskUpdateDueDateCommand.class))).thenReturn(detailResult);

        mockMvc.perform(patch("/api/projects/{projectId}/tasks/{taskId}/due-date", 10L, 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dueDate": "2026-03-23"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(100));

        verify(taskCommandService).updateDueDate(any(TaskUpdateDueDateCommand.class));
    }

    @Test
    void updateTaskPriority_shouldMapRequestAndReturnOk() throws Exception {
        authenticateAs(101L);
        TaskDetailResult detailResult = createTaskDetail(100L, 10L, TaskStatus.PENDING);

        when(taskCommandService.updatePriority(any(TaskUpdatePriorityCommand.class))).thenReturn(detailResult);

        mockMvc.perform(patch("/api/projects/{projectId}/tasks/{taskId}/priority", 10L, 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "priority": "LOW"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(100));

        verify(taskCommandService).updatePriority(any(TaskUpdatePriorityCommand.class));
    }

    @Test
    void startTask_shouldMapPathAndActorAndReturnOk() throws Exception {
        authenticateAs(101L);
        TaskDetailResult detailResult = createTaskDetail(100L, 10L, TaskStatus.IN_PROGRESS);

        when(taskCommandService.startTask(any(TaskStartCommand.class))).thenReturn(detailResult);

        mockMvc.perform(post("/api/projects/{projectId}/tasks/{taskId}/start", 10L, 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        ArgumentCaptor<TaskStartCommand> captor = ArgumentCaptor.forClass(TaskStartCommand.class);
        verify(taskCommandService).startTask(captor.capture());
        assertEquals(10L, captor.getValue().projectId());
        assertEquals(100L, captor.getValue().taskId());
        assertEquals(101L, captor.getValue().actorId());
    }

    @Test
    void forceCompleteTask_shouldMapPathAndActorAndReturnOk() throws Exception {
        authenticateAs(101L);
        TaskDetailResult detailResult = createTaskDetail(100L, 10L, TaskStatus.COMPLETED);

        when(taskCommandService.forceCompleteTask(any(TaskForceCompleteCommand.class))).thenReturn(detailResult);

        mockMvc.perform(post("/api/projects/{projectId}/tasks/{taskId}/force-complete", 10L, 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        ArgumentCaptor<TaskForceCompleteCommand> captor = ArgumentCaptor.forClass(TaskForceCompleteCommand.class);
        verify(taskCommandService).forceCompleteTask(captor.capture());
        assertEquals(10L, captor.getValue().projectId());
        assertEquals(100L, captor.getValue().taskId());
        assertEquals(101L, captor.getValue().actorId());
    }

    @Test
    void cancelStartTask_shouldMapPathAndActorAndReturnOk() throws Exception {
        authenticateAs(101L);
        TaskDetailResult detailResult = createTaskDetail(100L, 10L, TaskStatus.PENDING);

        when(taskCommandService.cancelStartTask(any(TaskCancelStartCommand.class))).thenReturn(detailResult);

        mockMvc.perform(post("/api/projects/{projectId}/tasks/{taskId}/cancel-start", 10L, 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        ArgumentCaptor<TaskCancelStartCommand> captor = ArgumentCaptor.forClass(TaskCancelStartCommand.class);
        verify(taskCommandService).cancelStartTask(captor.capture());
        assertEquals(10L, captor.getValue().projectId());
        assertEquals(100L, captor.getValue().taskId());
        assertEquals(101L, captor.getValue().actorId());
    }

    @Test
    void assignTask_withInvalidBody_shouldReturnBadRequest() throws Exception {
        authenticateAs(101L);

        mockMvc.perform(post("/api/projects/{projectId}/tasks/{taskId}/assign", 10L, 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorInfo.code").value("INVALID_ARGUMENT"));

        verifyNoInteractions(taskCommandService);
    }

    @Test
    void createTask_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/projects/{projectId}/tasks", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "업무 제목"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorInfo.code").value("USER_UNAUTHENTICATED"));

        verifyNoInteractions(taskCommandService);
    }

    private void authenticateAs(Long userId) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(userId.toString(), "N/A", List.of()));
        SecurityContextHolder.setContext(context);
    }

    private TaskDetailResult createTaskDetail(Long taskId, Long projectId, TaskStatus status) {
        return new TaskDetailResult(
                taskId,
                projectId,
                101L,
                "업무 제목",
                "업무 설명",
                status,
                TaskPriority.HIGH,
                LocalDate.of(2026, 3, 17),
                LocalDate.of(2026, 3, 20),
                Instant.parse("2026-03-17T05:00:00Z"),
                Instant.parse("2026-03-17T06:00:00Z")
        );
    }
}
