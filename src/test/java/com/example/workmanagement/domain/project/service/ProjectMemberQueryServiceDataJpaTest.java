package com.example.workmanagement.domain.project.service;

import com.example.workmanagement.domain.project.entity.Project;
import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.project.repository.ProjectRepository;
import com.example.workmanagement.domain.project.service.result.ProjectMemberBasicResult;
import com.example.workmanagement.domain.project.service.result.ProjectMemberWithRolesResult;
import com.example.workmanagement.global.config.JpaAuditingConfig;
import com.example.workmanagement.global.role.entity.ProjectMemberRole;
import com.example.workmanagement.global.role.entity.Role;
import com.example.workmanagement.global.role.repository.ProjectMemberRoleRepository;
import com.example.workmanagement.global.role.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DataJpaTest
@Import({JpaAuditingConfig.class, ProjectMemberQueryService.class})
class ProjectMemberQueryServiceDataJpaTest {

    @Autowired
    private ProjectMemberQueryService projectMemberQueryService;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProjectMemberRoleRepository projectMemberRoleRepository;

    @MockitoSpyBean
    private ProjectMemberRepository projectMemberRepositorySpy;

    @MockitoSpyBean
    private RoleRepository roleRepositorySpy;

    @MockitoSpyBean
    private ProjectMemberRoleRepository projectMemberRoleRepositorySpy;

    private Long projectAId;
    private Long projectBId;
    private ProjectMember memberA1;
    private ProjectMember memberA2;
    private ProjectMember memberA3Inactive;
    private Role roleADeveloper;
    private Role roleAReviewer;
    private Role roleAInactive;
    private Role roleBExternal;

    @BeforeEach
    void setUp() {
        projectMemberRoleRepository.deleteAll();
        roleRepository.deleteAll();
        projectMemberRepository.deleteAll();
        projectRepository.deleteAll();

        Project projectA = projectRepository.save(Project.builder()
                .name("Project-A")
                .description("A")
                .imageUrl("a.png")
                .build());
        Project projectB = projectRepository.save(Project.builder()
                .name("Project-B")
                .description("B")
                .imageUrl("b.png")
                .build());
        projectAId = projectA.getId();
        projectBId = projectB.getId();

        memberA1 = projectMemberRepository.save(ProjectMember.builder().projectId(projectAId).userId(101L).build());
        memberA2 = projectMemberRepository.save(ProjectMember.builder().projectId(projectAId).userId(102L).build());
        memberA3Inactive = projectMemberRepository.save(ProjectMember.builder().projectId(projectAId).userId(103L).build());
        memberA3Inactive.FireMember();
        memberA3Inactive = projectMemberRepository.save(memberA3Inactive);

        roleADeveloper = roleRepository.save(Role.customRole(projectAId, "DEV", "Developer", "dev", 0L, 0L, 0L, false, memberA1.getId()));
        roleAReviewer = roleRepository.save(Role.customRole(projectAId, "REVIEWER", "Reviewer", "review", 0L, 0L, 0L, false, memberA1.getId()));
        roleAInactive = roleRepository.save(Role.customRole(projectAId, "OLD", "Old", "inactive", 0L, 0L, 0L, false, memberA1.getId()));
        setRoleInactive(roleAInactive);
        roleAInactive = roleRepository.save(roleAInactive);
        roleBExternal = roleRepository.save(Role.customRole(projectBId, "EXTERNAL", "External", "other project", 0L, 0L, 0L, false, memberA1.getId()));

        projectMemberRoleRepository.save(ProjectMemberRole.assign(memberA1.getId(), roleADeveloper.getId(), memberA1.getId()));
        projectMemberRoleRepository.save(ProjectMemberRole.assign(memberA2.getId(), roleAReviewer.getId(), memberA1.getId()));
        projectMemberRoleRepository.save(ProjectMemberRole.assign(memberA2.getId(), roleBExternal.getId(), memberA1.getId()));
        projectMemberRoleRepository.save(ProjectMemberRole.assign(memberA1.getId(), roleAInactive.getId(), memberA1.getId()));

        ProjectMemberRole revoked = projectMemberRoleRepository
                .save(ProjectMemberRole.assign(memberA1.getId(), roleAReviewer.getId(), memberA1.getId()));
        revoked.revoke(memberA1.getId());
        projectMemberRoleRepository.save(revoked);

        projectMemberRoleRepository.save(ProjectMemberRole.assign(memberA3Inactive.getId(), roleADeveloper.getId(), memberA1.getId()));
    }

    // policy: PJM-P-05 (비활성 멤버 제외)
    @Test
    void findActiveMemberResults_returnsOnlyActiveMembers() {
        List<ProjectMemberBasicResult> results = projectMemberQueryService.findActiveMemberResults(projectAId);

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(result -> result.status() == ProjectMemberStatus.ACTIVE));
        List<Long> userIds = results.stream().map(ProjectMemberBasicResult::userId).toList();
        assertTrue(userIds.containsAll(List.of(101L, 102L)));
        assertFalse(userIds.contains(103L));
    }

    // policy: ROL-P-03, ROL-P-04, PJM-P-05
    @Test
    void findActiveMemberWithRolesResults_assemblesOnlyActiveSameProjectRoles() {
        List<ProjectMemberWithRolesResult> results =
                projectMemberQueryService.findActiveMemberWithRolesResults(projectAId);

        assertEquals(2, results.size());

        Map<Long, List<String>> roleCodesByMemberId = results.stream()
                .collect(Collectors.toMap(
                        result -> result.member().projectMemberId(),
                        result -> result.roles().stream().map(role -> role.roleCode()).toList()
                ));

        assertEquals(List.of("DEV"), roleCodesByMemberId.get(memberA1.getId()));
        assertEquals(List.of("REVIEWER"), roleCodesByMemberId.get(memberA2.getId()));
        assertTrue(roleCodesByMemberId.values().stream().flatMap(List::stream).noneMatch("EXTERNAL"::equals));
        assertTrue(roleCodesByMemberId.values().stream().flatMap(List::stream).noneMatch("OLD"::equals));
    }

    // policy: ROL-P-03, ROL-P-04, PJM-P-05
    @Test
    void findActiveMemberWithRolesResults_bulkRead_usesBatchQueriesWithoutPerMemberLoopQueries() {
        for (int i = 0; i < 20; i++) {
            ProjectMember member = projectMemberRepository
                    .save(ProjectMember.builder().projectId(projectAId).userId(200L + i).build());
            projectMemberRoleRepository
                    .save(ProjectMemberRole.assign(member.getId(), roleADeveloper.getId(), memberA1.getId()));
        }

        reset(projectMemberRepositorySpy, roleRepositorySpy, projectMemberRoleRepositorySpy);

        List<ProjectMemberWithRolesResult> results =
                projectMemberQueryService.findActiveMemberWithRolesResults(projectAId);

        assertEquals(22, results.size());
        verify(projectMemberRepositorySpy, times(1)).findByProjectId(projectAId);
        verify(roleRepositorySpy, times(1)).findByProjectIdAndIsActiveTrue(projectAId);
        verify(projectMemberRoleRepositorySpy, times(1))
                .findActiveRoleLinksByProjectId(projectAId, ProjectMemberStatus.ACTIVE);
        verify(projectMemberRoleRepositorySpy, never()).findByProjectMemberIdAndRevokedAtIsNull(any());
        verify(projectMemberRoleRepositorySpy, never()).findByProjectMemberIdAndRevokedAtIsNull(anyLong());
    }

    private void setRoleInactive(Role role) {
        try {
            Field isActiveField = Role.class.getDeclaredField("isActive");
            isActiveField.setAccessible(true);
            isActiveField.set(role, false);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set role inactive", e);
        }
    }
}
