package com.example.workmanagement.global.role.presentation;

import com.example.workmanagement.global.response.ApiResponse;
import com.example.workmanagement.global.role.entity.Role;
import com.example.workmanagement.global.role.presentation.dto.RoleResponse;
import com.example.workmanagement.global.role.presentation.dto.RoleUpdateRequest;
import com.example.workmanagement.global.role.service.RoleManagementService;
import com.example.workmanagement.global.security.resolver.AuthenticatedUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/roles")
@Tag(name = "역할 관리", description = "커스텀 역할 CRUD API")
public class RoleManagementController {

    private final RoleManagementService roleManagementService;

    public RoleManagementController(RoleManagementService roleManagementService) {
        this.roleManagementService = roleManagementService;
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "커스텀 역할 수정", description = "프로젝트의 커스텀 역할을 수정합니다.")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable
            Long projectId,

            @Parameter(description = "역할 ID", example = "5")
            @PathVariable
            Long roleId,

            @Valid
            @RequestBody
            RoleUpdateRequest request
    ) {
        // policy: ROL-P-02, ROL-P-07
        Role updatedRole = roleManagementService.updateRole(request.toCommand(projectId, roleId, actorId));
        return ResponseEntity.ok(ApiResponse.success(RoleResponse.from(updatedRole)));
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "커스텀 역할 삭제", description = "프로젝트의 커스텀 역할을 논리 삭제합니다.")
    public ResponseEntity<Void> deleteRole(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable
            Long projectId,

            @Parameter(description = "역할 ID", example = "5")
            @PathVariable
            Long roleId
    ) {
        // policy: ROL-P-02, ROL-P-05, ROL-P-07
        roleManagementService.deleteRole(new com.example.workmanagement.global.role.service.command.RoleDeleteCommand(
                projectId,
                roleId,
                actorId
        ));
        return ResponseEntity.noContent().build();
    }
}
