# 검토 액션 API 표

## 공통 응답 형식

### 성공 응답

모든 성공 응답은 `ApiResponse<T>`를 사용한다.

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Review submitted.",
  "data": {},
  "timestamp": "2026-03-08T10:00:00Z"
}
```

### 실패 응답

모든 실패 응답은 `ErrorResponse`를 사용한다.

```json
{
  "code": "REVIEW_APPROVAL_FORBIDDEN",
  "message": "User is not allowed to approve this review.",
  "timestamp": "2026-03-08T10:00:00Z",
  "path": "/api/v1/reviews/3001/approve"
}
```

## 액션 API 표

| 기능 | Method | Path | 상태 조건 | 권한 조건 | Request | Response | Error Codes | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 검토 생성 | `POST` | `/api/v1/tasks/{taskId}/reviews` | 시작 업무 상태 `IN_PROGRESS` | 작성자 또는 관리자 예외 권한자 | `ReviewCreateRequest` | `ApiResponse<ReviewDetailResponse>` | `TASK_NOT_FOUND`, `REVIEW_SUBMIT_NOT_ALLOWED`, `REVIEW_SUBMIT_FORBIDDEN`, `REVIEW_ALREADY_SUBMITTED_FOR_TASK_VERSION`, `NOT_IMPLEMENTED` | 처리 결과로 업무는 `IN_REVIEW`, 검토는 `SUBMITTED` 생성 |
| 검토 재상신 | `POST` | `/api/v1/tasks/{taskId}/reviews` | 시작 업무 상태 `IN_PROGRESS`, 기존 `REJECTED` 검토 존재 | 작성자 또는 관리자 예외 권한자 | `ReviewCreateRequest` | `ApiResponse<ReviewDetailResponse>` | `TASK_NOT_FOUND`, `REVIEW_RESUBMISSION_NOT_ALLOWED`, `REVIEW_RESUBMISSION_FORBIDDEN`, `REVIEW_ALREADY_SUBMITTED_FOR_TASK_VERSION`, `NOT_IMPLEMENTED` | 생성 API와 동일 엔드포인트 사용 |
| 검토 수정 | `PATCH` | `/api/v1/reviews/{reviewId}` | `SUBMITTED` | 작성자 또는 관리자 예외 권한자 | `ReviewUpdateRequest` | `ApiResponse<ReviewDetailResponse>` | `REVIEW_NOT_FOUND`, `REVIEW_UPDATE_NOT_ALLOWED`, `REVIEW_UPDATE_FORBIDDEN`, `REVIEW_VERSION_CONFLICT`, `NOT_IMPLEMENTED` | 본문 `content`만 수정 대상, `If-Match` 헤더 필요 |
| 검토 승인 | `POST` | `/api/v1/reviews/{reviewId}/approve` | `SUBMITTED` | 기본 검토 권한자 또는 추가 검토자 또는 관리자 예외 권한자 | 없음 | `ApiResponse<ReviewDetailResponse>` | `REVIEW_NOT_FOUND`, `REVIEW_APPROVAL_NOT_ALLOWED`, `REVIEW_APPROVAL_FORBIDDEN`, `REVIEW_VERSION_CONFLICT`, `NOT_IMPLEMENTED` | 참조자는 승인 불가, `If-Match` 헤더 필요 |
| 검토 반려 | `POST` | `/api/v1/reviews/{reviewId}/reject` | `SUBMITTED` | 기본 검토 권한자 또는 추가 검토자 또는 관리자 예외 권한자 | `ReviewRejectRequest` | `ApiResponse<ReviewDetailResponse>` | `REVIEW_NOT_FOUND`, `REVIEW_REJECTION_NOT_ALLOWED`, `REVIEW_REJECTION_FORBIDDEN`, `REJECTION_REASON_REQUIRED`, `REVIEW_VERSION_CONFLICT`, `NOT_IMPLEMENTED` | 참조자는 반려 불가, `If-Match` 헤더 필요 |
| 검토 취소 | `POST` | `/api/v1/reviews/{reviewId}/cancel` | `SUBMITTED` | 제출자 또는 관리자 예외 권한자 | `ReviewCancelRequest` | `ApiResponse<ReviewDetailResponse>` | `REVIEW_NOT_FOUND`, `REVIEW_CANCEL_NOT_ALLOWED`, `REVIEW_CANCEL_FORBIDDEN`, `REVIEW_VERSION_CONFLICT`, `NOT_IMPLEMENTED` | 제출 철회 의미, 물리 삭제 대신 취소 사용, `If-Match` 헤더 필요 |
| 참조자 추가 | `POST` | `/api/v1/reviews/{reviewId}/references` | `SUBMITTED` | 작성자 또는 관리자 예외 권한자 | `ReferenceAssignRequest` | `ApiResponse<ReviewDetailResponse>` | `REVIEW_NOT_FOUND`, `REFERENCE_ASSIGN_NOT_ALLOWED`, `REFERENCE_ASSIGN_FORBIDDEN`, `REFERENCE_ALREADY_ASSIGNED`, `REVIEW_VERSION_CONFLICT`, `NOT_IMPLEMENTED` | `If-Match` 헤더 필요 |
| 참조자 해제 | `DELETE` | `/api/v1/reviews/{reviewId}/references/{userId}` | `SUBMITTED` | 작성자 또는 관리자 예외 권한자 | 없음 | `ApiResponse<ReviewDetailResponse>` | `REVIEW_NOT_FOUND`, `REFERENCE_UNASSIGN_NOT_ALLOWED`, `REFERENCE_UNASSIGN_FORBIDDEN`, `REVIEW_VERSION_CONFLICT`, `NOT_IMPLEMENTED` | `If-Match` 헤더 필요 |
| 첨부 업로드 URL 발급 | `POST` | `/api/v1/reviews/{reviewId}/attachments/presign` | `SUBMITTED` | 작성자 또는 관리자 예외 권한자 | `AttachmentPresignRequest` | `ApiResponse<AttachmentPresignResponse>` | `REVIEW_NOT_FOUND`, `ATTACHMENT_ADD_NOT_ALLOWED`, `ATTACHMENT_ADD_FORBIDDEN`, `REVIEW_VERSION_CONFLICT`, `NOT_IMPLEMENTED` | presigned URL 발급 단계, `If-Match` 헤더 필요 |
| 검토자료 첨부 확정 | `POST` | `/api/v1/reviews/{reviewId}/attachments` | `SUBMITTED` | 작성자 또는 관리자 예외 권한자 | `AttachmentConfirmRequest` | `ApiResponse<ReviewDetailResponse>` | `REVIEW_NOT_FOUND`, `ATTACHMENT_ADD_NOT_ALLOWED`, `ATTACHMENT_ADD_FORBIDDEN`, `REVIEW_VERSION_CONFLICT`, `NOT_IMPLEMENTED` | S3 업로드가 이미 완료되었다는 전제로 DB 연결만 생성, `If-Match` 헤더 필요 |
| 첨부 해제 | `DELETE` | `/api/v1/reviews/{reviewId}/attachments/{attachmentId}` | `SUBMITTED` | 작성자 또는 관리자 예외 권한자 | 없음 | `ApiResponse<ReviewDetailResponse>` | `REVIEW_NOT_FOUND`, `ATTACHMENT_REMOVE_NOT_ALLOWED`, `ATTACHMENT_REMOVE_FORBIDDEN`, `REVIEW_VERSION_CONFLICT`, `NOT_IMPLEMENTED` | S3 원본 즉시 삭제가 아니라 DB 연결 해제 기준, `If-Match` 헤더 필요 |
| 추가 검토자 할당 | `POST` | `/api/v1/reviews/{reviewId}/additional-reviewers` | `SUBMITTED` | 작성자 또는 관리자 예외 권한자 | `AdditionalReviewerAssignRequest` | `ApiResponse<ReviewDetailResponse>` | `REVIEW_NOT_FOUND`, `ADDITIONAL_REVIEWER_ASSIGN_NOT_ALLOWED`, `ADDITIONAL_REVIEWER_ASSIGN_FORBIDDEN`, `ADDITIONAL_REVIEWER_ALREADY_ASSIGNED`, `REVIEW_VERSION_CONFLICT`, `NOT_IMPLEMENTED` | `If-Match` 헤더 필요 |
| 추가 검토자 할당 취소 | `DELETE` | `/api/v1/reviews/{reviewId}/additional-reviewers/{userId}` | `SUBMITTED` | 작성자 또는 관리자 예외 권한자 | 없음 | `ApiResponse<ReviewDetailResponse>` | `REVIEW_NOT_FOUND`, `ADDITIONAL_REVIEWER_UNASSIGN_NOT_ALLOWED`, `ADDITIONAL_REVIEWER_UNASSIGN_FORBIDDEN`, `REVIEW_VERSION_CONFLICT`, `NOT_IMPLEMENTED` | `If-Match` 헤더 필요 |
| 검토 코멘트 작성 | `POST` | `/api/v1/reviews/{reviewId}/comments` | `SUBMITTED` 또는 `APPROVED` | 작성자, 참조자, 기본 검토 권한자, 추가 검토자, 관리자 예외 권한자 | `CommentCreateRequest` | `ApiResponse<ReviewDetailResponse>` | `REVIEW_NOT_FOUND`, `COMMENT_CREATE_NOT_ALLOWED`, `COMMENT_CREATE_FORBIDDEN`, `NOT_IMPLEMENTED` | `REJECTED`에서는 작성 불가 |
| 검토 코멘트 수정 | `PATCH` | `/api/v1/reviews/{reviewId}/comments/{commentId}` | `SUBMITTED` | 코멘트 작성자 또는 관리자 예외 권한자 | `CommentUpdateRequest` | `ApiResponse<ReviewDetailResponse>` | `REVIEW_NOT_FOUND`, `COMMENT_UPDATE_NOT_ALLOWED`, `COMMENT_UPDATE_FORBIDDEN`, `NOT_IMPLEMENTED` | `APPROVED`, `REJECTED`에서는 수정 불가 |
| 검토 코멘트 삭제 | `DELETE` | `/api/v1/reviews/{reviewId}/comments/{commentId}` | `SUBMITTED` | 코멘트 작성자 또는 관리자 예외 권한자 | 없음 | `ApiResponse<ReviewDetailResponse>` | `REVIEW_NOT_FOUND`, `COMMENT_DELETE_NOT_ALLOWED`, `COMMENT_DELETE_FORBIDDEN`, `NOT_IMPLEMENTED` | `APPROVED`, `REJECTED`에서는 삭제 불가 |

## 헤더 규약

리뷰 본체 변경 API는 아래 헤더를 받는다.

```http
If-Match: 3
```

- 대상 API: 본문 수정, 승인, 반려, 취소, 참조자 추가/해제, 첨부 추가/해제, 추가 검토자 할당/해제
- 의미: 현재 클라이언트가 보고 있는 `review.lockVersion`

## Request JSON 예시

### 1. 검토 생성 / 검토 재상신

```json
{
  "content": "검토 요청 본문입니다.",
  "referenceUserIds": [201, 202],
  "attachments": [
    {
      "objectKey": "reviews/10/files/spec.pdf",
      "originalName": "spec.pdf",
      "contentType": "application/pdf",
      "sizeBytes": 204800,
      "sortOrder": 0
    }
  ]
}
```

### 2. 검토 수정

```json
{
  "content": "수정된 검토 본문입니다."
}
```

### 3. 검토 반려

```json
{
  "reason": "필수 첨부 문서가 누락되었습니다."
}
```

### 4. 검토 취소

```json
{
  "reason": "본문 보완 후 다시 상신 예정입니다."
}
```

### 5. 참조자 추가

```json
{
  "userId": 201
}
```

### 6. 첨부 업로드 URL 발급

```json
{
  "originalName": "spec.pdf",
  "contentType": "application/pdf",
  "sizeBytes": 204800
}
```

### 7. 검토자료 첨부 확정

```json
{
  "objectKey": "reviews/10/files/spec.pdf",
  "originalName": "spec.pdf",
  "contentType": "application/pdf",
  "sizeBytes": 204800,
  "sortOrder": 0
}
```

### 8. 추가 검토자 할당

```json
{
  "userId": 301
}
```

### 9. 검토 코멘트 작성

```json
{
  "content": "본문 수정 없이 확인 의견만 남깁니다."
}
```

### 10. 검토 코멘트 수정

```json
{
  "content": "표현을 조금 더 명확하게 수정했습니다."
}
```

## Response JSON 예시

### 1. `ApiResponse<ReviewDetailResponse>`

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Review submitted.",
  "data": {
    "reviewId": 3001,
    "taskId": 10,
    "roundNo": 2,
    "status": "SUBMITTED",
    "content": "검토 요청 본문입니다.",
    "rejectionReason": null,
    "submittedBy": 101,
    "decidedBy": null,
    "cancelledBy": null,
    "lockVersion": 0,
    "submittedAt": "2026-03-08T10:00:00Z",
    "decidedAt": null,
    "cancelledAt": null,
    "references": [
      {
        "userId": 201,
        "addedBy": 101,
        "createdAt": "2026-03-08T10:00:00Z"
      }
    ],
    "additionalReviewers": [
      {
        "userId": 301,
        "assignedBy": 101,
        "createdAt": "2026-03-08T10:05:00Z"
      }
    ],
    "attachments": [
      {
        "attachmentId": 4001,
        "objectKey": "reviews/10/files/spec.pdf",
        "originalName": "spec.pdf",
        "contentType": "application/pdf",
        "sizeBytes": 204800,
        "sortOrder": 0,
        "createdAt": "2026-03-08T10:00:00Z"
      }
    ],
    "comments": [
      {
        "commentId": 5001,
        "authorId": 101,
        "content": "본문 수정 없이 확인 의견만 남깁니다.",
        "edited": false,
        "editedAt": null,
        "createdAt": "2026-03-08T10:10:00Z",
        "deletedAt": null
      }
    ]
  },
  "timestamp": "2026-03-08T10:00:00Z"
}
```

### 2. `ApiResponse<AttachmentPresignResponse>`

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Attachment presign URL created.",
  "data": {
    "objectKey": "reviews/10/files/spec.pdf",
    "uploadUrl": "https://storage.example.com/presigned-url",
    "expiresAt": "2026-03-08T10:10:00Z"
  },
  "timestamp": "2026-03-08T10:00:00Z"
}
```

### 3. `ApiResponse<Void>`

`DELETE` 계열을 빈 응답으로 설계할 경우 아래 형식을 사용한다.

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Attachment removed.",
  "data": null,
  "timestamp": "2026-03-08T10:00:00Z"
}
```

## 에러 코드 정리

### 공통 존재/버전 검증

| 에러 코드 | HTTP | 설명 |
| --- | --- | --- |
| `TASK_NOT_FOUND` | `404` | 대상 업무가 없음 |
| `REVIEW_NOT_FOUND` | `404` | 대상 검토가 없음 |
| `REVIEW_VERSION_CONFLICT` | `409` | 리뷰 낙관적 락 충돌 |
| `NOT_IMPLEMENTED` | `501` | 현재 문서/스켈레톤만 있고 로직 미구현 |

### 상태 위반

| 에러 코드 | HTTP | 설명 |
| --- | --- | --- |
| `REVIEW_SUBMIT_NOT_ALLOWED` | `409` | 현재 업무 상태에서 검토 생성 불가 |
| `REVIEW_RESUBMISSION_NOT_ALLOWED` | `409` | 재상신 전제 조건 미충족 |
| `REVIEW_ALREADY_SUBMITTED_FOR_TASK_VERSION` | `409` | 같은 업무에 유효한 `SUBMITTED` 검토가 이미 존재 |
| `REVIEW_UPDATE_NOT_ALLOWED` | `409` | 현재 상태에서 검토 본문 수정 불가 |
| `REVIEW_APPROVAL_NOT_ALLOWED` | `409` | 현재 상태에서 승인 불가 |
| `REVIEW_REJECTION_NOT_ALLOWED` | `409` | 현재 상태에서 반려 불가 |
| `REVIEW_CANCEL_NOT_ALLOWED` | `409` | 현재 상태에서 취소 불가 |
| `REFERENCE_ASSIGN_NOT_ALLOWED` | `409` | 현재 상태에서 참조자 추가 불가 |
| `REFERENCE_UNASSIGN_NOT_ALLOWED` | `409` | 현재 상태에서 참조자 해제 불가 |
| `REFERENCE_ALREADY_ASSIGNED` | `409` | 동일 사용자가 이미 참조자로 등록됨 |
| `ATTACHMENT_ADD_NOT_ALLOWED` | `409` | 현재 상태에서 첨부 추가 불가 |
| `ATTACHMENT_REMOVE_NOT_ALLOWED` | `409` | 현재 상태에서 첨부 해제 불가 |
| `ADDITIONAL_REVIEWER_ASSIGN_NOT_ALLOWED` | `409` | 현재 상태에서 추가 검토자 할당 불가 |
| `ADDITIONAL_REVIEWER_UNASSIGN_NOT_ALLOWED` | `409` | 현재 상태에서 추가 검토자 할당 취소 불가 |
| `ADDITIONAL_REVIEWER_ALREADY_ASSIGNED` | `409` | 동일 사용자가 이미 추가 검토자로 등록됨 |
| `COMMENT_CREATE_NOT_ALLOWED` | `409` | 현재 상태에서 코멘트 작성 불가 |
| `COMMENT_UPDATE_NOT_ALLOWED` | `409` | 현재 상태에서 코멘트 수정 불가 |
| `COMMENT_DELETE_NOT_ALLOWED` | `409` | 현재 상태에서 코멘트 삭제 불가 |
| `REJECTION_REASON_REQUIRED` | `400` | 반려 사유 누락 |

### 권한 위반

| 에러 코드 | HTTP | 설명 |
| --- | --- | --- |
| `REVIEW_SUBMIT_FORBIDDEN` | `403` | 검토 생성 권한 없음 |
| `REVIEW_RESUBMISSION_FORBIDDEN` | `403` | 재상신 권한 없음 |
| `REVIEW_UPDATE_FORBIDDEN` | `403` | 검토 수정 권한 없음 |
| `REVIEW_APPROVAL_FORBIDDEN` | `403` | 검토 승인 권한 없음 |
| `REVIEW_REJECTION_FORBIDDEN` | `403` | 검토 반려 권한 없음 |
| `REVIEW_CANCEL_FORBIDDEN` | `403` | 검토 취소 권한 없음 |
| `REFERENCE_ASSIGN_FORBIDDEN` | `403` | 참조자 추가 권한 없음 |
| `REFERENCE_UNASSIGN_FORBIDDEN` | `403` | 참조자 해제 권한 없음 |
| `ATTACHMENT_ADD_FORBIDDEN` | `403` | 첨부 추가 권한 없음 |
| `ATTACHMENT_REMOVE_FORBIDDEN` | `403` | 첨부 해제 권한 없음 |
| `ADDITIONAL_REVIEWER_ASSIGN_FORBIDDEN` | `403` | 추가 검토자 할당 권한 없음 |
| `ADDITIONAL_REVIEWER_UNASSIGN_FORBIDDEN` | `403` | 추가 검토자 할당 취소 권한 없음 |
| `COMMENT_CREATE_FORBIDDEN` | `403` | 코멘트 작성 권한 없음 |
| `COMMENT_UPDATE_FORBIDDEN` | `403` | 코멘트 수정 권한 없음 |
| `COMMENT_DELETE_FORBIDDEN` | `403` | 코멘트 삭제 권한 없음 |

## 권한 주체 정의

| 주체 | 설명 |
| --- | --- |
| 작성자 | 업무 작성자이자 검토 상신 주체 |
| 참조자 | 검토를 조회하고 코멘트를 남길 수 있는 사용자 |
| 기본 검토 권한자 | 원래 승인/반려 권한을 가진 사용자 |
| 추가 검토자 | 원래 권한은 없지만 특정 검토에 한해 승인/반려 권한을 부여받은 사용자 |
| 관리자 예외 권한자 | 운영상 정정 또는 예외 처리를 수행할 수 있는 관리자 |

## 구현 계획 메모

- 서비스 메서드 초입에서 `상태 검증 -> 권한 검증 -> 버전 검증` 순서로 처리한다.
- 권한 데이터 소스는 다른 팀 구현을 사용하되, 우리 쪽 서비스는 `ReviewAuthorizationPort` 같은 인터페이스를 통해 추상화한다.
- 승인/반려 권한 검증은 `기본 검토 권한자 또는 추가 검토자` 조건을 모두 확인해야 한다.
- 참조자는 승인/반려 권한이 없고, 코멘트 작성 권한만 가진다.
- `검토자료 첨부 확정`은 1차에서는 S3 object existence check 없이 업로드 완료를 전제로 메타데이터를 등록한다.
- `첨부 해제`는 S3 원본 즉시 삭제가 아니라 DB 연결 제거 기준으로 처리한다.
