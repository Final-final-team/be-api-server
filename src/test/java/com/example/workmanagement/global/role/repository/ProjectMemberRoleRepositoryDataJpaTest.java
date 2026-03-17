package com.example.workmanagement.global.role.repository;

import com.example.workmanagement.global.config.JpaAuditingConfig;
import com.example.workmanagement.global.role.entity.ProjectMemberRole;
import com.example.workmanagement.global.role.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class ProjectMemberRoleRepositoryDataJpaTest {

    @Autowired
    private ProjectMemberRoleRepository projectMemberRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role activeRole;
    private Role inactiveRole;
    private ProjectMemberRole member1ActiveRole;
    private ProjectMemberRole member1RevokedRole;
    private ProjectMemberRole member2ActiveRole;

    @BeforeEach
    void setUp() {
        projectMemberRoleRepository.deleteAll();
        roleRepository.deleteAll();

        // Create active role
        activeRole = roleRepository.save(Role.customRole(
                1L, "DEVELOPER", "Developer", "Developer role",
                0L, 0L, 0L, false, 1L));

        // Create inactive role (need reflection to set isActive=false)
        Role tempInactive = Role.customRole(
                1L, "OLD_ROLE", "Old Role", "Old role",
                0L, 0L, 0L, false, 1L);
        setInactive(tempInactive);
        inactiveRole = roleRepository.save(tempInactive);

        // Create ProjectMemberRoles
        member1ActiveRole = projectMemberRoleRepository.save(
                ProjectMemberRole.assign(101L, activeRole.getId(), 1L));

        ProjectMemberRole tempRevoked = projectMemberRoleRepository.save(
                ProjectMemberRole.assign(101L, activeRole.getId(), 1L));
        tempRevoked.revoke(1L);
        member1RevokedRole = projectMemberRoleRepository.save(tempRevoked);

        member2ActiveRole = projectMemberRoleRepository.save(
                ProjectMemberRole.assign(102L, activeRole.getId(), 1L));
    }

    // policy: ROL-P-03 (권한 합집합 계산), PJM-P-05 (멤버별 역할 조회)
    @Test
    void findByProjectMemberIdInAndRevokedAtIsNull_returnsActiveRolesForMultipleMembers() {
        List<ProjectMemberRole> results = projectMemberRoleRepository
                .findByProjectMemberIdInAndRevokedAtIsNull(List.of(101L, 102L));

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(ProjectMemberRole::isActive));
        assertTrue(results.contains(member1ActiveRole));
        assertTrue(results.contains(member2ActiveRole));
    }

    // policy: ROL-P-03 (권한 합집합 계산), PJM-P-05 (멤버별 역할 조회)
    @Test
    void findByProjectMemberIdInAndRevokedAtIsNull_excludesRevokedRoles() {
        List<ProjectMemberRole> results = projectMemberRoleRepository
                .findByProjectMemberIdInAndRevokedAtIsNull(List.of(101L));

        assertEquals(1, results.size());
        assertEquals(member1ActiveRole.getId(), results.get(0).getId());
        assertFalse(results.contains(member1RevokedRole));
    }

    // policy: ROL-P-03 (권한 합집합 계산), PJM-P-05 (멤버별 역할 조회)
    @Test
    void findByProjectMemberIdInAndRevokedAtIsNull_emptyCollectionReturnsEmpty() {
        List<ProjectMemberRole> results = projectMemberRoleRepository
                .findByProjectMemberIdInAndRevokedAtIsNull(List.of());

        assertTrue(results.isEmpty());
    }

    @Test
    void findByProjectMemberIdAndRevokedAtIsNull_returnsActiveRolesForSingleMember() {
        List<ProjectMemberRole> results = projectMemberRoleRepository
                .findByProjectMemberIdAndRevokedAtIsNull(101L);

        assertEquals(1, results.size());
        assertEquals(member1ActiveRole.getId(), results.get(0).getId());
    }

    // policy: ROL-P-03 (권한 합집합 계산)
    @Test
    void findByIdInAndIsActiveTrue_returnsOnlyActiveRoles() {
        List<Role> results = roleRepository
                .findByIdInAndIsActiveTrue(List.of(activeRole.getId(), inactiveRole.getId()));

        assertEquals(1, results.size());
        assertEquals(activeRole.getId(), results.get(0).getId());
        assertTrue(results.get(0).getIsActive());
    }

    private void setInactive(Role role) {
        try {
            var field = Role.class.getDeclaredField("isActive");
            field.setAccessible(true);
            field.set(role, false);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set isActive", e);
        }
    }
}
