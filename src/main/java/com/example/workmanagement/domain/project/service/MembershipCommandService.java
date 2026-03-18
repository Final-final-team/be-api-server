package com.example.workmanagement.domain.project.service;

import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.project.repository.ProjectRepository;
import com.example.workmanagement.domain.project.service.command.JoinProjectCommand;
import com.example.workmanagement.domain.project.service.command.LeaveProjectCommand;
import com.example.workmanagement.domain.project.service.command.RemoveMemberCommand;
import com.example.workmanagement.global.authorization.PermissionChecker;
import com.example.workmanagement.global.role.repository.ProjectMemberRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MembershipCommandService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;
    private final PermissionChecker permissionChecker;

    public MembershipCommandService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            ProjectMemberRoleRepository projectMemberRoleRepository,
            PermissionChecker permissionChecker
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectMemberRoleRepository = projectMemberRoleRepository;
        this.permissionChecker = permissionChecker;
    }

    // policy: PJM-F-01, PJM-P-03
    public void join(JoinProjectCommand command) {
        throw new UnsupportedOperationException("join is not implemented yet");
    }

    // policy: PJM-F-03, PJM-P-05, PJM-P-06
    public void leave(LeaveProjectCommand command) {
        throw new UnsupportedOperationException("leave is not implemented yet");
    }

    // policy: PJM-F-04, PJM-P-05, PJM-P-06
    public void remove(RemoveMemberCommand command) {
        throw new UnsupportedOperationException("remove is not implemented yet");
    }
}
