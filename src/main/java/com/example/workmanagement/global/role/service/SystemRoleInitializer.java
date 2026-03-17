package com.example.workmanagement.global.role.service;

import com.example.workmanagement.global.role.entity.Role;
import com.example.workmanagement.global.role.repository.RoleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * System Role 초기화 컴포넌트
 * 프로젝트 생성 시 PROJECT_LEADER와 PROJECT_MEMBER 시스템 역할을 생성합니다.
 */
@Component
public class SystemRoleInitializer {

    private final RoleRepository roleRepository;

    public SystemRoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * 프로젝트를 위한 시스템 Role 초기화
     * - PROJECT_LEADER: 모든 권한 (-1L)
     * - PROJECT_MEMBER: 권한 없음 (0L)
     *
     * @param projectId 프로젝트 ID
     * @param creatorPmId 프로젝트 생성자 ProjectMember ID
     */
    @Transactional
    public void initializeSystemRoles(Long projectId, Long creatorPmId) {
        // 중복 체크: 이미 시스템 Role이 있으면 생성하지 않음
        List<Role> existingSystemRoles = roleRepository.findByProjectIdAndIsSystemTrue(projectId);
        if (!existingSystemRoles.isEmpty()) {
            return;
        }

        // PROJECT_LEADER 생성
        Role leaderRole = Role.systemRole(
            projectId,
            "PROJECT_LEADER",
            "프로젝트 리더",
            "프로젝트의 모든 권한을 가진 리더 역할",
            -1L, -1L, -1L,
            true,
            creatorPmId
        );
        // PROJECT_MEMBER 생성
        Role memberRole = Role.systemRole(
            projectId,
            "PROJECT_MEMBER",
            "프로젝트 멤버",
            "기본 프로젝트 멤버 역할",
            0L, 0L, 0L,
            false,
            creatorPmId
        );
        roleRepository.saveAll(List.of(leaderRole, memberRole));
    }
}
