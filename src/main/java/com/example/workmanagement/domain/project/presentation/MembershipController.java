package com.example.workmanagement.domain.project.presentation;

import com.example.workmanagement.domain.project.presentation.dto.JoinProjectRequest;
import com.example.workmanagement.domain.project.presentation.dto.MembershipResponse;
import com.example.workmanagement.domain.project.error.ProjectDomainException;
import com.example.workmanagement.domain.project.error.ProjectErrorCode;
import com.example.workmanagement.domain.project.service.MembershipCommandService;
import com.example.workmanagement.domain.project.service.ProjectMemberQueryService;
import com.example.workmanagement.domain.project.service.command.JoinProjectCommand;
import com.example.workmanagement.domain.project.service.command.LeaveProjectCommand;
import com.example.workmanagement.domain.project.service.command.RemoveMemberCommand;
import com.example.workmanagement.global.response.ApiResponse;
import com.example.workmanagement.global.security.resolver.AuthenticatedUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@Tag(name = "프로젝트 멤버십", description = "프로젝트 멤버 입퇴장 관리 API")
public class MembershipController {

    private final MembershipCommandService membershipCommandService;
    private final ProjectMemberQueryService projectMemberQueryService;

    public MembershipController(
            MembershipCommandService membershipCommandService,
            ProjectMemberQueryService projectMemberQueryService
    ) {
        this.membershipCommandService = membershipCommandService;
        this.projectMemberQueryService = projectMemberQueryService;
    }

    @PostMapping
    @Operation(summary = "프로젝트에 멤버 초대/추가", description = "프로젝트에 사용자를 멤버로 추가합니다.")
    public ResponseEntity<ApiResponse<MembershipResponse>> joinProject(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable
            Long projectId,

            @Valid
            @RequestBody
            JoinProjectRequest request
    ) {
        membershipCommandService.join(
                new JoinProjectCommand(projectId, actorId, request.targetUserId())
        );

        var memberInfo = projectMemberQueryService.findActiveMemberResults(projectId)
                .stream()
                .filter(m -> m.userId().equals(request.targetUserId()))
                .findFirst()
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_NOT_FOUND));

        MembershipResponse response = MembershipResponse.fromMemberBasicResult(memberInfo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @DeleteMapping("/me")
    @Operation(summary = "프로젝트에서 탈퇴", description = "현재 사용자가 프로젝트에서 탈퇴합니다.")
    public ResponseEntity<Void> leaveProject(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable
            Long projectId
    ) {
        membershipCommandService.leave(
                new LeaveProjectCommand(projectId, actorId)
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{targetUserId}")
    @Operation(summary = "멤버 제거", description = "프로젝트에서 특정 멤버를 제거합니다. (관리자만 가능)")
    public ResponseEntity<Void> removeMember(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable
            Long projectId,

            @Parameter(description = "제거 대상 사용자 ID", example = "5")
            @PathVariable
            Long targetUserId
    ) {
        membershipCommandService.remove(
                new RemoveMemberCommand(projectId, actorId, targetUserId)
        );

        return ResponseEntity.noContent().build();
    }
}
