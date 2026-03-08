# 검토 도메인 문서

- [도메인 정책](./review-policy.md)
- [ERD](./review-erd.md)
- [테이블 명세](./review-table-spec.md)
- [액션 API 명세](./review-action-api-spec.md)

## 현재 기준 요약

- 업무에는 비즈니스 버전 개념이 없다.
- 검토는 `roundNo`로 N번째 검토 사이클을 구분한다.
- 동시성 제어는 `lockVersion`으로만 처리한다.
- 한 업무에는 동시에 `SUBMITTED` 상태의 검토가 하나만 존재할 수 있다.

## 문서 역할

- `review-policy.md`: 검토 도메인 정책 원문 보관
- `review-erd.md`: 관계와 상태 흐름 요약
- `review-table-spec.md`: 테이블/컬럼/인덱스 명세
- `review-action-api-spec.md`: 액션 API 요청/응답/권한/에러 명세
