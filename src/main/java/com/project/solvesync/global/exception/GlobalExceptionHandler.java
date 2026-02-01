package com.project.solvesync.global.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException e) {
        BaseResponseStatus status = e.getStatus();
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.failure(status, e.getMessage()));
    }

    /** @Valid DTO 검증 실패 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        String message = buildFieldErrorMessage(result);

        BaseResponseStatus status = BaseResponseStatus.VALIDATION_ERROR;
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.failure(status, message));
    }

    /** @Validated + RequestParam/PathVariable 검증 실패 등 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        BaseResponseStatus status = BaseResponseStatus.VALIDATION_ERROR;
        String message = ex.getConstraintViolations().stream()
                .map(this::formatViolation)
                .collect(Collectors.joining(" | "));
        if (message.isBlank()) message = status.getMessage();

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.failure(status, message));
    }

    /**
     * ✅ DB 무결성/제약조건 위반 (유니크 키, FK, NOT NULL 등)
     * - 대표적으로 "이미 가입된 사용자" 같은 상황이 여기로 떨어짐
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // 기본은 CONFLICT로 내려주는 게 안전
        BaseResponseStatus status = BaseResponseStatus.CONFLICT;

        // 메시지는 너무 자세히 노출하면 안 좋아서, 기본 메시지 + 필요 시만 힌트
        String rootMsg = rootMessage(ex);
        String message = status.getMessage();

        // 중복키(Duplicate entry) 등 흔한 케이스에 대해 힌트 제공 (선택)
        if (rootMsg != null) {
            String lower = rootMsg.toLowerCase();
            if (lower.contains("duplicate") || lower.contains("unique")) {
                message = "이미 존재하는 값입니다. (중복 요청)";
            } else if (lower.contains("cannot be null") || lower.contains("not-null")) {
                message = "필수 값이 누락되었습니다.";
                status = BaseResponseStatus.VALIDATION_ERROR;
            } else if (lower.contains("foreign key")) {
                message = "참조 무결성 위반입니다. (연관 리소스가 존재하지 않을 수 있습니다.)";
                status = BaseResponseStatus.BAD_REQUEST;
            }
        }

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.failure(status, message));
    }

    /**
     * ✅ 트랜잭션 커밋 시점에 엔티티 검증이 터지는 경우
     * (Hibernate Validator가 TransactionSystemException으로 감싸서 던질 수 있음)
     */
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<BaseResponse<Void>> handleTransactionSystemException(TransactionSystemException ex) {
        Throwable root = rootCause(ex);
        if (root instanceof ConstraintViolationException cve) {
            BaseResponseStatus status = BaseResponseStatus.VALIDATION_ERROR;
            String message = cve.getConstraintViolations().stream()
                    .map(this::formatViolation)
                    .collect(Collectors.joining(" | "));
            if (message.isBlank()) message = status.getMessage();

            return ResponseEntity
                    .status(status.getHttpStatus())
                    .body(BaseResponse.failure(status, message));
        }

        // 그 외는 서버 에러로
        BaseResponseStatus status = BaseResponseStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.failure(status));
    }

    /** 마지막 안전망 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleException(Exception ex) {
        BaseResponseStatus status = BaseResponseStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.failure(status));
    }

    private String buildFieldErrorMessage(BindingResult result) {
        StringBuilder sb = new StringBuilder();
        for (FieldError error : result.getFieldErrors()) {
            sb.append("[")
                    .append(error.getField())
                    .append("] ")
                    .append(error.getDefaultMessage())
                    .append(" ");
        }
        return sb.toString().trim();
    }

    private String formatViolation(ConstraintViolation<?> v) {
        String path = v.getPropertyPath() == null ? "" : v.getPropertyPath().toString();
        String msg = v.getMessage() == null ? "" : v.getMessage();
        return "[" + path + "] " + msg;
    }

    private String rootMessage(Throwable t) {
        Throwable root = rootCause(t);
        return root == null ? null : root.getMessage();
    }

    private Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur != null && cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur;
    }
}
