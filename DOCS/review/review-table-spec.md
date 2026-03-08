# 검토 도메인 테이블 명세

## 1. `tasks`

| 컬럼명 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | `bigint` | PK | 업무 ID |
| `status` | `varchar(30)` | not null | 업무 상태 |
| `author_id` | `bigint` | not null | 업무 작성자 ID |
| `lock_version` | `bigint` | not null | 낙관적 락 버전 |
| `created_at` | `timestamp` | not null | 생성 시각 |
| `updated_at` | `timestamp` | not null | 수정 시각 |

## 2. `reviews`

| 컬럼명 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | `bigint` | PK | 검토 ID |
| `task_id` | `bigint` | FK -> `tasks.id`, not null | 대상 업무 ID |
| `round_no` | `int` | not null | 해당 업무의 검토 라운드 순번 |
| `status` | `varchar(30)` | not null | 검토 상태 |
| `content` | `text` | not null | 검토 본문 |
| `rejection_reason` | `text` | null | 반려 사유 |
| `submitted_by` | `bigint` | not null | 상신자 ID |
| `decided_by` | `bigint` | null | 승인/반려 처리자 ID |
| `decided_at` | `timestamp` | null | 승인/반려 처리 시각 |
| `cancelled_by` | `bigint` | null | 취소자 ID |
| `cancelled_at` | `timestamp` | null | 취소 시각 |
| `lock_version` | `bigint` | not null | 낙관적 락 버전 |
| `created_at` | `timestamp` | not null | 생성 시각 |
| `updated_at` | `timestamp` | not null | 수정 시각 |

## 3. `review_references`

| 컬럼명 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | `bigint` | PK | 참조자 매핑 ID |
| `review_id` | `bigint` | FK -> `reviews.id`, not null | 검토 ID |
| `user_id` | `bigint` | not null | 참조자 사용자 ID |
| `added_by` | `bigint` | not null | 참조자 추가 수행자 ID |
| `created_at` | `timestamp` | not null | 생성 시각 |

## 4. `review_additional_reviewers`

| 컬럼명 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | `bigint` | PK | 추가 검토자 매핑 ID |
| `review_id` | `bigint` | FK -> `reviews.id`, not null | 검토 ID |
| `user_id` | `bigint` | not null | 추가 검토자 사용자 ID |
| `assigned_by` | `bigint` | not null | 할당 수행자 ID |
| `created_at` | `timestamp` | not null | 생성 시각 |

## 5. `review_attachments`

| 컬럼명 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | `bigint` | PK | 첨부 ID |
| `review_id` | `bigint` | FK -> `reviews.id`, not null | 검토 ID |
| `object_key` | `varchar(255)` | not null | 스토리지 키 |
| `original_name` | `varchar(255)` | not null | 원본 파일명 |
| `content_type` | `varchar(150)` | null | MIME 타입 |
| `size_bytes` | `bigint` | not null | 파일 크기(byte) |
| `sort_order` | `int` | not null | 정렬 순서 |
| `uploaded_by` | `bigint` | not null | 업로드 사용자 ID |
| `created_at` | `timestamp` | not null | 생성 시각 |

## 6. `review_comments`

| 컬럼명 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | `bigint` | PK | 코멘트 ID |
| `review_id` | `bigint` | FK -> `reviews.id`, not null | 검토 ID |
| `author_id` | `bigint` | not null | 작성자 ID |
| `content` | `text` | not null | 코멘트 본문 |
| `is_edited` | `boolean` | not null | 수정 여부 |
| `edited_at` | `timestamp` | null | 수정 시각 |
| `deleted_at` | `timestamp` | null | 삭제 시각 |
| `deleted_by` | `bigint` | null | 삭제자 ID |
| `created_at` | `timestamp` | not null | 생성 시각 |
| `updated_at` | `timestamp` | not null | 수정 시각 |

## 7. `review_histories`

| 컬럼명 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | `bigint` | PK | 감사 로그 ID |
| `review_id` | `bigint` | FK -> `reviews.id`, not null | 검토 ID |
| `action_type` | `varchar(50)` | not null | 이벤트 타입 |
| `actor_id` | `bigint` | not null | 수행자 ID |
| `reason` | `text` | null | 사유 |
| `target_type` | `varchar(30)` | not null | 대상 타입 |
| `target_id` | `bigint` | not null | 대상 ID |
| `metadata_json` | `text` | null | 부가 메타데이터 |
| `occurred_at` | `timestamp` | not null | 이벤트 발생 시각 |

## 8. 인덱스 및 제약 조건

### 필수 유니크/인덱스

- `reviews(task_id, round_no)` unique
- `review_references(review_id, user_id)` unique
- `review_additional_reviewers(review_id, user_id)` unique
- `review_comments(review_id, created_at desc)` index
- `review_histories(review_id, occurred_at desc)` index
- `review_attachments(review_id, sort_order)` index

### 부분 유니크 인덱스

동일 업무에 유효한 `SUBMITTED` 검토는 하나만 허용한다.

```sql
create unique index uk_reviews_task_submitted
    on reviews (task_id)
    where status = 'SUBMITTED';
```

## 9. 감사 로그 `action_type` 후보

- `REVIEW_CREATED`
- `REVIEW_RESUBMITTED`
- `REVIEW_UPDATED`
- `REVIEW_APPROVED`
- `REVIEW_REJECTED`
- `REVIEW_CANCELLED`
- `REFERENCE_ASSIGNED`
- `REFERENCE_REMOVED`
- `ADDITIONAL_REVIEWER_ASSIGNED`
- `ADDITIONAL_REVIEWER_REMOVED`
- `ATTACHMENT_ADDED`
- `ATTACHMENT_REMOVED`
- `COMMENT_CREATED`
- `COMMENT_UPDATED`
- `COMMENT_DELETED`
