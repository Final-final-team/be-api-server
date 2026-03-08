# 검토 도메인 모델링 및 테이블 명세

## 1. 애그리거트 경계

### Review Aggregate

- Aggregate Root: `Review`
- 포함 엔티티: `ReviewReference`, `ReviewAttachment`, `ReviewComment`
- 별도 기록 엔티티: `ReviewHistory`
- 외부 참조 엔티티: `Task`, `Member`

## 2. 상태 머신

### 리뷰 상태

| 현재 상태 | 허용 액션 | 다음 상태 | 비고 |
| --- | --- | --- | --- |
| 없음 | 검토 생성 | `SUBMITTED` | 업무가 이미 `IN_REVIEW`여야 함 |
| `SUBMITTED` | 승인 | `APPROVED` | 업무는 `COMPLETED`로 전환 |
| `SUBMITTED` | 반려 | `REJECTED` | 반려 사유 필수, 업무는 `IN_PROGRESS`로 전환 |
| `SUBMITTED` | 취소 | `CANCELLED` | 제출 철회 의미, 업무는 `IN_PROGRESS`로 전환 |
| `REJECTED` | 재상신 | 신규 `SUBMITTED` 생성 | 기존 리뷰 재사용 금지 |
| `APPROVED` | 신규 코멘트 작성 | `APPROVED` 유지 | 리뷰 본체는 잠김 |
| `REJECTED` | 직접 변경 불가 | 상태 유지 | 신규 코멘트도 불가 |
| `CANCELLED` | 직접 변경 불가 | 상태 유지 | 재상신은 새 리뷰 생성 |

### 업무 상태 연동

| 리뷰 이벤트 | 업무 상태 결과 |
| --- | --- |
| 검토 생성/재상신 | `IN_REVIEW` |
| 검토 승인 | `COMPLETED` |
| 검토 반려 | `IN_PROGRESS` |
| 검토 취소 | `IN_PROGRESS` |

## 3. 핵심 불변 조건

1. 업무가 `IN_REVIEW` 상태일 때만 검토를 생성할 수 있다.
2. 동일한 `task_id + task_version_no`에 대해 `SUBMITTED` 상태 검토는 하나만 존재할 수 있다.
3. `REJECTED` 이후 재상신은 기존 리뷰 상태를 되돌리지 않고 새 리뷰를 생성한다.
4. `APPROVED`, `REJECTED`, `CANCELLED` 상태 리뷰의 본문/첨부/참조자는 변경할 수 없다.
5. `APPROVED` 상태에서는 신규 코멘트만 허용된다.
6. `REJECTED` 상태에서는 코멘트 작성/수정/삭제가 모두 금지된다.
7. 검토 승인 시 `tasks.current_version_no == reviews.task_version_no`를 만족해야 한다.
8. 반려 사유는 `REJECTED` 전이에 필수다.

## 4. 동시성 규칙

- 낙관적 락 기준 컬럼: `reviews.lock_version`
- `approve`, `reject`, `cancel`, 참조자 변경, 첨부 변경은 모두 `If-Match` 헤더에 리뷰 버전을 받아 처리한다.
- 충돌 시 `409 Conflict`와 도메인 에러 코드를 반환한다.
- 코멘트는 리뷰 락 버전 대신 현재 리뷰 상태를 다시 확인하여 처리한다.

### 대표 충돌 시나리오

| 동시 요청 | 처리 원칙 |
| --- | --- |
| 승인 + 승인 | 먼저 반영된 쪽만 성공 |
| 승인 + 반려 | 먼저 반영된 쪽만 성공 |
| 승인 + 첨부 변경 | 저장 완료된 최신 `lock_version` 기준으로 후행 요청 실패 |
| 승인 + 참조자 변경 | 저장 완료된 최신 `lock_version` 기준으로 후행 요청 실패 |
| 승인 + 코멘트 작성 | 승인 자체는 가능, 코멘트는 승인 이후 신규 작성 허용 |
| 반려 + 코멘트 작성 | 반려가 먼저 완료되면 코멘트 요청 실패 |

## 5. 감사 이벤트

| action_type | 설명 |
| --- | --- |
| `REVIEW_CREATED` | 최초 상신 |
| `REVIEW_RESUBMITTED` | 반려 이후 새 검토 생성 |
| `REVIEW_APPROVED` | 승인 |
| `REVIEW_REJECTED` | 반려 |
| `REVIEW_CANCELLED` | 제출 철회 |
| `REFERENCE_ASSIGNED` | 참조자 추가 |
| `REFERENCE_REMOVED` | 참조자 해제 |
| `ATTACHMENT_ADDED` | 첨부 확정 |
| `ATTACHMENT_REMOVED` | 첨부 삭제 |
| `COMMENT_CREATED` | 코멘트 생성 |
| `COMMENT_UPDATED` | 코멘트 수정 |
| `COMMENT_DELETED` | 코멘트 삭제 |

## 6. 물리 테이블 명세

### `tasks`

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | bigint | PK | 업무 식별자 |
| `status` | varchar(30) | not null | `IN_PROGRESS`, `IN_REVIEW`, `COMPLETED` |
| `author_id` | bigint | not null | 작성자 회원 ID |
| `current_version_no` | int | not null | 현재 업무 본문 버전 |
| `lock_version` | bigint | not null | JPA `@Version` |
| `created_at` | timestamptz | not null | 생성 시각 |
| `updated_at` | timestamptz | not null | 수정 시각 |

### `reviews`

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | bigint | PK | 검토 식별자 |
| `task_id` | bigint | FK -> tasks.id | 대상 업무 |
| `task_version_no` | int | not null | 검토가 바라보는 업무 버전 |
| `round_no` | int | not null | 검토 라운드 |
| `status` | varchar(30) | not null | `SUBMITTED`, `APPROVED`, `REJECTED`, `CANCELLED` |
| `content` | text | not null | 검토 본문 |
| `rejection_reason` | text | nullable | 반려 사유 |
| `submitted_by` | bigint | not null | 상신자 |
| `decided_by` | bigint | nullable | 승인/반려 처리자 |
| `decided_at` | timestamptz | nullable | 승인/반려 시각 |
| `cancelled_by` | bigint | nullable | 취소 처리자 |
| `cancelled_at` | timestamptz | nullable | 취소 시각 |
| `lock_version` | bigint | not null | JPA `@Version` |
| `created_at` | timestamptz | not null | 생성 시각 |
| `updated_at` | timestamptz | not null | 수정 시각 |

### `review_references`

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | bigint | PK | 참조자 관계 ID |
| `review_id` | bigint | FK -> reviews.id | 대상 검토 |
| `user_id` | bigint | not null | 참조자 회원 ID |
| `added_by` | bigint | not null | 할당 수행자 |
| `created_at` | timestamptz | not null | 생성 시각 |

### `review_attachments`

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | bigint | PK | 첨부 식별자 |
| `review_id` | bigint | FK -> reviews.id | 대상 검토 |
| `object_key` | varchar(255) | not null | 스토리지 객체 키 |
| `original_name` | varchar(255) | not null | 원본 파일명 |
| `content_type` | varchar(150) | nullable | MIME 타입 |
| `size_bytes` | bigint | not null | 파일 크기 |
| `sort_order` | int | not null | 화면 정렬 순서 |
| `uploaded_by` | bigint | not null | 업로드 사용자 |
| `created_at` | timestamptz | not null | 생성 시각 |

### `review_comments`

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | bigint | PK | 코멘트 식별자 |
| `review_id` | bigint | FK -> reviews.id | 대상 검토 |
| `author_id` | bigint | not null | 코멘트 작성자 |
| `content` | text | not null | 코멘트 본문 |
| `is_edited` | boolean | not null | 편집 여부 |
| `edited_at` | timestamptz | nullable | 편집 시각 |
| `deleted_at` | timestamptz | nullable | 소프트 삭제 시각 |
| `deleted_by` | bigint | nullable | 삭제자 |
| `created_at` | timestamptz | not null | 생성 시각 |
| `updated_at` | timestamptz | not null | 수정 시각 |

### `review_histories`

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | bigint | PK | 이력 식별자 |
| `review_id` | bigint | FK -> reviews.id | 대상 검토 |
| `action_type` | varchar(50) | not null | 이벤트 유형 |
| `actor_id` | bigint | not null | 수행자 |
| `reason` | text | nullable | 사유 |
| `target_type` | varchar(30) | not null | `TASK`, `REVIEW`, `REFERENCE`, `ATTACHMENT`, `COMMENT` |
| `target_id` | bigint | not null | 실제 대상 ID |
| `metadata_json` | text | nullable | 부가 정보 |
| `occurred_at` | timestamptz | not null | 발생 시각 |

## 7. 후속 확장

- 추가 검토자 전용 매핑 테이블
- 관리자 예외 수정에 대한 별도 감사 이벤트와 권한 모델
- `metadata_json`의 `jsonb` 승격
- presigned URL 만료/확정 실패 정리 배치

