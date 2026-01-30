package com.project.solvesync.global.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final BaseResponseStatus status;

    public BaseException(BaseResponseStatus status) {
        super(status.getMessage());
        this.status = status; // ✅ 누락 버그 수정
    }

    public BaseException(BaseResponseStatus status, String message) {
        super(message);
        this.status = status;
    }
}
