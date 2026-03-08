# 검토 액션 API 명세서

## 1. 공통 규약

### 공통 Request Header

| key | value | 설명 |
| --- | --- | --- |
| `Authorization` | `Bearer {accessToken}` | 사용자 인증 및 권한 판별 |
| `Content-Type` | `application/json` | Body가 있는 요청 공통 |
| `If-Match` | `{lockVersion}` | 리뷰 본체 변경 API의 낙관적 락 검증 |

### 공통 Response Header

| key | value |
| --- | --- |
| `Content-Type` | `application/json;charset=UTF-8` |

### 공통 성공 응답

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Review submitted.",
  "data": {},
  "timestamp": "2026-03-08T10:00:00Z"
}
```

### 공통 실패 응답

```json
{
  "code": "REVIEW_APPROVAL_FORBIDDEN",
  "message": "User is not allowed to approve this review.",
  "timestamp": "2026-03-08T10:00:00Z",
  "path": "/api/v1/reviews/3001/approve"
}
```

## 2. 권한 주체 정의

| 주체 | 설명 |
| --- | --- |
| 작성자 | 업무 작성자이자 검토 상신 주체 |
| 참조자 | 검토 조회 및 코멘트 작성 가능 |
| 기본 검토 권한자 | 원래 승인/반려 권한을 가진 사용자 |
| 추가 검토자 | 특정 review에 한해 승인/반려 권한을 부여받은 사용자 |
| 관리자 예외 권한자 | 운영상 예외 수정/처리가 가능한 관리자 |

## 3. 액션 API 목록

| 기능 | Method | Path | 상태 조건 | 권한 조건 | 비고 |
| --- | --- | --- | --- | --- | --- |
| 검토 생성 | `POST` | `/api/v1/tasks/{taskId}/reviews` | 업무 시작 상태 `IN_PROGRESS` | `REVIEW_SUBMIT` 또는 작성자 또는 `ADMIN_OVERRIDE` | 처리 결과로 업무는 `IN_REVIEW`, 검토는 `SUBMITTED` |
| 검토 재상신 | `POST` | `/api/v1/tasks/{taskId}/reviews` | 업무 시작 상태 `IN_PROGRESS` + 기존 `REJECTED` 검토 존재 | `REVIEW_SUBMIT` 또는 작성자 또는 `ADMIN_OVERRIDE` | 생성과 같은 엔드포인트 사용 |
| 검토 수정 | `PATCH` | `/api/v1/reviews/{reviewId}` | `SUBMITTED` | `REVIEW_UPDATE` 또는 작성자 또는 `ADMIN_OVERRIDE` | 본문 `content`만 수정 |
| 검토 승인 | `POST` | `/api/v1/reviews/{reviewId}/approve` | `SUBMITTED` | `REVIEW_APPROVE` 또는 추가 검토자 또는 `ADMIN_OVERRIDE` | 참조자는 승인 불가 |
| 검토 반려 | `POST` | `/api/v1/reviews/{reviewId}/reject` | `SUBMITTED` | `REVIEW_REJECT` 또는 추가 검토자 또는 `ADMIN_OVERRIDE` | 참조자는 반려 불가 |
| 검토 취소 | `POST` | `/api/v1/reviews/{reviewId}/cancel` | `SUBMITTED` | `REVIEW_CANCEL` 또는 제출자 또는 `ADMIN_OVERRIDE` | 제출 철회 의미 |
| 참조자 추가 | `POST` | `/api/v1/reviews/{reviewId}/references` | `SUBMITTED` | `REVIEW_REFERENCE_MANAGE` 또는 작성자 또는 `ADMIN_OVERRIDE` | - |
| 참조자 해제 | `DELETE` | `/api/v1/reviews/{reviewId}/references/{userId}` | `SUBMITTED` | `REVIEW_REFERENCE_MANAGE` 또는 작성자 또는 `ADMIN_OVERRIDE` | - |
| 첨부 업로드 URL 발급 | `POST` | `/api/v1/reviews/{reviewId}/attachments/presign` | `SUBMITTED` | `REVIEW_ATTACHMENT_MANAGE` 또는 작성자 또는 `ADMIN_OVERRIDE` | presigned URL 발급 |
| 첨부 확정 | `POST` | `/api/v1/reviews/{reviewId}/attachments` | `SUBMITTED` | `REVIEW_ATTACHMENT_MANAGE` 또는 작성자 또는 `ADMIN_OVERRIDE` | S3 업로드 완료 전제 |
| 첨부 해제 | `DELETE` | `/api/v1/reviews/{reviewId}/attachments/{attachmentId}` | `SUBMITTED` | `REVIEW_ATTACHMENT_MANAGE` 또는 작성자 또는 `ADMIN_OVERRIDE` | DB 연결 해제 기준 |
| 추가 검토자 할당 | `POST` | `/api/v1/reviews/{reviewId}/additional-reviewers` | `SUBMITTED` | `REVIEW_ADDITIONAL_REVIEWER_MANAGE` 또는 작성자 또는 `ADMIN_OVERRIDE` | - |
| 추가 검토자 할당 취소 | `DELETE` | `/api/v1/reviews/{reviewId}/additional-reviewers/{userId}` | `SUBMITTED` | `REVIEW_ADDITIONAL_REVIEWER_MANAGE` 또는 작성자 또는 `ADMIN_OVERRIDE` | - |
| 검토 코멘트 작성 | `POST` | `/api/v1/reviews/{reviewId}/comments` | `SUBMITTED` 또는 `APPROVED` | `REVIEW_COMMENT_CREATE` 또는 작성자/참조자/검토자/추가 검토자/`ADMIN_OVERRIDE` | `REJECTED`에서는 작성 불가 |
| 검토 코멘트 수정 | `PATCH` | `/api/v1/reviews/{reviewId}/comments/{commentId}` | `SUBMITTED` | `REVIEW_COMMENT_UPDATE` 또는 코멘트 작성자 또는 `ADMIN_OVERRIDE` | `APPROVED`, `REJECTED` 수정 불가 |
| 검토 코멘트 삭제 | `DELETE` | `/api/v1/reviews/{reviewId}/comments/{commentId}` | `SUBMITTED` | `REVIEW_COMMENT_DELETE` 또는 코멘트 작성자 또는 `ADMIN_OVERRIDE` | `APPROVED`, `REJECTED` 삭제 불가 |

## 4. 대표 응답 JSON

### 4.1 `ApiResponse<ReviewDetailResponse>`

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

### 4.2 `ApiResponse<AttachmentPresignResponse>`

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

## 5. 기능별 상세

### 5.1 검토 생성 / 검토 재상신

- Method: `POST`
- Path: `/api/v1/tasks/{taskId}/reviews`

#### Request Header

| key | value |
| --- | --- |
| `Authorization` | `Bearer {accessToken}` |
| `Content-Type` | `application/json` |

#### Request Body JSON

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

#### Error Codes

- `TASK_NOT_FOUND`
- `REVIEW_SUBMIT_NOT_ALLOWED`
- `REVIEW_SUBMIT_FORBIDDEN`
- `REVIEW_ALREADY_SUBMITTED_FOR_TASK_VERSION`
- `REVIEW_RESUBMISSION_NOT_ALLOWED`
- `REVIEW_RESUBMISSION_FORBIDDEN`

### 5.2 검토 수정

- Method: `PATCH`
- Path: `/api/v1/reviews/{reviewId}`

#### Request Header

| key | value |
| --- | --- |
| `Authorization` | `Bearer {accessToken}` |
| `Content-Type` | `application/json` |
| `If-Match` | `3` |

#### Request Body JSON

```json
{
  "content": "수정된 검토 본문입니다."
}
```

#### Error Codes

- `REVIEW_NOT_FOUND`
- `REVIEW_UPDATE_NOT_ALLOWED`
- `REVIEW_UPDATE_FORBIDDEN`
- `REVIEW_VERSION_CONFLICT`

### 5.3 검토 승인

- Method: `POST`
- Path: `/api/v1/reviews/{reviewId}/approve`

#### Request Header

| key | value |
| --- | --- |
| `Authorization` | `Bearer {accessToken}` |
| `If-Match` | `3` |

#### Request Body

- 없음

#### Error Codes

- `REVIEW_NOT_FOUND`
- `REVIEW_APPROVAL_NOT_ALLOWED`
- `REVIEW_APPROVAL_FORBIDDEN`
- `REVIEW_VERSION_CONFLICT`

### 5.4 검토 반려

- Method: `POST`
- Path: `/api/v1/reviews/{reviewId}/reject`

#### Request Header

| key | value |
| --- | --- |
| `Authorization` | `Bearer {accessToken}` |
| `Content-Type` | `application/json` |
| `If-Match` | `3` |

#### Request Body JSON

```json
{
  "reason": "필수 첨부 문서가 누락되었습니다."
}
```

#### Error Codes

- `REVIEW_NOT_FOUND`
- `REVIEW_REJECTION_NOT_ALLOWED`
- `REVIEW_REJECTION_FORBIDDEN`
- `REJECTION_REASON_REQUIRED`
- `REVIEW_VERSION_CONFLICT`

### 5.5 검토 취소

- Method: `POST`
- Path: `/api/v1/reviews/{reviewId}/cancel`

#### Request Header

| key | value |
| --- | --- |
| `Authorization` | `Bearer {accessToken}` |
| `Content-Type` | `application/json` |
| `If-Match` | `3` |

#### Request Body JSON

```json
{
  "reason": "본문 보완 후 다시 상신 예정입니다."
}
```

#### Error Codes

- `REVIEW_NOT_FOUND`
- `REVIEW_CANCEL_NOT_ALLOWED`
- `REVIEW_CANCEL_FORBIDDEN`
- `REVIEW_VERSION_CONFLICT`

### 5.6 참조자 추가

- Method: `POST`
- Path: `/api/v1/reviews/{reviewId}/references`

#### Request Header

| key | value |
| --- | --- |
| `Authorization` | `Bearer {accessToken}` |
| `Content-Type` | `application/json` |
| `If-Match` | `3` |

#### Request Body JSON

```json
{
  "userId": 201
}
```

#### Error Codes

- `REVIEW_NOT_FOUND`
- `REFERENCE_ASSIGN_NOT_ALLOWED`
- `REFERENCE_ASSIGN_FORBIDDEN`
- `REFERENCE_ALREADY_ASSIGNED`
- `REVIEW_VERSION_CONFLICT`

### 5.7 참조자 해제

- Method: `DELETE`
- Path: `/api/v1/reviews/{reviewId}/references/{userId}`

#### Request Header

| key | value |
| --- | --- |
| `Authorization` | `Bearer {accessToken}` |
| `If-Match` | `3` |

#### Error Codes

- `REVIEW_NOT_FOUND`
- `REFERENCE_UNASSIGN_NOT_ALLOWED`
- `REFERENCE_UNASSIGN_FORBIDDEN`
- `REVIEW_VERSION_CONFLICT`

### 5.8 첨부 업로드 URL 발급

- Method: `POST`
- Path: `/api/v1/reviews/{reviewId}/attachments/presign`

#### Request Header

| key | value |
| --- | --- |
| `Authorization` | `Bearer {accessToken}` |
| `Content-Type` | `application/json` |
| `If-Match` | `3` |

#### Request Body JSON

```json
{
  "originalName": "spec.pdf",
  "contentType": "application/pdf",
  "sizeBytes": 204800
}
```

#### Error Codes

- `REVIEW_NOT_FOUND`
- `ATTACHMENT_ADD_NOT_ALLOWED`
- `ATTACHMENT_ADD_FORBIDDEN`
- `REVIEW_VERSION_CONFLICT`

### 5.9 첨부 확정

- Method: `POST`
- Path: `/api/v1/reviews/{reviewId}/attachments`

#### Request Header

| key | value |
| --- | --- |
| `Authorization` | `Bearer {accessToken}` |
| `Content-Type` | `application/json` |
| `If-Match` | `3` |

#### Request Body JSON

```json
{
  "objectKey": "reviews/10/files/spec.pdf",
  "originalName": "spec.pdf",
  "contentType": "application/pdf",
  "sizeBytes": 204800,
  "sortOrder": 0
}
```

#### Error Codes

- `REVIEW_NOT_FOUND`
- `ATTACHMENT_ADD_NOT_ALLOWED`
- `ATTACHMENT_ADD_FORBIDDEN`
- `REVIEW_VERSION_CONFLICT`

### 5.10 첨부 해제

- Method: `DELETE`
- Path: `/api/v1/reviews/{reviewId}/attachments/{attachmentId}`

#### Request Header

| key | value |
| --- | --- |
| `Authorization` | `Bearer {accessToken}` |
| `If-Match` | `3` |

#### Error Codes

- `REVIEW_NOT_FOUND`
- `ATTACHMENT_REMOVE_NOT_ALLOWED`
- `ATTACHMENT_REMOVE_FORBIDDEN`
- `REVIEW_VERSION_CONFLICT`

### 5.11 추가 검토자 할당

- Method: `POST`
- Path: `/api/v1/reviews/{reviewId}/additional-reviewers`

#### Request Header

| key | value |
| --- | --- |
| `Authorization` | `Bearer {accessToken}` |
| `Content-Type` | `application/json` |
| `If-Match` | `3` |

#### Request Body JSON

```json
{
  "userId": 301
}
```

#### Error Codes

- `REVIEW_NOT_FOUND`
- `ADDITIONAL_REVIEWER_ASSIGN_NOT_ALLOWED`
- `ADDITIONAL_REVIEWER_ASSIGN_FORBIDDEN`
- `ADDITIONAL_REVIEWER_ALREADY_ASSIGNED`
- `REVIEW_VERSION_CONFLICT`

### 5.12 추가 검토자 할당 취소

- Method: `DELETE`
- Path: `/api/v1/reviews/{reviewId}/additional-reviewers/{userId}`

#### Request Header

| key | value |
| --- | --- |
| `Authorization` | `Bearer {accessToken}` |
| `If-Match` | `3` |

#### Error Codes

- `REVIEW_NOT_FOUND`
- `ADDITIONAL_REVIEWER_UNASSIGN_NOT_ALLOWED`
- `ADDITIONAL_REVIEWER_UNASSIGN_FORBIDDEN`
- `REVIEW_VERSION_CONFLICT`

### 5.13 검토 코멘트 작성

- Method: `POST`
- Path: `/api/v1/reviews/{reviewId}/comments`

#### Request Header

| key | value |
| --- | --- |
| `Authorization` | `Bearer {accessToken}` |
| `Content-Type` | `application/json` |

#### Request Body JSON

```json
{
  "content": "본문 수정 없이 확인 의견만 남깁니다."
}
```

#### Error Codes

- `REVIEW_NOT_FOUND`
- `COMMENT_CREATE_NOT_ALLOWED`
- `COMMENT_CREATE_FORBIDDEN`

### 5.14 검토 코멘트 수정

- Method: `PATCH`
- Path: `/api/v1/reviews/{reviewId}/comments/{commentId}`

#### Request Header

| key | value |
| --- | --- |
| `Authorization` | `Bearer {accessToken}` |
| `Content-Type` | `application/json` |

#### Request Body JSON

```json
{
  "content": "표현을 조금 더 명확하게 수정했습니다."
}
```

#### Error Codes

- `REVIEW_NOT_FOUND`
- `COMMENT_UPDATE_NOT_ALLOWED`
- `COMMENT_UPDATE_FORBIDDEN`

### 5.15 검토 코멘트 삭제

- Method: `DELETE`
- Path: `/api/v1/reviews/{reviewId}/comments/{commentId}`

#### Request Header

| key | value |
| --- | --- |
| `Authorization` | `Bearer {accessToken}` |

#### Error Codes

- `REVIEW_NOT_FOUND`
- `COMMENT_DELETE_NOT_ALLOWED`
- `COMMENT_DELETE_FORBIDDEN`

## 6. 구현 메모

- 서비스 메서드 초입에서 `상태 검증 -> 권한 검증 -> 버전 검증` 순서로 처리한다.
- 권한 데이터 소스는 외부 팀 구현을 사용하되, 우리 쪽 서비스는 `ReviewAuthorizationPort` 같은 인터페이스로 추상화한다.
- 승인/반려 권한 검증은 `기본 검토 권한자 또는 추가 검토자`를 함께 판정해야 한다.
- 참조자는 승인/반려 권한이 없고, 코멘트 작성 권한만 가진다.
- 첨부 확정은 1차에서 S3 object existence check 없이 업로드 완료를 전제로 메타데이터를 등록한다.
- 첨부 해제는 S3 원본 즉시 삭제가 아니라 DB 연결 제거 기준으로 처리한다.
