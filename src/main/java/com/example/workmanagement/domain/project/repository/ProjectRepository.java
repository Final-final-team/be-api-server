package com.example.workmanagement.domain.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.workmanagement.domain.project.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
