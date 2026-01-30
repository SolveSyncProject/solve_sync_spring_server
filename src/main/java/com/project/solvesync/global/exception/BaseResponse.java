package com.project.solvesync.global.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BaseResponse<T> {

    private final boolean isSuccess;
    private final String message;
    private final int code;
    private final T data;

    @Builder
    private BaseResponse(boolean isSuccess, String message, int code, T data) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.code = code;
        this.data = data;
    }

    /* ========= Success ========= */

    public static <T> BaseResponse<T> success(T data) {
        BaseResponseStatus status = BaseResponseStatus.SUCCESS;
        return BaseResponse.<T>builder()
                .isSuccess(status.isSuccess())
                .code(status.getCode())
                .message(status.getMessage())
                .data(data)
                .build();
    }

    public static BaseResponse<Void> success() {
        BaseResponseStatus status = BaseResponseStatus.SUCCESS;
        return BaseResponse.<Void>builder()
                .isSuccess(status.isSuccess())
                .code(status.getCode())
                .message(status.getMessage())
                .build();
    }

    /** 성공이지만 SUCCESS가 아닌 커스텀 성공 메시지/코드를 쓰고 싶을 때 */
    public static <T> BaseResponse<T> success(BaseResponseStatus status, T data) {
        return BaseResponse.<T>builder()
                .isSuccess(status.isSuccess())
                .code(status.getCode())
                .message(status.getMessage())
                .data(data)
                .build();
    }

    /* ========= Failure ========= */

    public static BaseResponse<Void> failure(BaseResponseStatus status) {
        return BaseResponse.<Void>builder()
                .isSuccess(status.isSuccess())
                .code(status.getCode())
                .message(status.getMessage())
                .build();
    }

    /** 실패인데 메시지를 상황별로 다르게 주고 싶을 때(검증 메시지 등) */
    public static BaseResponse<Void> failure(BaseResponseStatus status, String messageOverride) {
        return BaseResponse.<Void>builder()
                .isSuccess(status.isSuccess())
                .code(status.getCode())
                .message(messageOverride)
                .build();
    }
}
