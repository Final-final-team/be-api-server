package com.example.workmanagement.domain.project.service.result;

import java.util.List;

public record ProjectBootstrapResult(
        boolean hasProject,
        Long defaultProjectId,
        List<ProjectSummaryResult> projects
) {
}
