package com.example.workmanagement.domain.project.repository;

import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.workmanagement.domain.project.entity.Project;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("""
            select p
            from Project p
            join com.example.workmanagement.domain.project.entity.ProjectMember pm on pm.projectId = p.id
            where pm.userId = :userId
              and pm.status = :status
            order by p.updatedAt desc, p.id desc
            """)
    List<Project> findAccessibleProjectsByUserId(
            @Param("userId") Long userId,
            @Param("status") ProjectMemberStatus status
    );
}
