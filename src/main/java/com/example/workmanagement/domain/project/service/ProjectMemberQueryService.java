package com.example.workmanagement.domain.project.service;

import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.global.role.entity.ProjectMemberRole;
import com.example.workmanagement.global.role.entity.Role;
import com.example.workmanagement.global.role.repository.ProjectMemberRoleRepository;
import com.example.workmanagement.global.role.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProjectMemberQueryService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;
    private final RoleRepository roleRepository;

    public ProjectMemberQueryService(
            ProjectMemberRepository projectMemberRepository,
            ProjectMemberRoleRepository projectMemberRoleRepository,
            RoleRepository roleRepository
    ) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectMemberRoleRepository = projectMemberRoleRepository;
        this.roleRepository = roleRepository;
    }

    // policy: PJM-P-05 (LEFT/REMOVED 상태는 즉시 권한이 제거되므로 조회 대상에서 제외하고 ACTIVE 멤버만 반환)
    public List<ProjectMember> findActiveMembers(Long projectId) {
        return projectMemberRepository.findByProjectId(projectId)
                .stream()
                .filter(member -> member.getStatus() == ProjectMemberStatus.ACTIVE)
                .toList();
    }

    // policy: ROL-P-03(다중 Role 권한 합집합), ROL-P-04(Role 변경 즉시 반영), PJM-P-05(비활성 멤버 권한 차단)
    public Map<ProjectMember, List<Role>> findActiveMembersWithRoles(Long projectId) {
        List<ProjectMember> activeMembers = findActiveMembers(projectId);
        if (activeMembers.isEmpty()) {
            return Map.of();
        }

        List<Role> activeProjectRoles = roleRepository.findByProjectIdAndIsActiveTrue(projectId);
        if (activeProjectRoles.isEmpty()) {
            return activeMembers.stream()
                    .collect(Collectors.toMap(
                            member -> member,
                            member -> List.of(),
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));
        }

        List<ProjectMemberRole> activeRoleLinks =
                projectMemberRoleRepository.findActiveRoleLinksByProjectId(projectId, ProjectMemberStatus.ACTIVE);
        if (activeRoleLinks.isEmpty()) {
            return activeMembers.stream()
                    .collect(Collectors.toMap(
                            member -> member,
                            member -> List.of(),
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));
        }

        Map<Long, Role> activeRoleById = activeProjectRoles
                .stream()
                .collect(Collectors.toMap(Role::getId, role -> role));

        Map<Long, List<Role>> rolesByMemberId = new HashMap<>();
        for (ProjectMemberRole activeRoleLink : activeRoleLinks) {
            Role activeRole = activeRoleById.get(activeRoleLink.getRoleId());
            if (activeRole == null) {
                continue;
            }
            rolesByMemberId
                    .computeIfAbsent(activeRoleLink.getProjectMemberId(), ignored -> new ArrayList<>())
                    .add(activeRole);
        }

        Map<ProjectMember, List<Role>> snapshotsByMember = new LinkedHashMap<>();
        for (ProjectMember activeMember : activeMembers) {
            snapshotsByMember.put(
                    activeMember,
                    List.copyOf(rolesByMemberId.getOrDefault(activeMember.getId(), List.of()))
            );
        }
        return snapshotsByMember;
    }
}
