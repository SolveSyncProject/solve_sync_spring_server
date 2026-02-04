package com.project.solvesync.domain.user.controller;

import com.project.solvesync.domain.user.dto.UserDtos;
import com.project.solvesync.domain.user.service.UserService;
import com.project.solvesync.global.exception.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me")
public class MeUserController {

    private final UserService userService;

    /** MVP: 인증 대신 X-User-Id 헤더로 본인 식별 */
    @GetMapping
    public BaseResponse<UserDtos.MeResponse> me(@RequestHeader("X-User-Id") Long userId) {
        return BaseResponse.success(userService.getMe(userId));
    }

    /** username(공개 식별자) 수정 */
    @PutMapping("/username")
    public BaseResponse<UserDtos.UpdateUsernameResponse> updateUsername(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UserDtos.UpdateUsernameRequest request
    ) {
        return BaseResponse.success(userService.updateUsername(userId, request));
    }
}
