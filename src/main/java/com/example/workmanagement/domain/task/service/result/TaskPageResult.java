package com.example.workmanagement.domain.task.service.result;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "업무 목록 페이지 응답")
public record TaskPageResult<T>(
        @Schema(description = "현재 페이지 데이터")
        List<T> items,
        @Schema(description = "현재 페이지 번호(0-base)", example = "0")
        int page,
        @Schema(description = "페이지 크기", example = "20")
        int size,
        @Schema(description = "전체 데이터 건수", example = "135")
        long totalElements,
        @Schema(description = "전체 페이지 수", example = "7")
        int totalPages,
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,
        @Schema(description = "이전 페이지 존재 여부", example = "false")
        boolean hasPrevious
) {
    public static <T> TaskPageResult<T> from(Page<T> page) {
        return new TaskPageResult<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
