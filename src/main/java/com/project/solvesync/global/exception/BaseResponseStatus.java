package com.project.solvesync.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BaseResponseStatus {

    /* ========= Global ========= */
    SUCCESS(true, HttpStatus.OK, 1000, "요청에 성공하였습니다."),
    BAD_REQUEST(false, HttpStatus.BAD_REQUEST, 1001, "잘못된 요청입니다."),
    VALIDATION_ERROR(false, HttpStatus.BAD_REQUEST, 1002, "요청 값 검증에 실패했습니다."),
    FORBIDDEN(false, HttpStatus.FORBIDDEN, 1003, "권한이 없습니다."),
    INTERNAL_SERVER_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR, 1004, "서버 에러가 발생했습니다."),
    CONFLICT(false, HttpStatus.CONFLICT, 1005, "요청이 충돌했습니다."),

    /* ========= User / Account (2000~) ========= */
    USER_NOT_FOUND(false, HttpStatus.NOT_FOUND, 2000, "해당 유저 정보가 없습니다."),
    USERNAME_REQUIRED(false, HttpStatus.BAD_REQUEST, 2001, "username은 필수입니다."),
    USERNAME_ALREADY_TAKEN(false, HttpStatus.CONFLICT, 2002, "이미 사용 중인 username입니다."),
    USERNAME_NOT_FOUND(false, HttpStatus.NOT_FOUND, 2003, "해당 username의 유저를 찾을 수 없습니다."),

    PLATFORM_ACCOUNT_REQUIRED(false, HttpStatus.BAD_REQUEST, 2100, "플랫폼 계정 등록이 필요합니다."),
    // ✅ 01:30~03:00 점검 시간에 플랫폼 계정 등록/수정 차단
    PLATFORM_ACCOUNT_MAINTENANCE(false, HttpStatus.SERVICE_UNAVAILABLE, 2101, "플랫폼 계정 점검시간입니다. 잠시 후 다시 시도해주세요."),

    /* ========= Room (3000~) ========= */
    ROOM_NOT_FOUND(false, HttpStatus.NOT_FOUND, 3000, "스터디 룸을 찾을 수 없습니다."),
    ROOM_NOT_DRAFT(false, HttpStatus.BAD_REQUEST, 3001, "DRAFT 상태에서만 가능한 작업입니다."),
    ROOM_NOT_ACTIVE(false, HttpStatus.BAD_REQUEST, 3002, "ACTIVE 상태에서만 가능한 작업입니다."),
    ROOM_INVITE_CODE_REQUIRED(false, HttpStatus.BAD_REQUEST, 3003, "초대코드가 필요합니다."),
    ROOM_INVITE_CODE_MISMATCH(false, HttpStatus.BAD_REQUEST, 3004, "초대코드가 일치하지 않습니다."),
    ROOM_NOT_PUBLIC_LISTED(false, HttpStatus.BAD_REQUEST, 3005, "공개 목록에 노출된 방만 초대코드 없이 신청할 수 있습니다."),
    ROOM_RULE_INVALID(false, HttpStatus.BAD_REQUEST, 3006, "방 규칙 설정이 유효하지 않습니다."),
    INVALID_PLATFORM_SELECTION(false, HttpStatus.BAD_REQUEST, 3007, "방에서 허용하지 않는 플랫폼이 포함되어 있습니다."),
    OWNER_PLATFORM_ACCOUNT_REQUIRED(false, HttpStatus.BAD_REQUEST, 3008, "방장은 방 생성 전 해당 플랫폼 계정 등록이 필요합니다."),

    /* ========= Membership (4000~) ========= */
    MEMBERSHIP_ALREADY_EXISTS(false, HttpStatus.CONFLICT, 4000, "이미 멤버십이 존재합니다."),
    MEMBERSHIP_NOT_FOUND(false, HttpStatus.NOT_FOUND, 4001, "멤버십을 찾을 수 없습니다."),
    MEMBERSHIP_FORBIDDEN(false, HttpStatus.FORBIDDEN, 4002, "멤버십에 대한 권한이 없습니다."),

    /* ========= Join Request (5000~) ========= */
    JOIN_REQUEST_NOT_FOUND(false, HttpStatus.NOT_FOUND, 5000, "참여 신청을 찾을 수 없습니다."),
    JOIN_REQUEST_NOT_PENDING(false, HttpStatus.BAD_REQUEST, 5001, "PENDING 상태가 아닙니다."),
    JOIN_REQUEST_ALREADY_EXISTS(false, HttpStatus.CONFLICT, 5002, "이미 참여 신청(PENDING)이 존재합니다."),
    JOIN_REQUEST_CONFLICT_INVITATION(false, HttpStatus.CONFLICT, 5003, "이미 PENDING 초대가 존재하여 신청할 수 없습니다."),

    /* ========= Invitation (6000~) ========= */
    INVITATION_NOT_FOUND(false, HttpStatus.NOT_FOUND, 6000, "초대를 찾을 수 없습니다."),
    INVITATION_FORBIDDEN(false, HttpStatus.FORBIDDEN, 6001, "초대에 대한 권한이 없습니다."),
    INVITATION_NOT_PENDING(false, HttpStatus.BAD_REQUEST, 6002, "PENDING 상태가 아닙니다."),
    INVITATION_ALREADY_EXISTS(false, HttpStatus.CONFLICT, 6003, "이미 PENDING 초대가 존재합니다.");

    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    BaseResponseStatus(boolean isSuccess, HttpStatus httpStatus, int code, String message) {
        this.isSuccess = isSuccess;
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
