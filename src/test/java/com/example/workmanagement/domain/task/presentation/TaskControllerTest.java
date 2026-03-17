package com.example.workmanagement.domain.task.presentation;

import com.example.workmanagement.domain.task.domain.model.TaskPriority;
import com.example.workmanagement.domain.task.domain.model.TaskStatus;
import com.example.workmanagement.domain.task.service.TaskQueryService;
import com.example.workmanagement.domain.task.service.result.TaskDetailResult;
import com.example.workmanagement.domain.task.service.result.TaskPageResult;
import com.example.workmanagement.domain.task.service.result.TaskSummaryResult;
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
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskQueryService taskQueryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TaskController controller = new TaskController(taskQueryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new AuthenticatedUserIdArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getTask_shouldResolveActorAndReturnDetail() throws Exception {
        authenticateAs(101L);
        TaskDetailResult detailResult = createTaskDetail(100L, 10L);

        when(taskQueryService.findTask(10L, 100L, 101L)).thenReturn(detailResult);

        mockMvc.perform(get("/api/v1/projects/{projectId}/tasks/{taskId}", 10L, 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(100))
                .andExpect(jsonPath("$.data.projectId").value(10))
                .andExpect(jsonPath("$.errorInfo").doesNotExist());

        verify(taskQueryService).findTask(10L, 100L, 101L);
    }

    @Test
    void getTasks_withoutParams_shouldApplyPageableDefault() throws Exception {
        authenticateAs(101L);
        TaskPageResult<TaskSummaryResult> pageResult = new TaskPageResult<>(
                List.of(createTaskSummary(100L, 10L)),
                0,
                20,
                1,
                1,
                false,
                false
        );

        when(taskQueryService.findTasks(eq(10L), eq(101L), isNull(), any(Pageable.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/projects/{projectId}/tasks", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.items[0].taskId").value(100));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(taskQueryService).findTasks(eq(10L), eq(101L), isNull(), pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(20, pageableCaptor.getValue().getPageSize());
        assertEquals("createdAt: DESC", pageableCaptor.getValue().getSort().toString());
    }

    @Test
    void getTasks_withStatusesAndPagingParams_shouldBindRequestValues() throws Exception {
        authenticateAs(101L);
        List<TaskStatus> statuses = List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS);
        TaskPageResult<TaskSummaryResult> pageResult = new TaskPageResult<>(
                List.of(createTaskSummary(100L, 10L)),
                1,
                30,
                1,
                1,
                false,
                true
        );

        when(taskQueryService.findTasks(eq(10L), eq(101L), eq(statuses), any(Pageable.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/projects/{projectId}/tasks", 10L)
                        .param("statuses", "PENDING", "IN_PROGRESS")
                        .param("page", "1")
                        .param("size", "30")
                        .param("sort", "dueDate,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(30));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(taskQueryService).findTasks(eq(10L), eq(101L), eq(statuses), pageableCaptor.capture());
        assertEquals(1, pageableCaptor.getValue().getPageNumber());
        assertEquals(30, pageableCaptor.getValue().getPageSize());
        assertEquals("dueDate: ASC", pageableCaptor.getValue().getSort().toString());
    }

    @Test
    void getTask_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/projects/{projectId}/tasks/{taskId}", 10L, 100L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorInfo.code").value("USER_UNAUTHENTICATED"));

        verifyNoInteractions(taskQueryService);
    }

    private void authenticateAs(Long userId) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(userId.toString(), "N/A", List.of()));
        SecurityContextHolder.setContext(context);
    }

    private TaskDetailResult createTaskDetail(Long taskId, Long projectId) {
        return new TaskDetailResult(
                taskId,
                projectId,
                101L,
                "업무 제목",
                "업무 설명",
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH,
                LocalDate.of(2026, 3, 17),
                LocalDate.of(2026, 3, 20),
                Instant.parse("2026-03-17T05:00:00Z"),
                Instant.parse("2026-03-17T06:00:00Z")
        );
    }

    private TaskSummaryResult createTaskSummary(Long taskId, Long projectId) {
        return new TaskSummaryResult(
                taskId,
                projectId,
                "업무 제목",
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH,
                LocalDate.of(2026, 3, 17),
                LocalDate.of(2026, 3, 20),
                101L,
                Instant.parse("2026-03-17T05:00:00Z"),
                Instant.parse("2026-03-17T06:00:00Z")
        );
    }
}
