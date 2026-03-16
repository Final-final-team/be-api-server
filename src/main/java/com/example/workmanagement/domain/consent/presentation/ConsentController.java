package com.example.workmanagement.domain.consent.presentation;

import com.example.workmanagement.domain.consent.exception.ConsentErrorCode;
import com.example.workmanagement.domain.consent.exception.ConsentDomainException;
import com.example.workmanagement.domain.consent.presentation.dto.ConsentSubmitRequest;
import com.example.workmanagement.domain.consent.service.ConsentService;
import com.example.workmanagement.domain.consent.service.command.SubmitConsentsCommand;
import com.example.workmanagement.domain.consent.service.result.ConsentStatusResult;
import com.example.workmanagement.domain.consent.service.result.ConsentSubmitResult;
import com.example.workmanagement.domain.consent.service.result.RequiredConsentCheckResult;
import com.example.workmanagement.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 회원 동의 관련 API 진입점.
 *
 * 컨트롤러에서는 JWT subject를 userId로 해석하고,
 * 실제 동의 규칙(최신 버전 검증, 필수 동의 충족 판정)은 서비스 계층에 위임한다.
 */
@RestController
@RequestMapping("/api/consents")
@Tag(name = "회원 동의", description = "회원 동의 조회/제출 API")
public class ConsentController {

    // 동의 조회/제출/필수 체크를 한곳에서 조합
    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @GetMapping
    @Operation(summary = "내 동의 현황 조회", description = "최신 버전 동의 항목 기준으로 동의 여부를 조회합니다.")
    public ResponseEntity<ApiResponse<List<ConsentStatusResult>>> getMyConsentStatuses(
            @AuthenticationPrincipal Jwt jwt
    ) {
        // subject 문자열을 도메인에서 쓰는 userId(Long)로 변환
        Long userId = resolveUserId(jwt);
        return ResponseEntity.ok(ApiResponse.success(consentService.getConsentStatuses(userId)));
    }

    @GetMapping("/required/check")
    @Operation(summary = "필수 동의 충족 여부 조회", description = "서비스 접근에 필요한 필수 동의 충족 여부를 조회합니다.")
    public ResponseEntity<ApiResponse<RequiredConsentCheckResult>> checkMyRequiredConsents(
            @AuthenticationPrincipal Jwt jwt
    ) {
        // 필수 동의 충족 여부만 빠르게 조회 (콜백 분기/게이트 판단 공용)
        Long userId = resolveUserId(jwt);
        return ResponseEntity.ok(ApiResponse.success(consentService.checkRequiredConsents(userId)));
    }

    @PostMapping
    @Operation(summary = "동의 제출", description = "최신 버전 동의 항목에 대한 동의를 제출합니다.")
    public ResponseEntity<ApiResponse<ConsentSubmitResult>> submitConsents(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ConsentSubmitRequest request
    ) {
        Long userId = resolveUserId(jwt);

        // API DTO를 서비스용 커맨드로 변환해 계층 의존을 분리
        SubmitConsentsCommand command = new SubmitConsentsCommand(
                request.agreements().stream()
                        .map(item -> new SubmitConsentsCommand.Agreement(item.type(), item.code(), item.version(), item.agreed()))
                        .toList()
        );

        return ResponseEntity.ok(ApiResponse.success(consentService.submitConsents(userId, command)));
    }

    /**
     * 인증 주체를 userId로 해석한다.
     *
     * 인증 자체는 시큐리티에서 처리되지만, subject 포맷이 깨진 경우는
     * 동의 도메인 에러코드로 명확히 내려주기 위해 여기서 검증한다.
     */
    private Long resolveUserId(Jwt jwt) {
        if (jwt == null) {
            throw new ConsentDomainException(ConsentErrorCode.CONSENT_UNAUTHENTICATED);
        }

        try {
            return Long.parseLong(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new ConsentDomainException(ConsentErrorCode.CONSENT_INVALID_AUTH_SUBJECT);
        }
    }
}
