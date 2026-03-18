package com.example.workmanagement.domain.review.authorization;

import java.util.Collections;
import java.util.Set;

public record ActorContext(
        Long actorId,
        Set<String> roles,
        Set<String> permissions // 공통 권한 스냅샷을 review 내부 문자열 상수로 매핑한 임시 어댑터
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
    public boolean isAdminOverride() {
        return hasPermission(ReviewPermissions.ADMIN_OVERRIDE)
                || hasPermission(ReviewPermissions.REVIEW_ADMIN_OVERRIDE);
    }
}
