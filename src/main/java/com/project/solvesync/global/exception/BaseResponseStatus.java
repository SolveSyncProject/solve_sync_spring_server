package com.project.solvesync.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BaseResponseStatus {

    /* ========= 공통 성공 ========= */
    SUCCESS(true, HttpStatus.OK, 1000, "요청에 성공했습니다."),

    /* ========= 공통 실패 ========= */
    BAD_REQUEST(false, HttpStatus.BAD_REQUEST, 4000, "잘못된 요청입니다."),
    VALIDATION_ERROR(false, HttpStatus.BAD_REQUEST, 4001, "요청 값이 올바르지 않습니다."),
    UNAUTHORIZED(false, HttpStatus.UNAUTHORIZED, 4010, "인증이 필요합니다."),
    FORBIDDEN(false, HttpStatus.FORBIDDEN, 4030, "권한이 없습니다."),
    NOT_FOUND(false, HttpStatus.NOT_FOUND, 4040, "리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR, 5000, "서버 오류가 발생했습니다."),

    /* ========= 도메인(예시: SolveSync MVP) ========= */
    USER_NOT_FOUND(false, HttpStatus.NOT_FOUND, 2000, "해당 유저 정보가 없습니다."),
    PLATFORM_ACCOUNT_REQUIRED(false, HttpStatus.BAD_REQUEST, 2100, "선택한 플랫폼 계정을 먼저 등록해주세요."),

    ROOM_NOT_FOUND(false, HttpStatus.NOT_FOUND, 3000, "해당 방이 존재하지 않습니다."),
    ROOM_RULE_NOT_FOUND(false, HttpStatus.NOT_FOUND, 3001, "해당 방의 규칙이 존재하지 않습니다."),

    INVALID_PLATFORM_SELECTION(false, HttpStatus.BAD_REQUEST, 3200, "선택한 플랫폼이 방 규칙(검사 플랫폼)에 포함되어야 합니다."),
    JOIN_REQUEST_ALREADY_EXISTS(false, HttpStatus.CONFLICT, 3300, "이미 참여 신청한 방입니다."),
    JOIN_REQUEST_NOT_PENDING(false, HttpStatus.CONFLICT, 3301, "대기중(PENDING)인 신청만 처리할 수 있습니다."),
    JOIN_REQUEST_NOT_FOUND(false, HttpStatus.NOT_FOUND, 3302, "참여 신청 내역이 존재하지 않습니다.");

    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final int code;          // ✅ 앱 내부 코드 (1000, 3000 …)
    private final String message;    // ✅ 기본 메시지

    BaseResponseStatus(boolean isSuccess, HttpStatus httpStatus, int code, String message) {
        this.isSuccess = isSuccess;
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
