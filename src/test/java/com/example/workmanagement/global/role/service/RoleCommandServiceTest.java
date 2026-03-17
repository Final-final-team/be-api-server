package com.example.workmanagement.global.role.service;

import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.error.ProjectDomainException;
import com.example.workmanagement.domain.project.error.ProjectErrorCode;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.global.role.entity.ProjectMemberRole;
import com.example.workmanagement.global.role.entity.Role;
import com.example.workmanagement.global.role.entity.RoleAuditActionType;
import com.example.workmanagement.global.role.entity.RoleAuditLog;
import com.example.workmanagement.global.role.error.RoleDomainException;
import com.example.workmanagement.global.role.error.RoleErrorCode;
import com.example.workmanagement.global.role.repository.ProjectMemberRoleRepository;
import com.example.workmanagement.global.role.repository.RoleAuditLogRepository;
import com.example.workmanagement.global.role.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoleCommandServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ProjectMemberRoleRepository projectMemberRoleRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private RoleAuditLogRepository roleAuditLogRepository;

    @InjectMocks
    private RoleCommandService roleCommandService;

    private static final Long PROJECT_ID = 1L;
    private static final Long ACTOR_PM_ID = 100L;
    private static final Long TARGET_PM_ID = 200L;
    private static final Long ROLE_ID = 10L;

    @Nested
    @DisplayName("assignRole 테스트")
    class AssignRoleTest {

        // policy: ROLE_NOT_FOUND
        @Test
        @DisplayName("존재하지 않는 Role이면 예외 발생")
        void assignRole_roleNotFound_throwsException() {
            // given
            given(roleRepository.findById(ROLE_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> roleCommandService.assignRole(PROJECT_ID, ACTOR_PM_ID, TARGET_PM_ID, ROLE_ID))
                    .isInstanceOf(RoleDomainException.class)
                    .extracting(e -> ((RoleDomainException) e).errorCode())
                    .isEqualTo(RoleErrorCode.ROLE_NOT_FOUND);
        }

        // policy: PROJECT_MEMBER_NOT_FOUND
        @Test
        @DisplayName("존재하지 않는 Member면 예외 발생")
        void assignRole_memberNotFound_throwsException() {
            // given
            Role role = createCustomRole(PROJECT_ID, false, false);
            given(roleRepository.findById(ROLE_ID)).willReturn(Optional.of(role));
            given(projectMemberRepository.findById(TARGET_PM_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> roleCommandService.assignRole(PROJECT_ID, ACTOR_PM_ID, TARGET_PM_ID, ROLE_ID))
                    .isInstanceOf(ProjectDomainException.class)
                    .extracting(e -> ((ProjectDomainException) e).errorCode())
                    .isEqualTo(ProjectErrorCode.PROJECT_MEMBER_NOT_FOUND);
        }

        // policy: ROL-P-02 (시스템 Role 수정 불가)
        @Test
        @DisplayName("시스템 Role 부여 시 예외 발생")
        void assignRole_systemRole_throwsSystemRoleImmutable() {
            // given
            Role systemRole = createSystemRole(PROJECT_ID);
            ProjectMember member = createActiveMember(PROJECT_ID);
            
            given(roleRepository.findById(ROLE_ID)).willReturn(Optional.of(systemRole));
            given(projectMemberRepository.findById(TARGET_PM_ID)).willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> roleCommandService.assignRole(PROJECT_ID, ACTOR_PM_ID, TARGET_PM_ID, ROLE_ID))
                    .isInstanceOf(RoleDomainException.class)
                    .extracting(e -> ((RoleDomainException) e).errorCode())
                    .isEqualTo(RoleErrorCode.SYSTEM_ROLE_IMMUTABLE);
        }

        // policy: PJM-P-05 (비활성 멤버 권한 차단)
        @Test
        @DisplayName("비활성 멤버에게 Role 부여 시 예외 발생")
        void assignRole_inactiveMember_throwsInactiveMemberNotAllowed() {
            // given
            Role role = createCustomRole(PROJECT_ID, false, false);
            ProjectMember inactiveMember = createInactiveMember(PROJECT_ID);
            
            given(roleRepository.findById(ROLE_ID)).willReturn(Optional.of(role));
            given(projectMemberRepository.findById(TARGET_PM_ID)).willReturn(Optional.of(inactiveMember));

            // when & then
            assertThatThrownBy(() -> roleCommandService.assignRole(PROJECT_ID, ACTOR_PM_ID, TARGET_PM_ID, ROLE_ID))
                    .isInstanceOf(RoleDomainException.class)
                    .extracting(e -> ((RoleDomainException) e).errorCode())
                    .isEqualTo(RoleErrorCode.INACTIVE_MEMBER_ROLE_ASSIGN_NOT_ALLOWED);
        }
    }

    @Nested
    @DisplayName("revokeRole 테스트")
    class RevokeRoleTest {

        // policy: ROLE_NOT_ASSIGNED
        @Test
        @DisplayName("부여되지 않은 Role 회수 시 예외 발생")
        void revokeRole_roleNotAssigned_throwsException() {
            // given
            Role role = createCustomRole(PROJECT_ID, false, false);
            given(roleRepository.findById(ROLE_ID)).willReturn(Optional.of(role));
            given(projectMemberRoleRepository.findByProjectMemberIdAndRoleIdAndRevokedAtIsNull(TARGET_PM_ID, ROLE_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> roleCommandService.revokeRole(PROJECT_ID, ACTOR_PM_ID, TARGET_PM_ID, ROLE_ID))
                    .isInstanceOf(RoleDomainException.class)
                    .extracting(e -> ((RoleDomainException) e).errorCode())
                    .isEqualTo(RoleErrorCode.ROLE_NOT_ASSIGNED);
        }

        // policy: ROL-P-05 (시스템 Role 삭제 불가)
        @Test
        @DisplayName("시스템 Role 회수 시 예외 발생")
        void revokeRole_systemRole_throwsSystemRoleImmutable() {
            // given
            Role systemRole = createSystemRole(PROJECT_ID);
            ProjectMemberRole memberRole = createMemberRole(TARGET_PM_ID, ROLE_ID);
            
            given(roleRepository.findById(ROLE_ID)).willReturn(Optional.of(systemRole));
            given(projectMemberRoleRepository.findByProjectMemberIdAndRoleIdAndRevokedAtIsNull(TARGET_PM_ID, ROLE_ID))
                    .willReturn(Optional.of(memberRole));

            // when & then
            assertThatThrownBy(() -> roleCommandService.revokeRole(PROJECT_ID, ACTOR_PM_ID, TARGET_PM_ID, ROLE_ID))
                    .isInstanceOf(RoleDomainException.class)
                    .extracting(e -> ((RoleDomainException) e).errorCode())
                    .isEqualTo(RoleErrorCode.SYSTEM_ROLE_IMMUTABLE);
        }

        // policy: ROL-P-06, PJM-P-06 (마지막 리더 역할 회수 불가)
        @Test
        @DisplayName("마지막 리더 Role 회수 시 예외 발생")
        void revokeRole_lastLeader_throwsLastLeaderCannotBeRemoved() {
            // given
            Role leaderRole = createLeaderRole(PROJECT_ID);
            ProjectMemberRole memberRole = createMemberRole(TARGET_PM_ID, ROLE_ID);
            
            given(roleRepository.findById(ROLE_ID)).willReturn(Optional.of(leaderRole));
            given(projectMemberRoleRepository.findByProjectMemberIdAndRoleIdAndRevokedAtIsNull(TARGET_PM_ID, ROLE_ID))
                    .willReturn(Optional.of(memberRole));
            given(roleRepository.findByProjectIdAndIsLeaderRoleTrueAndIsActiveTrue(PROJECT_ID))
                    .willReturn(List.of(leaderRole));
            given(projectMemberRoleRepository.countActiveLeadersByProjectId(anyLong(), any(), any()))
                    .willReturn(1L); // 마지막 리더

            // when & then
            assertThatThrownBy(() -> roleCommandService.revokeRole(PROJECT_ID, ACTOR_PM_ID, TARGET_PM_ID, ROLE_ID))
                    .isInstanceOf(RoleDomainException.class)
                    .extracting(e -> ((RoleDomainException) e).errorCode())
                    .isEqualTo(RoleErrorCode.LAST_LEADER_CANNOT_BE_REMOVED);
        }
    }

    @Nested
    @DisplayName("성공 케이스 테스트")
    class SuccessTest {

        @Test
        @DisplayName("assignRole 성공 시 ProjectMemberRole 생성")
        void assignRole_success_createsProjectMemberRole() {
            // given
            Role role = createCustomRole(PROJECT_ID, false, false);
            ProjectMember member = createActiveMember(PROJECT_ID);
            
            given(roleRepository.findById(ROLE_ID)).willReturn(Optional.of(role));
            given(projectMemberRepository.findById(TARGET_PM_ID)).willReturn(Optional.of(member));
            given(projectMemberRoleRepository.save(any(ProjectMemberRole.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            ProjectMemberRole result = roleCommandService.assignRole(PROJECT_ID, ACTOR_PM_ID, TARGET_PM_ID, ROLE_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getProjectMemberId()).isEqualTo(TARGET_PM_ID);
            assertThat(result.getRoleId()).isEqualTo(ROLE_ID);
            assertThat(result.getGrantedByPmId()).isEqualTo(ACTOR_PM_ID);
            verify(projectMemberRoleRepository).save(any(ProjectMemberRole.class));
        }

        @Test
        @DisplayName("assignRole 성공 시 ROLE_GRANTED 감사 로그 생성")
        void assignRole_success_createsAuditLog() {
            // given
            Role role = createCustomRole(PROJECT_ID, false, false);
            ProjectMember member = createActiveMember(PROJECT_ID);
            
            given(roleRepository.findById(ROLE_ID)).willReturn(Optional.of(role));
            given(projectMemberRepository.findById(TARGET_PM_ID)).willReturn(Optional.of(member));
            given(projectMemberRoleRepository.save(any(ProjectMemberRole.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            roleCommandService.assignRole(PROJECT_ID, ACTOR_PM_ID, TARGET_PM_ID, ROLE_ID);

            // then
            verify(roleAuditLogRepository).save(any(RoleAuditLog.class));
        }

        @Test
        @DisplayName("revokeRole 성공 시 revokedAt, revokedByPmId 설정")
        void revokeRole_success_updatesRevokedFields() {
            // given
            Role role = createCustomRole(PROJECT_ID, false, false);
            ProjectMemberRole memberRole = createMemberRole(TARGET_PM_ID, ROLE_ID);
            
            given(roleRepository.findById(ROLE_ID)).willReturn(Optional.of(role));
            given(projectMemberRoleRepository.findByProjectMemberIdAndRoleIdAndRevokedAtIsNull(TARGET_PM_ID, ROLE_ID))
                    .willReturn(Optional.of(memberRole));

            // when
            ProjectMemberRole result = roleCommandService.revokeRole(PROJECT_ID, ACTOR_PM_ID, TARGET_PM_ID, ROLE_ID);

            // then
            assertThat(result.getRevokedAt()).isNotNull();
            assertThat(result.getRevokedByPmId()).isEqualTo(ACTOR_PM_ID);
        }

        @Test
        @DisplayName("revokeRole 성공 시 ROLE_REVOKED 감사 로그 생성")
        void revokeRole_success_createsAuditLog() {
            // given
            Role role = createCustomRole(PROJECT_ID, false, false);
            ProjectMemberRole memberRole = createMemberRole(TARGET_PM_ID, ROLE_ID);
            
            given(roleRepository.findById(ROLE_ID)).willReturn(Optional.of(role));
            given(projectMemberRoleRepository.findByProjectMemberIdAndRoleIdAndRevokedAtIsNull(TARGET_PM_ID, ROLE_ID))
                    .willReturn(Optional.of(memberRole));

            // when
            roleCommandService.revokeRole(PROJECT_ID, ACTOR_PM_ID, TARGET_PM_ID, ROLE_ID);

            // then
            verify(roleAuditLogRepository).save(any(RoleAuditLog.class));
        }

        // policy: ROL-P-06, PJM-P-06 (리더 2명 이상이면 회수 가능)
        @Test
        @DisplayName("리더가 2명 이상이면 정상 회수")
        void revokeRole_withMultipleLeaders_success() {
            // given
            Role leaderRole = createLeaderRole(PROJECT_ID);
            ProjectMemberRole memberRole = createMemberRole(TARGET_PM_ID, ROLE_ID);
            
            given(roleRepository.findById(ROLE_ID)).willReturn(Optional.of(leaderRole));
            given(projectMemberRoleRepository.findByProjectMemberIdAndRoleIdAndRevokedAtIsNull(TARGET_PM_ID, ROLE_ID))
                    .willReturn(Optional.of(memberRole));
            given(roleRepository.findByProjectIdAndIsLeaderRoleTrueAndIsActiveTrue(PROJECT_ID))
                    .willReturn(List.of(leaderRole));
            given(projectMemberRoleRepository.countActiveLeadersByProjectId(anyLong(), any(), any()))
                    .willReturn(2L); // 리더 2명

            // when
            ProjectMemberRole result = roleCommandService.revokeRole(PROJECT_ID, ACTOR_PM_ID, TARGET_PM_ID, ROLE_ID);

            // then
            assertThat(result.getRevokedAt()).isNotNull();
            verify(roleAuditLogRepository).save(any(RoleAuditLog.class));
        }
    }

    // ============ Helper Methods ============

    private Role createCustomRole(Long projectId, boolean isSystem, boolean isLeader) {
        // Reflection으로 Role 생성 (테스트용)
        try {
            Role role = Role.customRole(projectId, "TEST", "Test Role", "desc", 0L, 0L, 0L, isLeader, 1L);
            setField(role, "id", ROLE_ID);
            if (isSystem) {
                setField(role, "isSystem", true);
            }
            return role;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Role createSystemRole(Long projectId) {
        return createCustomRole(projectId, true, false);
    }

    private Role createLeaderRole(Long projectId) {
        return createCustomRole(projectId, false, true);
    }

    private ProjectMember createActiveMember(Long projectId) {
        ProjectMember member = ProjectMember.builder()
                .projectId(projectId)
                .userId(1L)
                .build();
        setField(member, "id", TARGET_PM_ID);
        return member;
    }

    private ProjectMember createInactiveMember(Long projectId) {
        ProjectMember member = createActiveMember(projectId);
        setField(member, "status", ProjectMemberStatus.INACTIVE);
        return member;
    }

    private ProjectMemberRole createMemberRole(Long memberId, Long roleId) {
        ProjectMemberRole memberRole = ProjectMemberRole.assign(memberId, roleId, ACTOR_PM_ID);
        setField(memberRole, "id", 1L);
        return memberRole;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            // 상위 클래스에서 찾기
            try {
                var field = target.getClass().getSuperclass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to set field: " + fieldName, ex);
            }
        }
    }
}
