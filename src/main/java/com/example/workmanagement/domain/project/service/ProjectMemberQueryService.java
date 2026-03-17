package com.example.workmanagement.domain.project.service;

import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProjectMemberQueryService {

    private final ProjectMemberRepository projectMemberRepository;

    public ProjectMemberQueryService(ProjectMemberRepository projectMemberRepository) {
        this.projectMemberRepository = projectMemberRepository;
    }

    // policy: PJM-P-05 (퇴장/방출 후 효과 정책 - ACTIVE 멤버만 조회)
    public List<ProjectMember> findActiveMembers(Long projectId) {
        return projectMemberRepository.findByProjectId(projectId)
                .stream()
                .filter(member -> member.getStatus() == ProjectMemberStatus.ACTIVE)
                .toList();
    }
}
