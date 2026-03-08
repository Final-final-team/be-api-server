package com.example.workmanagement.domain.review.authorization;

import java.util.Collections;
import java.util.Set;

public record ActorContext(
        Long actorId,
        Set<String> roles,
        Set<String> permissions
) {

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
        return hasPermission(ReviewPermissions.ADMIN_OVERRIDE);
    }
}
