# 검토 도메인 문서 맵

이 디렉터리는 `검토(review)` 도메인의 1차 MVP 기준 문서를 담는다. 현재 코드는 스프링부트 3.3.5, JDK 17, Gradle Groovy 기준으로 뼈대만 생성되어 있으며, 컨트롤러와 서비스는 계약을 고정한 상태에서 `501 Not Implemented`를 반환하도록 두었다.

## 패키지 트리

```text
src/main/java/com/example/workmanagement
├── WorkManagementApplication.java
├── domain
│   ├── member
│   │   └── entity
│   │       └── Member.java
│   ├── review
│   │   ├── controller
│   │   ├── dto
│   │   ├── entity
│   │   ├── enums
│   │   ├── event
│   │   ├── exception
│   │   ├── repository
│   │   └── service
│   └── task
│       └── entity
│           ├── Task.java
│           └── TaskStatus.java
├── global
│   ├── config
│   ├── error
│   └── response
└── infrastructure
    ├── audit
    └── storage
```

## 상태 전이 요약

| 트리거 | 리뷰 상태 | 업무 상태 |
| --- | --- | --- |
| 최초 상신/재상신 | `SUBMITTED` 생성 | `IN_REVIEW` 유지 |
| 승인 | `SUBMITTED -> APPROVED` | `COMPLETED` |
| 반려 | `SUBMITTED -> REJECTED` | `IN_PROGRESS` |
| 취소 | `SUBMITTED -> CANCELLED` | `IN_PROGRESS` |

## 문서 구성

- `review-erd.md`: ERD, 관계, 인덱스, 유니크 제약
- `review-domain-model.md`: 애그리거트, 상태 머신, 테이블 명세, 락 정책
- `review-api-spec.md`: 엔드포인트, 요청/응답 규약, 에러 코드, 후속 확장 API

## 구현 메모

- 1차 MVP 범위는 검토 생성/조회/승인/반려/취소, 참조자, 첨부, 코멘트, 감사 로그까지다.
- 인증/인가 자체는 아직 구현하지 않으므로 `actor`는 향후 인증 컨텍스트에서 해석하는 것으로 두고 API 바디에는 넣지 않았다.
- `추가 검토자`, `관리자 예외 수정`, 알림/실시간, 템플릿은 후속 확장 영역으로 분리했다.

