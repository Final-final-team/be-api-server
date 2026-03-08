# 검토 API 계약 확정 및 PR 범위 정리 계획

## Summary

- 정책 정합화는 **현재 코드 유지안**으로 고정한다.
  - `IN_PROGRESS` 상태의 업무에 대해 작성자의 상신 요청이 들어오면
  - 같은 처리 안에서 업무는 `IN_REVIEW`, 검토는 `SUBMITTED`가 된다.
- 이번 단계의 목적은 검토 도메인 액션 API를 **프론트 연동 가능한 최종 계약**으로 고정하는 것이다.
- 테스트 코드는 구현 검증용으로만 유지하고, **현재 PR 범위에서는 제외**한다.

## Key Changes

- 정책 기준 확정
  - 검토 생성/재상신의 시작 상태는 `Task.IN_PROGRESS`
  - 상신 요청 처리 결과는 `Task.IN_REVIEW + Review.SUBMITTED`
  - 재상신은 `REJECTED` 검토 이력이 있는 경우의 새 검토 생성으로 정의
- API 스펙 최종 고정
  - `DOCS/review/review-action-api-spec.md`를 단일 기준 문서로 사용
  - 각 API에 대해 아래를 확정한다.
    - Method / Path
    - Request Header
    - Request Body 필드, 타입, 필수 여부
    - Response Body 필드
    - 상태 조건
    - 권한 조건
    - HTTP status / 에러코드
    - `If-Match`와 `lockVersion` 규칙
  - 중복 문서는 두지 않고, 표형 문서는 별도 유지하지 않는다.
- 프론트 연동용 계약 보강
  - 각 상태별 허용 액션 표 추가
    - `SUBMITTED`, `APPROVED`, `REJECTED`, `CANCELLED`
  - 각 주체별 허용 액션 표 추가
    - 작성자, 참조자, 기본 검토 권한자, 추가 검토자, 관리자 예외 권한자
  - 버튼 기준 문구를 명확히 고정
    - 상신 / 재상신 / 승인 / 반려 / 취소
    - 참조자 추가·해제
    - 첨부 presign / 확정 / 해제
    - 추가 검토자 할당·해제
    - 코멘트 작성 / 수정 / 삭제
- PR 범위 정리
  - 현재 PR에는 검토 도메인 코드만 포함
  - 테스트 코드는 PR에서 제외
  - 로컬 문서(`AGENTS.md`, `PLAN`, `DOCS`, `WHY`)는 계속 PR 제외
  - `src/test/...` 변경도 이번 PR에서는 제외 대상으로 고정

## Implementation Changes

- 문서 확정 항목
  - `submitReview`를 “업무 상태 전이 포함 액션”으로 명시
  - `ReviewCreateRequest`에 `taskVersionNo` 없음 명시
  - `ReviewDetailResponse`의 `lockVersion`을 프론트 동시성 기준값으로 명시
  - `409 REVIEW_VERSION_CONFLICT`를 프론트 재조회 트리거로 명시
- PR 정리 항목
  - 검토 도메인 코드 브랜치에서 테스트 관련 파일을 제외한 diff만 남도록 정리
  - 최소 제외 대상:
    - `src/test/java/com/example/workmanagement/domain/review/service/ReviewCommandServiceTest.java`
    - `src/test/resources/application.properties`
- 문서에 추가로 고정할 표현
  - “생성/재상신은 작성자의 상신 요청 액션”
  - “검토 취소는 제출 철회”
  - “첨부 해제는 S3 삭제가 아니라 DB 연결 해제”
  - “권한 실구현은 후속이지만, 권한 계약 자체는 본 문서 기준으로 고정”

## Test Plan

- 문서-코드 정합성 확인
  - 생성/재상신 시작 상태가 코드와 문서 모두 `IN_PROGRESS`인지
  - 생성 결과가 `IN_REVIEW + SUBMITTED`로 일치하는지
- API 계약 확인
  - DTO 필드와 문서의 request/response 필드가 일치하는지
  - `If-Match`가 필요한 API와 아닌 API가 일치하는지
  - 에러코드와 실제 서비스 예외가 일치하는지
- PR 범위 확인
  - `git diff origin/dev...HEAD`에 테스트 파일이 포함되지 않는지
  - `git diff origin/dev...HEAD`에 로컬 전용 문서가 포함되지 않는지

## Assumptions

- 검토 생성 정책은 원문보다 현재 구현 흐름을 우선해 해석한다.
- 이번 단계에서는 테스트 품질 보강보다 **프론트 연동 계약 고정**이 우선이다.
- 테스트 코드는 로컬 검증용으로는 유지할 수 있지만, 현재 PR에는 포함하지 않는다.
- 권한 실구현 전이라도 API 명세서의 권한 조건과 실패 에러코드는 확정 대상으로 본다.
