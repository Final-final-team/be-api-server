# 검토 도메인 API 명세

## 1. 공통 응답 모델

### `ApiResponse<T>`

모든 성공 응답은 글로벌 공통 래퍼 `com.example.workmanagement.global.response.ApiResponse`를 사용한다.

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Review detail fetched.",
  "data": {},
  "timestamp": "2026-03-08T10:00:00Z"
}
```

### `ErrorResponse`

모든 실패 응답은 글로벌 공통 에러 응답 `com.example.workmanagement.global.response.ErrorResponse`를 사용한다.

```json
{
  "code": "REVIEW_VERSION_CONFLICT",
  "message": "Review version conflict detected.",
  "timestamp": "2026-03-08T10:00:00Z",
  "path": "/api/v1/reviews/3001/approve"
}
```

### 공통 규약

- Base URL: `/api/v1`
- 인증 주체: 1차 MVP에서는 미구현. 실제 구현 시 인증 컨텍스트에서 `actor`를 추출한다.
- 리뷰 본체 변경 API는 `If-Match: {reviewVersion}` 헤더를 필수로 받는다.
- 코멘트 API는 리뷰 상태 검증만 수행하고 `If-Match`는 받지 않는다.

### 공통 에러 코드

| 코드 | HTTP | 의미 |
| --- | --- | --- |
| `TASK_NOT_FOUND` | 404 | 대상 업무 없음 |
| `REVIEW_NOT_FOUND` | 404 | 대상 검토 없음 |
| `INVALID_TASK_STATUS_FOR_REVIEW` | 409 | 업무가 `IN_REVIEW`가 아님 |
| `REVIEW_STATUS_CONFLICT` | 409 | 현재 리뷰 상태에서 허용되지 않는 액션 |
| `REVIEW_VERSION_CONFLICT` | 409 | 리뷰 낙관적 락 충돌 |
| `TASK_VERSION_CONFLICT` | 409 | 승인 시 업무 버전 불일치 |
| `REJECTION_REASON_REQUIRED` | 400 | 반려 사유 누락 |
| `REFERENCE_MUTATION_FORBIDDEN` | 409 | 참조자 변경 불가 상태 |
| `ATTACHMENT_MUTATION_FORBIDDEN` | 409 | 첨부 변경 불가 상태 |
| `COMMENT_MUTATION_FORBIDDEN` | 409 | 코멘트 변경 불가 상태 |
| `NOT_IMPLEMENTED` | 501 | 현재 뼈대만 있고 실제 도메인 로직 미구현 |

## 2. 대표 응답 데이터 형태

### `ReviewSummaryResponse[]`

```json
[
  {
    "reviewId": 3001,
    "taskId": 10,
    "taskVersionNo": 3,
    "roundNo": 2,
    "status": "SUBMITTED",
    "reviewVersion": 0,
    "submittedAt": "2026-03-08T10:00:00Z",
    "decidedAt": null
  }
]
```

### `ReviewDetailResponse`

```json
{
  "reviewId": 3001,
  "taskId": 10,
  "taskVersionNo": 3,
  "roundNo": 2,
  "status": "SUBMITTED",
  "content": "검토 요청 본문",
  "rejectionReason": null,
  "submittedBy": 101,
  "decidedBy": null,
  "cancelledBy": null,
  "reviewVersion": 0,
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
      "content": "본문 수정 없이 참고 의견만 남깁니다.",
      "edited": false,
      "editedAt": null,
      "createdAt": "2026-03-08T10:10:00Z",
      "deletedAt": null
    }
  ]
}
```

### `ReviewAttachmentPresignResponse`

```json
{
  "objectKey": "reviews/10/files/spec.pdf",
  "uploadUrl": "https://storage.example.com/presigned-url",
  "expiresAt": "2026-03-08T10:15:00Z"
}
```

### `ReviewHistoryResponse[]`

```json
[
  {
    "historyId": 1,
    "actionType": "REVIEW_CREATED",
    "targetType": "REVIEW",
    "targetId": 3001,
    "actorId": 101,
    "reason": null,
    "metadataJson": "{\"roundNo\":2}",
    "occurredAt": "2026-03-08T10:00:00Z"
  }
]
```

## 3. 엔드포인트 상세

### 3.1 검토 생성/재상신

- Method: `POST`
- Path: `/api/v1/tasks/{taskId}/reviews`

#### Request JSON

```json
{
  "taskVersionNo": 3,
  "content": "검토 요청 본문",
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

#### Response JSON

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Review submitted.",
  "data": {
    "reviewId": 3001,
    "taskId": 10,
    "taskVersionNo": 3,
    "roundNo": 2,
    "status": "SUBMITTED",
    "content": "검토 요청 본문",
    "rejectionReason": null,
    "submittedBy": 101,
    "decidedBy": null,
    "cancelledBy": null,
    "reviewVersion": 0,
    "submittedAt": "2026-03-08T10:00:00Z",
    "decidedAt": null,
    "cancelledAt": null,
    "references": [],
    "attachments": [],
    "comments": []
  },
  "timestamp": "2026-03-08T10:00:00Z"
}
```

#### 오류 코드

- `TASK_NOT_FOUND`
- `INVALID_TASK_STATUS_FOR_REVIEW`
- `REVIEW_STATUS_CONFLICT`
- `NOT_IMPLEMENTED`

### 3.2 업무별 검토 목록 조회

- Method: `GET`
- Path: `/api/v1/tasks/{taskId}/reviews`

#### Request JSON

- 없음

#### Response JSON

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Task reviews fetched.",
  "data": [
    {
      "reviewId": 3001,
      "taskId": 10,
      "taskVersionNo": 3,
      "roundNo": 2,
      "status": "SUBMITTED",
      "reviewVersion": 0,
      "submittedAt": "2026-03-08T10:00:00Z",
      "decidedAt": null
    }
  ],
  "timestamp": "2026-03-08T10:00:00Z"
}
```

#### 오류 코드

- `TASK_NOT_FOUND`
- `NOT_IMPLEMENTED`

### 3.3 검토 상세 조회

- Method: `GET`
- Path: `/api/v1/reviews/{reviewId}`

#### Request JSON

- 없음

#### Response JSON

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Review detail fetched.",
  "data": {
    "reviewId": 3001,
    "taskId": 10,
    "taskVersionNo": 3,
    "roundNo": 2,
    "status": "SUBMITTED",
    "content": "검토 요청 본문",
    "rejectionReason": null,
    "submittedBy": 101,
    "decidedBy": null,
    "cancelledBy": null,
    "reviewVersion": 0,
    "submittedAt": "2026-03-08T10:00:00Z",
    "decidedAt": null,
    "cancelledAt": null,
    "references": [],
    "attachments": [],
    "comments": []
  },
  "timestamp": "2026-03-08T10:00:00Z"
}
```

#### 오류 코드

- `REVIEW_NOT_FOUND`
- `NOT_IMPLEMENTED`

### 3.4 검토 승인

- Method: `POST`
- Path: `/api/v1/reviews/{reviewId}/approve`
- Header: `If-Match: {reviewVersion}`

#### Request JSON

- 없음

#### Response JSON

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Review approved.",
  "data": {
    "reviewId": 3001,
    "taskId": 10,
    "taskVersionNo": 3,
    "roundNo": 2,
    "status": "APPROVED",
    "content": "검토 요청 본문",
    "rejectionReason": null,
    "submittedBy": 101,
    "decidedBy": 301,
    "cancelledBy": null,
    "reviewVersion": 1,
    "submittedAt": "2026-03-08T10:00:00Z",
    "decidedAt": "2026-03-08T11:00:00Z",
    "cancelledAt": null,
    "references": [],
    "attachments": [],
    "comments": []
  },
  "timestamp": "2026-03-08T11:00:00Z"
}
```

#### 오류 코드

- `REVIEW_NOT_FOUND`
- `REVIEW_STATUS_CONFLICT`
- `REVIEW_VERSION_CONFLICT`
- `TASK_VERSION_CONFLICT`
- `NOT_IMPLEMENTED`

### 3.5 검토 반려

- Method: `POST`
- Path: `/api/v1/reviews/{reviewId}/reject`
- Header: `If-Match: {reviewVersion}`

#### Request JSON

```json
{
  "reason": "결재 근거 문서가 누락되었습니다."
}
```

#### Response JSON

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Review rejected.",
  "data": {
    "reviewId": 3001,
    "taskId": 10,
    "taskVersionNo": 3,
    "roundNo": 2,
    "status": "REJECTED",
    "content": "검토 요청 본문",
    "rejectionReason": "결재 근거 문서가 누락되었습니다.",
    "submittedBy": 101,
    "decidedBy": 301,
    "cancelledBy": null,
    "reviewVersion": 1,
    "submittedAt": "2026-03-08T10:00:00Z",
    "decidedAt": "2026-03-08T11:00:00Z",
    "cancelledAt": null,
    "references": [],
    "attachments": [],
    "comments": []
  },
  "timestamp": "2026-03-08T11:00:00Z"
}
```

#### 오류 코드

- `REVIEW_NOT_FOUND`
- `REJECTION_REASON_REQUIRED`
- `REVIEW_STATUS_CONFLICT`
- `REVIEW_VERSION_CONFLICT`
- `NOT_IMPLEMENTED`

### 3.6 검토 취소

- Method: `POST`
- Path: `/api/v1/reviews/{reviewId}/cancel`
- Header: `If-Match: {reviewVersion}`

#### Request JSON

```json
{
  "reason": "업무 본문 보완 후 다시 올릴 예정입니다."
}
```

#### Response JSON

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Review cancelled.",
  "data": {
    "reviewId": 3001,
    "taskId": 10,
    "taskVersionNo": 3,
    "roundNo": 2,
    "status": "CANCELLED",
    "content": "검토 요청 본문",
    "rejectionReason": null,
    "submittedBy": 101,
    "decidedBy": null,
    "cancelledBy": 101,
    "reviewVersion": 1,
    "submittedAt": "2026-03-08T10:00:00Z",
    "decidedAt": null,
    "cancelledAt": "2026-03-08T10:30:00Z",
    "references": [],
    "attachments": [],
    "comments": []
  },
  "timestamp": "2026-03-08T10:30:00Z"
}
```

#### 오류 코드

- `REVIEW_NOT_FOUND`
- `REVIEW_STATUS_CONFLICT`
- `REVIEW_VERSION_CONFLICT`
- `NOT_IMPLEMENTED`

### 3.7 참조자 추가

- Method: `POST`
- Path: `/api/v1/reviews/{reviewId}/references`
- Header: `If-Match: {reviewVersion}`

#### Request JSON

```json
{
  "userId": 201
}
```

#### Response JSON

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Review reference assigned.",
  "data": {
    "reviewId": 3001,
    "taskId": 10,
    "taskVersionNo": 3,
    "roundNo": 2,
    "status": "SUBMITTED",
    "content": "검토 요청 본문",
    "rejectionReason": null,
    "submittedBy": 101,
    "decidedBy": null,
    "cancelledBy": null,
    "reviewVersion": 1,
    "submittedAt": "2026-03-08T10:00:00Z",
    "decidedAt": null,
    "cancelledAt": null,
    "references": [
      {
        "userId": 201,
        "addedBy": 101,
        "createdAt": "2026-03-08T10:05:00Z"
      }
    ],
    "attachments": [],
    "comments": []
  },
  "timestamp": "2026-03-08T10:05:00Z"
}
```

#### 오류 코드

- `REVIEW_NOT_FOUND`
- `REFERENCE_MUTATION_FORBIDDEN`
- `REVIEW_VERSION_CONFLICT`
- `NOT_IMPLEMENTED`

### 3.8 참조자 제거

- Method: `DELETE`
- Path: `/api/v1/reviews/{reviewId}/references/{userId}`
- Header: `If-Match: {reviewVersion}`

#### Request JSON

- 없음

#### Response JSON

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Review reference removed.",
  "data": {
    "reviewId": 3001,
    "taskId": 10,
    "taskVersionNo": 3,
    "roundNo": 2,
    "status": "SUBMITTED",
    "content": "검토 요청 본문",
    "rejectionReason": null,
    "submittedBy": 101,
    "decidedBy": null,
    "cancelledBy": null,
    "reviewVersion": 2,
    "submittedAt": "2026-03-08T10:00:00Z",
    "decidedAt": null,
    "cancelledAt": null,
    "references": [],
    "attachments": [],
    "comments": []
  },
  "timestamp": "2026-03-08T10:06:00Z"
}
```

#### 오류 코드

- `REVIEW_NOT_FOUND`
- `REFERENCE_MUTATION_FORBIDDEN`
- `REVIEW_VERSION_CONFLICT`
- `NOT_IMPLEMENTED`

### 3.9 첨부 Presign URL 발급

- Method: `POST`
- Path: `/api/v1/reviews/{reviewId}/attachments/presign`
- Header: `If-Match: {reviewVersion}`

#### Request JSON

```json
{
  "originalName": "spec.pdf",
  "contentType": "application/pdf",
  "sizeBytes": 204800
}
```

#### Response JSON

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Attachment presign URL created.",
  "data": {
    "objectKey": "reviews/10/files/spec.pdf",
    "uploadUrl": "https://storage.example.com/presigned-url",
    "expiresAt": "2026-03-08T10:15:00Z"
  },
  "timestamp": "2026-03-08T10:00:00Z"
}
```

#### 오류 코드

- `REVIEW_NOT_FOUND`
- `ATTACHMENT_MUTATION_FORBIDDEN`
- `REVIEW_VERSION_CONFLICT`
- `NOT_IMPLEMENTED`

### 3.10 첨부 확정

- Method: `POST`
- Path: `/api/v1/reviews/{reviewId}/attachments`
- Header: `If-Match: {reviewVersion}`

#### Request JSON

```json
{
  "objectKey": "reviews/10/files/spec.pdf",
  "originalName": "spec.pdf",
  "contentType": "application/pdf",
  "sizeBytes": 204800,
  "sortOrder": 0
}
```

#### Response JSON

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Review attachment confirmed.",
  "data": {
    "reviewId": 3001,
    "taskId": 10,
    "taskVersionNo": 3,
    "roundNo": 2,
    "status": "SUBMITTED",
    "content": "검토 요청 본문",
    "rejectionReason": null,
    "submittedBy": 101,
    "decidedBy": null,
    "cancelledBy": null,
    "reviewVersion": 1,
    "submittedAt": "2026-03-08T10:00:00Z",
    "decidedAt": null,
    "cancelledAt": null,
    "references": [],
    "attachments": [
      {
        "attachmentId": 4001,
        "objectKey": "reviews/10/files/spec.pdf",
        "originalName": "spec.pdf",
        "contentType": "application/pdf",
        "sizeBytes": 204800,
        "sortOrder": 0,
        "createdAt": "2026-03-08T10:03:00Z"
      }
    ],
    "comments": []
  },
  "timestamp": "2026-03-08T10:03:00Z"
}
```

#### 오류 코드

- `REVIEW_NOT_FOUND`
- `ATTACHMENT_MUTATION_FORBIDDEN`
- `REVIEW_VERSION_CONFLICT`
- `NOT_IMPLEMENTED`

### 3.11 첨부 삭제

- Method: `DELETE`
- Path: `/api/v1/reviews/{reviewId}/attachments/{attachmentId}`
- Header: `If-Match: {reviewVersion}`

#### Request JSON

- 없음

#### Response JSON

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Review attachment deleted.",
  "data": {
    "reviewId": 3001,
    "taskId": 10,
    "taskVersionNo": 3,
    "roundNo": 2,
    "status": "SUBMITTED",
    "content": "검토 요청 본문",
    "rejectionReason": null,
    "submittedBy": 101,
    "decidedBy": null,
    "cancelledBy": null,
    "reviewVersion": 2,
    "submittedAt": "2026-03-08T10:00:00Z",
    "decidedAt": null,
    "cancelledAt": null,
    "references": [],
    "attachments": [],
    "comments": []
  },
  "timestamp": "2026-03-08T10:04:00Z"
}
```

#### 오류 코드

- `REVIEW_NOT_FOUND`
- `ATTACHMENT_MUTATION_FORBIDDEN`
- `REVIEW_VERSION_CONFLICT`
- `NOT_IMPLEMENTED`

### 3.12 코멘트 생성

- Method: `POST`
- Path: `/api/v1/reviews/{reviewId}/comments`

#### Request JSON

```json
{
  "content": "본문 수정 없이 참고 의견만 남깁니다."
}
```

#### Response JSON

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Review comment created.",
  "data": {
    "reviewId": 3001,
    "taskId": 10,
    "taskVersionNo": 3,
    "roundNo": 2,
    "status": "APPROVED",
    "content": "검토 요청 본문",
    "rejectionReason": null,
    "submittedBy": 101,
    "decidedBy": 301,
    "cancelledBy": null,
    "reviewVersion": 1,
    "submittedAt": "2026-03-08T10:00:00Z",
    "decidedAt": "2026-03-08T11:00:00Z",
    "cancelledAt": null,
    "references": [],
    "attachments": [],
    "comments": [
      {
        "commentId": 5001,
        "authorId": 101,
        "content": "본문 수정 없이 참고 의견만 남깁니다.",
        "edited": false,
        "editedAt": null,
        "createdAt": "2026-03-08T11:10:00Z",
        "deletedAt": null
      }
    ]
  },
  "timestamp": "2026-03-08T11:10:00Z"
}
```

#### 오류 코드

- `REVIEW_NOT_FOUND`
- `COMMENT_MUTATION_FORBIDDEN`
- `NOT_IMPLEMENTED`

### 3.13 코멘트 수정

- Method: `PATCH`
- Path: `/api/v1/reviews/{reviewId}/comments/{commentId}`

#### Request JSON

```json
{
  "content": "표현만 다듬었습니다."
}
```

#### Response JSON

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Review comment updated.",
  "data": {
    "reviewId": 3001,
    "taskId": 10,
    "taskVersionNo": 3,
    "roundNo": 2,
    "status": "SUBMITTED",
    "content": "검토 요청 본문",
    "rejectionReason": null,
    "submittedBy": 101,
    "decidedBy": null,
    "cancelledBy": null,
    "reviewVersion": 0,
    "submittedAt": "2026-03-08T10:00:00Z",
    "decidedAt": null,
    "cancelledAt": null,
    "references": [],
    "attachments": [],
    "comments": [
      {
        "commentId": 5001,
        "authorId": 101,
        "content": "표현만 다듬었습니다.",
        "edited": true,
        "editedAt": "2026-03-08T10:11:00Z",
        "createdAt": "2026-03-08T10:10:00Z",
        "deletedAt": null
      }
    ]
  },
  "timestamp": "2026-03-08T10:11:00Z"
}
```

#### 오류 코드

- `REVIEW_NOT_FOUND`
- `COMMENT_MUTATION_FORBIDDEN`
- `NOT_IMPLEMENTED`

### 3.14 코멘트 삭제

- Method: `DELETE`
- Path: `/api/v1/reviews/{reviewId}/comments/{commentId}`

#### Request JSON

- 없음

#### Response JSON

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Review comment deleted.",
  "data": {
    "reviewId": 3001,
    "taskId": 10,
    "taskVersionNo": 3,
    "roundNo": 2,
    "status": "SUBMITTED",
    "content": "검토 요청 본문",
    "rejectionReason": null,
    "submittedBy": 101,
    "decidedBy": null,
    "cancelledBy": null,
    "reviewVersion": 0,
    "submittedAt": "2026-03-08T10:00:00Z",
    "decidedAt": null,
    "cancelledAt": null,
    "references": [],
    "attachments": [],
    "comments": []
  },
  "timestamp": "2026-03-08T10:12:00Z"
}
```

#### 오류 코드

- `REVIEW_NOT_FOUND`
- `COMMENT_MUTATION_FORBIDDEN`
- `NOT_IMPLEMENTED`

### 3.15 감사 로그 조회

- Method: `GET`
- Path: `/api/v1/reviews/{reviewId}/histories`

#### Request JSON

- 없음

#### Response JSON

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Review histories fetched.",
  "data": [
    {
      "historyId": 1,
      "actionType": "REVIEW_CREATED",
      "targetType": "REVIEW",
      "targetId": 3001,
      "actorId": 101,
      "reason": null,
      "metadataJson": "{\"roundNo\":2}",
      "occurredAt": "2026-03-08T10:00:00Z"
    }
  ],
  "timestamp": "2026-03-08T10:20:00Z"
}
```

#### 오류 코드

- `REVIEW_NOT_FOUND`
- `NOT_IMPLEMENTED`

## 4. 테스트 시나리오

1. `IN_REVIEW`가 아닌 업무에 검토 생성 요청 시 `409 INVALID_TASK_STATUS_FOR_REVIEW`
2. 동일 `task_id + task_version_no`에 `SUBMITTED` 검토가 이미 있으면 생성 실패
3. `REJECTED` 이후 재상신 시 새 리뷰 row와 증가한 `roundNo` 생성
4. 승인 시 `If-Match` 충돌이면 `409 REVIEW_VERSION_CONFLICT`
5. 승인 시 업무 버전이 다르면 `409 TASK_VERSION_CONFLICT`
6. 반려 시 `reason` 없으면 `400 REJECTION_REASON_REQUIRED`
7. 취소 시 `SUBMITTED`가 아니면 `409 REVIEW_STATUS_CONFLICT`
8. `APPROVED` 상태에서 참조자/첨부 변경 시 `409`
9. `APPROVED` 상태에서 신규 코멘트 작성은 성공, 수정/삭제는 실패
10. `REJECTED` 상태에서 코멘트 생성/수정/삭제 모두 실패

## 5. Future APIs

- `POST /api/v1/reviews/{reviewId}/extra-reviewers`
- `DELETE /api/v1/reviews/{reviewId}/extra-reviewers/{userId}`
- `PATCH /api/v1/reviews/{reviewId}/admin-overrides`
- `POST /api/v1/reviews/{reviewId}/templates`
