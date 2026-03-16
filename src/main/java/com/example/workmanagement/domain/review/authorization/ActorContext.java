package com.example.workmanagement.domain.review.authorization;

import java.util.Collections;
import java.util.Set;

public record ActorContext(
        Long actorId,
        Set<String> roles,
        Set<String> permissions // 현재 별도의 users 와 permission 도메인이 없으므로 Mock 차원에서 임시 구현, 추후 삭제 예정
) {
    // TODO 정책 코드: RVW-P-00-001, RVW-P-02-002, RVW-P-02-003
    // 프로젝트 소속 자격과 요청 시점 권한 재검증이 아직 없다.
    // TODO 정책 코드: RVW-P-09-001, RVW-P-09-002, RVW-P-09-003, RVW-P-09-004
    // 퇴사/비활성 사용자 처리와 탈퇴 사용자 표시 정책이 아직 없다.

    public ActorContext {
        roles = roles == null ? Collections.emptySet() : Set.copyOf(roles);
        permissions = permissions == null ? Collections.emptySet() : Set.copyOf(permissions);
    }

    /**
     * 요청자가 특정 권한을 보유하는지 확인한다.
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    /**
     * 요청자가 관리자 예외 권한을 보유하는지 확인한다.
     */
    // TODO 정책 코드: RVW-P-03-004
    // 정책서의 권한명은 REVIEW_ADMIN_OVERRIDE 인데 현재는 legacy ADMIN_OVERRIDE 상수를 참조한다.
    public boolean isAdminOverride() {
        return hasPermission(ReviewPermissions.ADMIN_OVERRIDE);
    }
}
