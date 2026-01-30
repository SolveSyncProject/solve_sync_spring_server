package com.project.solvesync.global.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.failure(status, ex.getMessage()));
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
}
