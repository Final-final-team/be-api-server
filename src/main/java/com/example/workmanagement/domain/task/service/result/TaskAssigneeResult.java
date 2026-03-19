package com.example.workmanagement.domain.task.service.result;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "업무 담당자 응답")
public record TaskAssigneeResult(
        @Schema(description = "사용자 ID", example = "101")
        Long userId,
        @Schema(description = "사용자 닉네임", example = "김하늘")
        String name
) {
}
