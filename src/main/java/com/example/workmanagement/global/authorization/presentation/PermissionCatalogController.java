package com.example.workmanagement.global.authorization.presentation;

import com.example.workmanagement.global.authorization.permission.ProjectPermission;
import com.example.workmanagement.global.authorization.permission.ReviewPermission;
import com.example.workmanagement.global.authorization.permission.TaskPermission;
import com.example.workmanagement.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permissions")
@Tag(name = "권한 카탈로그", description = "프론트 표시용 권한 카탈로그 API")
public class PermissionCatalogController {
    // 정책 통합 정리본 반영:
    // 권한 이름은 도메인별 permission key를 단일 카탈로그로 노출하고,
    // 프론트는 Role 이름이 아니라 permission 목록을 기준으로 화면을 구성한다.

    @GetMapping("/catalog")
    @Operation(summary = "권한 카탈로그 조회", description = "프로젝트/업무/검토 권한 정의를 화면 표시용 메타데이터와 함께 조회합니다.")
    public ResponseEntity<ApiResponse<List<PermissionDefinitionResult>>> getCatalog() {
        List<PermissionDefinitionResult> results = new ArrayList<>();

        results.addAll(List.of(
                new PermissionDefinitionResult(ProjectPermission.PROJECT_MANAGE.name(), "프로젝트 관리", "프로젝트", "프로젝트 기본 설정과 운영 정책을 관리합니다."),
                new PermissionDefinitionResult(ProjectPermission.INVITE.name(), "멤버 초대", "프로젝트", "프로젝트 멤버 초대 흐름을 관리합니다."),
                new PermissionDefinitionResult(ProjectPermission.REMOVE.name(), "멤버 제거", "프로젝트", "프로젝트 멤버를 제거합니다."),
                new PermissionDefinitionResult(ProjectPermission.MILESTONE_MANAGE.name(), "마일스톤 관리", "프로젝트", "마일스톤 생성과 수정, 정렬을 관리합니다."),
                new PermissionDefinitionResult(ProjectPermission.ROLE_MANAGE.name(), "역할 관리", "프로젝트", "역할 생성, 수정, 부여 정책을 관리합니다.")
        ));

        results.addAll(List.of(
                new PermissionDefinitionResult(TaskPermission.TASK_CREATE.name(), "업무 생성", "업무", "새 업무를 생성합니다."),
                new PermissionDefinitionResult(TaskPermission.TASK_ASSIGN.name(), "업무 할당", "업무", "업무 담당자를 지정하거나 해제합니다."),
                new PermissionDefinitionResult(TaskPermission.TASK_OVERWRITE.name(), "업무 수정", "업무", "업무 핵심 정보를 수정합니다."),
                new PermissionDefinitionResult(TaskPermission.TASK_COMMENT_MANAGE.name(), "업무 코멘트 관리", "업무", "업무 코멘트를 관리합니다."),
                new PermissionDefinitionResult(TaskPermission.TASK_DELETE.name(), "업무 삭제", "업무", "업무를 삭제합니다."),
                new PermissionDefinitionResult(TaskPermission.TASK_FORCE_DELETE.name(), "업무 강제 삭제", "업무", "정책 우회가 필요한 업무 삭제를 수행합니다."),
                new PermissionDefinitionResult(TaskPermission.TASK_FORCE_COMPLETE.name(), "업무 강제 완료", "업무", "검토 중 업무를 강제로 완료합니다.")
        ));

        results.addAll(List.of(
                new PermissionDefinitionResult(ReviewPermission.REVIEW_VIEW.name(), "검토 조회", "검토", "검토 상세와 히스토리를 조회합니다."),
                new PermissionDefinitionResult(ReviewPermission.REVIEW_DECIDE.name(), "검토 승인/반려", "검토", "검토를 승인하거나 반려합니다."),
                new PermissionDefinitionResult(ReviewPermission.REVIEW_ADMIN_OVERWRITE.name(), "검토 관리자 수정", "검토", "관리자 권한으로 검토를 수정하거나 정정합니다."),
                new PermissionDefinitionResult(ReviewPermission.REVIEW_COMMENT_CREATE.name(), "검토 코멘트 작성", "검토", "검토 코멘트를 작성합니다.")
        ));

        return ResponseEntity.ok(ApiResponse.success(List.copyOf(results)));
    }

    public record PermissionDefinitionResult(
            String key,
            String name,
            String category,
            String description
    ) {
    }
}
