# Review Storage And Paging Plan

## Requirements Summary

- Scope is limited to `review`-owned implementation work.
- Keep the temporary `X-Actor-*` header based auth flow for now.
- Do not change global role grant/revoke flow or global authorization wiring in this pass.
- Prefer not to touch other domains unless review attachment flow or compile restoration strictly requires it.
- Primary goal is forward implementation plus clean compilation. Full unit/integration test execution is deferred.
- Review the implementation against `/mnt/c/Users/alswl/Desktop/policy-consolidation.md` during the work, especially the upload policy.

## Current Facts

- `ReviewCommandService` already depends on `StoragePresignService` and `AuditLogger`, but only temporary interfaces exist:
  - [ReviewCommandService.java](/mnt/c/Users/alswl/Desktop/final/src/main/java/com/example/workmanagement/domain/review/service/ReviewCommandService.java)
  - [StoragePresignService.java](/mnt/c/Users/alswl/Desktop/final/src/main/java/com/example/workmanagement/domain/review/service/StoragePresignService.java)
  - [AuditLogger.java](/mnt/c/Users/alswl/Desktop/final/src/main/java/com/example/workmanagement/domain/review/service/AuditLogger.java)
- Attachment upload exists only as upload-presign plus confirm; download-presign is missing:
  - [ReviewAttachmentController.java](/mnt/c/Users/alswl/Desktop/final/src/main/java/com/example/workmanagement/domain/review/controller/ReviewAttachmentController.java)
- Review list/history endpoints still return raw lists and contain TODOs for paging:
  - [ReviewController.java](/mnt/c/Users/alswl/Desktop/final/src/main/java/com/example/workmanagement/domain/review/controller/ReviewController.java)
  - [ReviewHistoryController.java](/mnt/c/Users/alswl/Desktop/final/src/main/java/com/example/workmanagement/domain/review/controller/ReviewHistoryController.java)
  - [ReviewQueryService.java](/mnt/c/Users/alswl/Desktop/final/src/main/java/com/example/workmanagement/domain/review/service/ReviewQueryService.java)
- Current merged tree is not compile-clean because `ReviewCommandService` and `ReviewQueryService` still expect task integration wiring that no longer matches the tree:
  - [ReviewCommandService.java](/mnt/c/Users/alswl/Desktop/final/src/main/java/com/example/workmanagement/domain/review/service/ReviewCommandService.java)
  - [ReviewQueryService.java](/mnt/c/Users/alswl/Desktop/final/src/main/java/com/example/workmanagement/domain/review/service/ReviewQueryService.java)
  - [Task.java](/mnt/c/Users/alswl/Desktop/final/src/main/java/com/example/workmanagement/domain/task/domain/model/Task.java)

## Policy Mapping

- Backend must issue presigned URLs and frontend uploads directly to S3.
- Backend must generate the object key.
- Backend must validate file name, MIME type, and size independently.
- Upload and domain attachment confirmation should remain separated.
- Download should also stay backend-mediated via a presigned URL.
- This pass remains review-local, so the code should be written to be replaceable by a later global upload module.

## Acceptance Criteria

- `review` attachment upload uses a real S3-backed presign implementation with backend-generated object keys.
- `review` attachment confirmation re-validates the uploaded S3 object metadata before persisting the attachment row.
- `review` attachment download endpoint exists and returns a download presigned URL.
- Attachment delete removes both the review attachment row and the stored object when possible.
- Audit logging records standardized metadata keys across review history events.
- Review list and review history endpoints return normalized paged responses.
- Temporary header auth remains untouched.
- `./gradlew compileJava` completes successfully.

## Implementation Steps

1. Restore compile baseline with the real task model.
   - Point `review` back to the actual `Task` aggregate and `TaskRepository`.
   - Add only the minimum review-related task state transitions needed by the current review flow.
   - Keep the task change narrow and documented.

2. Add review-local storage wiring.
   - Add review storage properties/config under the `review.service` package.
   - Add an S3-backed `StoragePresignService` implementation.
   - Add a disabled fallback implementation so compilation and local boot do not require real AWS credentials.
   - Extend the storage port to support upload presign, download presign, head metadata lookup, and delete.

3. Implement attachment flow changes.
   - Update attachment presign generation to use backend-generated keys only.
   - Update attachment confirmation to validate actual object metadata from S3.
   - Add attachment download endpoint and query/service result model.
   - Delete object from storage on attachment removal.
   - Apply the same storage metadata validation to initial attachment drafts submitted with review creation.

4. Normalize review paging.
   - Introduce a review page result type matching the task page response shape.
   - Update review repositories with pageable queries.
   - Update review list and review history service/controller methods to accept `Pageable` and return paged responses.
   - Keep the existing header auth resolver usage unchanged.

5. Refine audit logging.
   - Add a concrete `AuditLogger` implementation using structured application logs.
   - Standardize metadata keys so every history event includes stable identifiers such as `reviewId`, `taskId`, `actionType`, `targetType`, `targetId`, and `actorId`.

6. Compile verification.
   - Run `./gradlew compileJava`.
   - If compilation fails, fix only issues directly caused by this scope or the already-broken merged review tree.

## Risks And Mitigations

- Risk: S3 integration can leak into a global upload design prematurely.
  - Mitigation: keep the implementation under `review.service` and document that it is a review-local adapter pending future extraction.

- Risk: fixing compile errors may tempt a wider task/review refactor.
  - Mitigation: keep task-side changes limited to the minimum review transition methods and avoid broader task API redesign.

- Risk: pageable endpoint changes alter API contracts.
  - Mitigation: keep endpoint paths unchanged and normalize payload shape in the same style already used by task paging.

- Risk: disabled local storage bean could hide misconfiguration.
  - Mitigation: fail explicitly at runtime when the storage endpoint is used without required config, while still allowing compilation and boot.

## Verification Steps

- `./gradlew compileJava`
- Manual code review against `/mnt/c/Users/alswl/Desktop/policy-consolidation.md` for:
  - backend-generated object keys
  - separated upload/confirm flow
  - backend-side MIME/name/size validation
  - backend-mediated download presign
  - no global auth flow change in this pass

## Policy Checkpoints

- After storage adapter wiring: confirm presign ownership and object-key generation policy.
- After attachment confirmation changes: confirm backend-side metadata validation policy.
- After paging/logging changes: confirm audit and query behavior still matches the consolidated policy intent.
