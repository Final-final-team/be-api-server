package com.example.workmanagement.domain.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.workmanagement.domain.project.entity.ProjectMember;

import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProjectId(Long projectId); // 프로젝트에 속한 멤버 조회
    List<ProjectMember> findByUserId(Long userId); // 멤버가 속한 프로젝트 조회
}
