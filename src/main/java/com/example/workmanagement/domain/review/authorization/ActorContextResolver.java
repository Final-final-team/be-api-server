package com.example.workmanagement.domain.review.authorization;

import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import com.example.workmanagement.domain.review.error.ReviewErrorCode;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ActorContextResolver {

    /**
     * 임시 헤더 값을 ActorContext로 변환한다.
     */
    public ActorContext resolve(String actorIdHeader, String rolesHeader, String permissionsHeader) {
        if (actorIdHeader == null || actorIdHeader.isBlank()) {
            throw new ReviewDomainException(ReviewErrorCode.ACTOR_HEADER_MISSING);
        }

        try {
            return new ActorContext(
                    Long.parseLong(actorIdHeader),
                    parseCsv(rolesHeader),
                    parseCsv(permissionsHeader)
            );
        } catch (NumberFormatException exception) {
            throw new ReviewDomainException(ReviewErrorCode.INVALID_ACTOR_ID_HEADER);
        }
    }

    /**
     * 쉼표 구분 헤더를 trim 처리된 집합으로 변환한다.
     */
    private Set<String> parseCsv(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(headerValue.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }
}
