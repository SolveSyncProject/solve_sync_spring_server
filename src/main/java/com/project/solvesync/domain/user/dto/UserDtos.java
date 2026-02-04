package com.project.solvesync.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDtos {

    /** (DEV) OAuth 없이 사용자 레코드 생성 */
    public record DevCreateRequest(
            @Email String email,

            /** 선택: 있으면 유니크 체크 */
            @Size(min = 2, max = 50)
            String username
    ) {}

    public record DevCreateResponse(
            Long userId,
            String email,
            String username
    ) {}

    /** /api/me 응답 */
    public record MeResponse(
            Long userId,
            String email,
            String username
    ) {}

    /** /api/me/username 업데이트 */
    public record UpdateUsernameRequest(
            @NotBlank
            @Size(min = 2, max = 50)
            String username
    ) {}

    public record UpdateUsernameResponse(
            Long userId,
            String username
    ) {}
}
