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
    CONFLICT(false, HttpStatus.CONFLICT, 4090, "요청이 현재 상태와 충돌합니다."),
    INTERNAL_SERVER_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR, 5000, "서버 오류가 발생했습니다."),

    /* ========= User / Account (2000~) ========= */
    USER_NOT_FOUND(false, HttpStatus.NOT_FOUND, 2000, "해당 유저 정보가 없습니다."),
    PLATFORM_ACCOUNT_REQUIRED(false, HttpStatus.BAD_REQUEST, 2100, "선택한 플랫폼 계정을 먼저 등록해주세요."),
    PLATFORM_ACCOUNT_NOT_FOUND(false, HttpStatus.NOT_FOUND, 2101, "플랫폼 계정 정보를 찾을 수 없습니다."),

    /* ========= Room / Rule (3000~) ========= */
    ROOM_NOT_FOUND(false, HttpStatus.NOT_FOUND, 3000, "해당 방이 존재하지 않습니다."),
    ROOM_NOT_DRAFT(false, HttpStatus.CONFLICT, 3001, "모집(DRAFT) 상태에서만 가능한 작업입니다."),
    ROOM_NOT_ACTIVE(false, HttpStatus.CONFLICT, 3002, "활성(ACTIVE) 상태에서만 가능한 작업입니다."),
    ROOM_ALREADY_ACTIVE(false, HttpStatus.CONFLICT, 3003, "이미 활성화된 방입니다."),
    ROOM_ENDED(false, HttpStatus.CONFLICT, 3004, "종료(ENDED)된 방입니다."),

    ROOM_INVITE_CODE_REQUIRED(false, HttpStatus.BAD_REQUEST, 3100, "비공개(PRIVATE) 방은 초대코드가 필요합니다."),
    ROOM_INVITE_CODE_MISMATCH(false, HttpStatus.BAD_REQUEST, 3101, "초대코드가 올바르지 않습니다."),
    ROOM_NOT_PUBLIC_LISTED(false, HttpStatus.FORBIDDEN, 3102, "공개 게시판에 노출되지 않은 방입니다."),

    ROOM_RULE_NOT_FOUND(false, HttpStatus.NOT_FOUND, 3200, "해당 방의 규칙이 존재하지 않습니다."),
    INVALID_PLATFORM_SELECTION(false, HttpStatus.BAD_REQUEST, 3201, "선택한 플랫폼이 방 규칙(검사 플랫폼)에 포함되어야 합니다."),

    /* ========= Membership (3500~) ========= */
    MEMBERSHIP_ALREADY_EXISTS(false, HttpStatus.CONFLICT, 3500, "이미 방에 참여 중인 사용자입니다."),
    MEMBERSHIP_NOT_FOUND(false, HttpStatus.NOT_FOUND, 3501, "방 참여 정보(멤버십)를 찾을 수 없습니다."),

    /* ========= JoinRequest (3300~) ========= */
    JOIN_REQUEST_NOT_FOUND(false, HttpStatus.NOT_FOUND, 3302, "참여 신청 내역이 존재하지 않습니다."),
    JOIN_REQUEST_ALREADY_EXISTS(false, HttpStatus.CONFLICT, 3300, "이미 참여 신청한 방입니다."),
    JOIN_REQUEST_NOT_PENDING(false, HttpStatus.CONFLICT, 3301, "대기중(PENDING)인 신청만 처리할 수 있습니다."),

    /* ========= Invitation (3400~) ========= */
    INVITATION_NOT_FOUND(false, HttpStatus.NOT_FOUND, 3401, "초대 내역이 존재하지 않습니다."),
    INVITATION_ALREADY_EXISTS(false, HttpStatus.CONFLICT, 3400, "이미 초대가 존재합니다."),
    INVITATION_NOT_PENDING(false, HttpStatus.CONFLICT, 3402, "대기중(PENDING)인 초대만 처리할 수 있습니다."),
    INVITATION_FORBIDDEN(false, HttpStatus.FORBIDDEN, 3403, "초대를 처리할 권한이 없습니다.");

    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final int code;          // 앱 내부 코드
    private final String message;

    BaseResponseStatus(boolean isSuccess, HttpStatus httpStatus, int code, String message) {
        this.isSuccess = isSuccess;
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
